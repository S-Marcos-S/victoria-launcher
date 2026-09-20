// SPDX-License-Identifier: GPL-3.0-or-later
package dev.victorialauncher.ui.applist

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.victorialauncher.data.EdgeSide
import kotlin.math.roundToInt

/**
 * Indicador de letra em Liquid Glass inspirado no sistema Apple (iOS / visionOS).
 *
 * Implementa de forma física e visualmente autêntica:
 * 1. Substrato translúcido de vidro leitoso (frosted glass) com dupla sombra de elevação;
 * 2. Refração e dispersão cromática espectral (chromatic aberration) nas bordas chanfradas (franja ciano-azul no topo e magenta-âmbar na base);
 * 3. Menisco de espessura de vidro 3D e reflexo especular direcional ultra nítido;
 * 4. Reflexo cáustico de lente esférica no domo superior;
 * 5. Tipografia suspensa no centro do cristal com brilho luminescente e sombra de profundidade;
 * 6. Microfísica fluida responsiva a cada troca de letra.
 */
@Composable
fun LiquidGlassLetterIndicator(
    letter: Char,
    activeSide: EdgeSide,
    pullPx: () -> Float,
    scrubY: () -> Float?,
    fontFamily: FontFamily? = null,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val bubbleSize = 76.dp
    val halfPx = with(density) { (bubbleSize / 2).toPx() }
    val insetPx = with(density) { 120.dp.toPx() }

    // Micro-interação fluida ao trocar de caractere
    val scaleAnim = remember { Animatable(1f) }
    LaunchedEffect(letter) {
        scaleAnim.snapTo(0.92f)
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        )
    }

    Box(
        modifier = modifier
            .offset {
                val x = insetPx + pullPx()
                IntOffset(
                    x = if (activeSide == EdgeSide.LEFT) x.roundToInt() else -x.roundToInt(),
                    y = ((scrubY() ?: 0f) - halfPx).roundToInt(),
                )
            }
            .size(bubbleSize)
            .graphicsLayer {
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
            }
            // Sombra física profunda de vidro suspenso
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0x66000000),
                spotColor = Color(0x88000000),
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Camadas de Vidro Líquido com Refração Cromática
        Canvas(modifier = Modifier.size(bubbleSize)) {
            val w = size.width
            val h = size.height
            val cornerRadius = 24.dp.toPx()
            val outerPath = Path().apply {
                addRoundRect(RoundRect(0f, 0f, w, h, CornerRadius(cornerRadius, cornerRadius)))
            }

            // 1. Corpo translúcido de vidro (gradiente de refração de volume)
            val glassBodyBrush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.32f),
                    Color.White.copy(alpha = 0.12f),
                    Color(0xFF101015).copy(alpha = 0.28f),
                ),
                start = Offset(0f, 0f),
                end = Offset(w, h),
            )
            drawRoundRect(
                brush = glassBodyBrush,
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            )

            // Efeitos internos com máscara de corte do vidro
            clipPath(outerPath) {
                // 2. Refração Cromática (Dispersão espectral diagonal interna)
                val spectralWashBrush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF00F5FF).copy(alpha = 0.18f),
                        Color.Transparent,
                        Color(0xFFFF007A).copy(alpha = 0.16f),
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(w, h),
                )
                drawRect(brush = spectralWashBrush)

                // 3. Aberração Cromática - Franja Ciano/Azul Elétrico (Borda Superior-Esquerda)
                val cyanFringeBrush = Brush.sweepGradient(
                    0.0f to Color(0xFF00F5FF).copy(alpha = 0.70f),
                    0.20f to Color(0xFF0072FF).copy(alpha = 0.50f),
                    0.35f to Color.Transparent,
                    0.80f to Color.Transparent,
                    1.0f to Color(0xFF00F5FF).copy(alpha = 0.70f),
                    center = Offset(w / 2f, h / 2f),
                )
                drawRoundRect(
                    brush = cyanFringeBrush,
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = 3.dp.toPx()),
                )

                // 4. Aberração Cromática - Franja Magenta/Coral/Âmbar (Borda Inferior-Direita)
                val magentaFringeBrush = Brush.sweepGradient(
                    0.0f to Color.Transparent,
                    0.40f to Color.Transparent,
                    0.56f to Color(0xFFFF007A).copy(alpha = 0.65f),
                    0.72f to Color(0xFFFF6600).copy(alpha = 0.55f),
                    0.84f to Color(0xFFFFCC00).copy(alpha = 0.40f),
                    1.0f to Color.Transparent,
                    center = Offset(w / 2f, h / 2f),
                )
                drawRoundRect(
                    brush = magentaFringeBrush,
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = 3.dp.toPx()),
                )

                // 5. Reflexo Cáustico de Lente Esférica (Domo de luz superior)
                val causticGlareBrush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.15f),
                        Color.Transparent,
                    ),
                    center = Offset(w * 0.45f, h * 0.22f),
                    radius = w * 0.48f,
                )
                drawRoundRect(
                    brush = causticGlareBrush,
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                )

                // 6. Menisco de Vidro Interno (Simula espessura física da placa de cristal)
                val innerMargin = 2.dp.toPx()
                val innerCorner = (cornerRadius - innerMargin).coerceAtLeast(0f)
                val meniscusBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.50f),
                        Color.Transparent,
                        Color.White.copy(alpha = 0.25f),
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(w, h),
                )
                drawRoundRect(
                    brush = meniscusBrush,
                    topLeft = Offset(innerMargin, innerMargin),
                    size = Size(w - innerMargin * 2f, h - innerMargin * 2f),
                    cornerRadius = CornerRadius(innerCorner, innerCorner),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }

            // 7. Borda Especular Chanfrada Ultra Nítida (Contorno externo com reflexos de luz)
            val outerSpecularBrush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.95f),
                    Color.White.copy(alpha = 0.40f),
                    Color.White.copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.75f),
                ),
                start = Offset(0f, 0f),
                end = Offset(w, h),
            )
            drawRoundRect(
                brush = outerSpecularBrush,
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = 1.5.dp.toPx()),
            )
        }

        // Elemento Interno: Letra ou Estrela Suspensa no Cristal Líquido
        if (letter == SCRUBBER_STAR) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer {
                        shadowElevation = 8f
                    },
            )
        } else {
            Text(
                text = letter.toString(),
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                fontFamily = fontFamily,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.55f),
                        offset = Offset(0f, 4f),
                        blurRadius = 8f,
                    ),
                ),
            )
        }
    }
}
