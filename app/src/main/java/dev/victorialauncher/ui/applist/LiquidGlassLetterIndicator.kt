// SPDX-License-Identifier: GPL-3.0-or-later
package dev.victorialauncher.ui.applist

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.victorialauncher.data.EdgeSide
import kotlin.math.roundToInt

/**
 * Mostrador de letra em "Liquid Glass" fiel à linguagem de design da Apple (iOS 26 / visionOS).
 *
 * Princípios ópticos e de engenharia visual implementados:
 * 1. Geometria de gota líquida com curvatura contínua (squircle de 25dp) e física de tensão superficial.
 * 2. Substrato de vidro cristalino translúcido com dispersão volumétrica e gradiente de incidência luminoso.
 * 3. Reflexo cáustico de cúpula convexa esférica (lensing dome glare) que confere volume tátil tridimensional.
 * 4. Dispersão cromática espectral física (Snell's Law / Cauchy):
 *    - Fringes de refração suaves e fotorrealistas posicionadas rigorosamente nos arcos de maior curvatura
 *      (arco superior-esquerdo para comprimentos de onda curtos ciano/azul-gelo a 225°;
 *       arco inferior-direito para comprimentos de onda longos pêssego/âmbar a 45°),
 *      sem coloração artificial berrante ou duplicidade de texto.
 * 5. Menisco interno de espessura de vidro (Inner Meniscus Bevel) reproduzindo a reflexão interna de parede.
 * 6. Chanfrado especular de borda externa nítida (Outer Specular Rim / Fresnel Highlight) com luz de 135°.
 * 7. Tipografia suspensa de alta legibilidade (branco puro com sombra óptica de profundidade),
 *    dando a percepção óptica exata de visualização através de uma lente líquida convexa.
 * 8. Resposta micro-física elástica (spring physics) a cada transição de letra do alfabeto.
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
    val bubbleSize = 74.dp
    val halfPx = with(density) { (bubbleSize / 2).toPx() }
    val insetPx = with(density) { 120.dp.toPx() }

    // Micro-interação de tensão superficial líquida com resposta de mola elástica (spring physics)
    val springScale = remember { Animatable(1f) }
    LaunchedEffect(letter) {
        springScale.snapTo(0.91f)
        springScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
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
                scaleX = springScale.value
                scaleY = springScale.value
            }
            // Sombra física de elevação e suspensão óptica no espaço 3D
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(25.dp),
                ambientColor = Color(0x40000000),
                spotColor = Color(0x60000000),
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Camadas físicas do Vidro Líquido: Óptica, Refração, Menisco e Especularidade
        Canvas(modifier = Modifier.size(bubbleSize)) {
            val w = size.width
            val h = size.height
            val cornerRadius = 25.dp.toPx()
            val outerPath = Path().apply {
                addRoundRect(RoundRect(0f, 0f, w, h, CornerRadius(cornerRadius, cornerRadius)))
            }

            // 1. Substrato de vidro cristalino translúcido (alta transparência luminosa)
            val crystalSubstrateBrush = Brush.linearGradient(
                0.00f to Color.White.copy(alpha = 0.28f),
                0.32f to Color.White.copy(alpha = 0.12f),
                0.68f to Color(0xFFDCE5F0).copy(alpha = 0.08f),
                1.00f to Color(0xFF0F172A).copy(alpha = 0.20f),
                start = Offset(0f, 0f),
                end = Offset(w, h),
            )
            drawRoundRect(
                brush = crystalSubstrateBrush,
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            )

            clipPath(outerPath) {
                // 2. Reflexo Cáustico de Cúpula Convexa Esférica (Lensing Dome Glare)
                // Cria a percepção volumétrica de gota de vidro líquido 3D com luz incidente superior
                val causticDomeBrush = Brush.radialGradient(
                    0.00f to Color.White.copy(alpha = 0.42f),
                    0.45f to Color.White.copy(alpha = 0.12f),
                    1.00f to Color.Transparent,
                    center = Offset(w * 0.44f, h * 0.22f),
                    radius = w * 0.52f,
                )
                drawRoundRect(
                    brush = causticDomeBrush,
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                )

                // 3. Dispersão Cromática Espectral Física (Aberração Óptica nos Bordos Refrativos)
                // No vidro líquido real, a luz branca que refrata nos arcos de alta curvatura sofre dispersão:
                // - Arco superior-esquerdo (quadrante 3, centro em 225° / normal 0.625): azul-gelo/ciano
                val incidentDispersionBrush = Brush.sweepGradient(
                    0.00f to Color.Transparent,
                    0.46f to Color.Transparent,
                    0.54f to Color(0xFF80D8FF).copy(alpha = 0.38f), // Ice blue
                    0.625f to Color(0xFF00E5FF).copy(alpha = 0.42f), // Cyan crest
                    0.71f to Color(0xFFB388FF).copy(alpha = 0.22f), // Soft violet
                    0.79f to Color.Transparent,
                    1.00f to Color.Transparent,
                    center = Offset(w / 2f, h / 2f),
                )
                drawRoundRect(
                    brush = incidentDispersionBrush,
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = 2.4.dp.toPx()),
                )

                // - Arco inferior-direito (quadrante 1, centro em 45° / normal 0.125): pêssego/âmbar
                val exitDispersionBrush = Brush.sweepGradient(
                    0.00f to Color.Transparent,
                    0.05f to Color(0xFFFFAB91).copy(alpha = 0.22f), // Coral soft
                    0.125f to Color(0xFFFFB74D).copy(alpha = 0.32f), // Warm amber
                    0.20f to Color(0xFFFFE082).copy(alpha = 0.18f), // Pale gold
                    0.26f to Color.Transparent,
                    1.00f to Color.Transparent,
                    center = Offset(w / 2f, h / 2f),
                )
                drawRoundRect(
                    brush = exitDispersionBrush,
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = 2.4.dp.toPx()),
                )

                // 4. Menisco Interno de Espessura do Vidro (Inner Meniscus Bevel)
                // Simula a parede física e a reflexão da face interna da lente líquida
                val innerInset = 2.2.dp.toPx()
                val innerCorner = (cornerRadius - innerInset).coerceAtLeast(0f)
                val innerMeniscusBrush = Brush.linearGradient(
                    0.00f to Color.White.copy(alpha = 0.55f),
                    0.40f to Color.Transparent,
                    0.75f to Color.Transparent,
                    1.00f to Color.White.copy(alpha = 0.20f),
                    start = Offset(0f, 0f),
                    end = Offset(w, h),
                )
                drawRoundRect(
                    brush = innerMeniscusBrush,
                    topLeft = Offset(innerInset, innerInset),
                    size = Size(w - innerInset * 2f, h - innerInset * 2f),
                    cornerRadius = CornerRadius(innerCorner, innerCorner),
                    style = Stroke(width = 1.2.dp.toPx()),
                )
            }

            // 5. Chanfrado Especular Externo Nítido (Outer Specular Rim / Fresnel Glare)
            // Traçado milimétrico de luz no perímetro polido do cristal com foco a 135°
            val outerSpecularBrush = Brush.linearGradient(
                0.00f to Color.White.copy(alpha = 0.95f),
                0.28f to Color.White.copy(alpha = 0.35f),
                0.52f to Color.White.copy(alpha = 0.10f),
                0.80f to Color.White.copy(alpha = 0.30f),
                1.00f to Color.White.copy(alpha = 0.60f),
                start = Offset(0f, 0f),
                end = Offset(w, h),
            )
            drawRoundRect(
                brush = outerSpecularBrush,
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = 1.5.dp.toPx()),
            )
        }

        // Camada Tipográfica Suspensa: Letra ou Ícone Nítido com Profundidade Óptica
        if (letter == SCRUBBER_STAR) {
            Box(contentAlignment = Alignment.Center) {
                // Sombra de profundidade óptica para suspensão no meio vítreo
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = Color(0x60000000),
                    modifier = Modifier
                        .size(38.dp)
                        .offset(y = 3.dp),
                )
                // Ícone principal límpido e cristalino
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp),
                )
            }
        } else {
            // Tipografia pura de altíssima legibilidade suspensa dentro da lente líquida
            Text(
                text = letter.toString(),
                color = Color.White,
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color(0x75000000),
                        offset = Offset(0f, with(density) { 3.dp.toPx() }),
                        blurRadius = with(density) { 6.dp.toPx() },
                    ),
                ),
            )
        }
    }
}
