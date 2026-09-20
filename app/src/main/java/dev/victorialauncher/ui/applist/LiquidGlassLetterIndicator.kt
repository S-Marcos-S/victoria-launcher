// SPDX-License-Identifier: GPL-3.0-or-later
package dev.victorialauncher.ui.applist

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Mostrador de letra em "Liquid Glass" fotorrealista e volumétrico inspirado na estética de vidro líquido da Apple.
 *
 * Arquitetura óptica de 8 camadas físicas:
 * 1. Substrato mineral de safira/cristal denso com alta transmitância luminosa, conferindo presença física contra qualquer papel de parede.
 * 2. Cúpula cáustica de brilho superior (*Curved Dome Glare / Crescent Specular*): reflexo esférico côncavo/convexo no hemisfério superior que confere volume tátil 3D imediato à gota.
 * 3. Luz de rebote inferior (*Bottom Bounce Reflection*): reflexo interno na face curva inferior do cristal.
 * 4. Dispersão cromática espectral fotorrealista (Lei de Snell / Cauchy):
 *    - Arco incidente superior-esquerdo a 225°: refração ciano/azul-elétrico/violeta;
 *    - Arco de saída inferior-direito a 45°: refração coral/âmbar/dourado.
 * 5. Menisco interno de espessura de vidro (*Inner Meniscus Bevel*): refração da parede interna simulando espessura de 3mm.
 * 6. Chanfrado especular externo nítido (*Fresnel Glare Rim*): realce puro com incidência a 135°.
 * 7. Tipografia suspensa e ampliada por lente líquida: halo cáustico de luz traseiro, sombra física de suspensão e caractere cristalino em alto relevo.
 * 8. Dinâmica de gota líquida (*Liquid Squish & Pop*): deformação elástica independente em X e Y simulando tensão superficial.
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
    val bubbleSize = 78.dp
    val halfPx = with(density) { (bubbleSize / 2).toPx() }
    val insetPx = with(density) { 120.dp.toPx() }

    // Dinâmica de tensão superficial líquida: ao trocar de letra, a gota se comprime e estica organicamente
    val springScaleX = remember { Animatable(1f) }
    val springScaleY = remember { Animatable(1f) }
    LaunchedEffect(letter) {
        springScaleX.snapTo(0.90f)
        springScaleY.snapTo(1.08f)
        launch {
            springScaleX.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        }
        launch {
            springScaleY.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        }
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
                scaleX = springScaleX.value
                scaleY = springScaleY.value
            }
            // Sombra física profunda de suspensão 3D sobre o plano de fundo
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color(0x60000000),
                spotColor = Color(0x80000000),
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Pipeline de Renderização Óptica de Vidro Líquido
        Canvas(modifier = Modifier.size(bubbleSize)) {
            val w = size.width
            val h = size.height
            val cornerRadius = 26.dp.toPx()
            val outerPath = Path().apply {
                addRoundRect(RoundRect(0f, 0f, w, h, CornerRadius(cornerRadius, cornerRadius)))
            }

            // 1. Substrato Mineral Translúcido Escuro (Contraste e presença vítrea real)
            val substrateBaseBrush = Brush.linearGradient(
                0.00f to Color(0x551E293B), // Slate cristalino
                0.50f to Color(0x350F172A),
                1.00f to Color(0x45020617),
                start = Offset(0f, 0f),
                end = Offset(w, h),
            )
            drawRoundRect(
                brush = substrateBaseBrush,
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            )

            // 2. Translucidez Leitosa / Brilho de Volume Vítreo (Luminous Glass Frost)
            val frostBrush = Brush.radialGradient(
                0.00f to Color.White.copy(alpha = 0.28f),
                0.55f to Color.White.copy(alpha = 0.12f),
                1.00f to Color.White.copy(alpha = 0.04f),
                center = Offset(w * 0.5f, h * 0.40f),
                radius = w * 0.70f,
            )
            drawRoundRect(
                brush = frostBrush,
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            )

            clipPath(outerPath) {
                // 3. Cúpula Convexa Especular Superior (Crescent Dome Glare da Lente Líquida)
                // O reflexo em meia-lua típico das superfícies líquidas e lentes convexas da Apple
                val topDomeGlare = Brush.verticalGradient(
                    0.00f to Color.White.copy(alpha = 0.70f),
                    0.30f to Color.White.copy(alpha = 0.25f),
                    0.75f to Color.White.copy(alpha = 0.04f),
                    1.00f to Color.Transparent,
                    startY = 0f,
                    endY = h * 0.55f,
                )
                drawOval(
                    brush = topDomeGlare,
                    topLeft = Offset(-w * 0.15f, -h * 0.10f),
                    size = Size(w * 1.30f, h * 0.65f),
                )

                // 4. Luz de Rebote Inferior (Bottom Bounce Glare na curvatura de saída)
                val bottomBounceGlare = Brush.verticalGradient(
                    0.00f to Color.Transparent,
                    0.45f to Color.White.copy(alpha = 0.06f),
                    1.00f to Color.White.copy(alpha = 0.40f),
                    startY = h * 0.50f,
                    endY = h,
                )
                drawOval(
                    brush = bottomBounceGlare,
                    topLeft = Offset(w * 0.05f, h * 0.58f),
                    size = Size(w * 0.90f, h * 0.45f),
                )

                // 5. Dispersão Cromática Espectral Física (Lei de Snell nos arcos de refração)
                // Arco incidente (superior-esquerdo a 225°): comprimentos de onda curtos (ciano elétrico, azul puro e violeta)
                val incidentDispersion = Brush.sweepGradient(
                    0.00f to Color.Transparent,
                    0.45f to Color.Transparent,
                    0.53f to Color(0xFF00E5FF).copy(alpha = 0.75f), // Cyan elétrico
                    0.625f to Color(0xFF0091FF).copy(alpha = 0.80f), // Azul safira
                    0.71f to Color(0xFF7C4DFF).copy(alpha = 0.55f), // Violeta prismático
                    0.80f to Color.Transparent,
                    1.00f to Color.Transparent,
                    center = Offset(w / 2f, h / 2f),
                )
                drawRoundRect(
                    brush = incidentDispersion,
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = 3.2.dp.toPx()),
                )

                // Arco de saída (inferior-direito a 45°): comprimentos de onda longos (coral, âmbar dourado e amarelo solar)
                val exitDispersion = Brush.sweepGradient(
                    0.00f to Color.Transparent,
                    0.04f to Color(0xFFFF5252).copy(alpha = 0.55f), // Coral avermelhado
                    0.125f to Color(0xFFFF9100).copy(alpha = 0.75f), // Âmbar puro
                    0.21f to Color(0xFFFFD740).copy(alpha = 0.60f), // Amarelo dourado
                    0.28f to Color.Transparent,
                    1.00f to Color.Transparent,
                    center = Offset(w / 2f, h / 2f),
                )
                drawRoundRect(
                    brush = exitDispersion,
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = 3.2.dp.toPx()),
                )

                // 6. Menisco Interno de Espessura do Vidro (Inner Meniscus Bevel)
                // Simula o bisel interno e a refração da parede espessa do cristal
                val innerInset = 2.5.dp.toPx()
                val innerCorner = (cornerRadius - innerInset).coerceAtLeast(0f)
                val innerMeniscus = Brush.linearGradient(
                    0.00f to Color.White.copy(alpha = 0.80f),
                    0.30f to Color.White.copy(alpha = 0.20f),
                    0.65f to Color.Transparent,
                    1.00f to Color.White.copy(alpha = 0.40f),
                    start = Offset(0f, 0f),
                    end = Offset(w, h),
                )
                drawRoundRect(
                    brush = innerMeniscus,
                    topLeft = Offset(innerInset, innerInset),
                    size = Size(w - innerInset * 2f, h - innerInset * 2f),
                    cornerRadius = CornerRadius(innerCorner, innerCorner),
                    style = Stroke(width = 1.4.dp.toPx()),
                )
            }

            // 7. Chanfrado Especular Externo Nítido (Outer Specular Rim / Fresnel Highlight)
            // Traço polido de alta reflexão ao redor do perímetro com foco a 135°
            val outerSpecular = Brush.linearGradient(
                0.00f to Color.White.copy(alpha = 0.98f),
                0.28f to Color.White.copy(alpha = 0.45f),
                0.50f to Color.White.copy(alpha = 0.15f),
                0.80f to Color.White.copy(alpha = 0.40f),
                1.00f to Color.White.copy(alpha = 0.85f),
                start = Offset(0f, 0f),
                end = Offset(w, h),
            )
            drawRoundRect(
                brush = outerSpecular,
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = 1.6.dp.toPx()),
            )
        }

        // 8. Tipografia Suspensa e Ampliada por Lente Líquida
        if (letter == SCRUBBER_STAR) {
            Box(contentAlignment = Alignment.Center) {
                // Halo cáustico de retroiluminação óptica através da lente
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.35f),
                                    Color(0xFF80D8FF).copy(alpha = 0.15f),
                                    Color.Transparent,
                                ),
                            ),
                            shape = CircleShape,
                        ),
                )
                // Sombra física projetada para criar percepção de suspensão no interior da gota
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = Color(0x99000000),
                    modifier = Modifier
                        .size(40.dp)
                        .offset(y = 3.5.dp),
                )
                // Ícone frontal puro com relevo de cristal
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp),
                )
            }
        } else {
            Box(contentAlignment = Alignment.Center) {
                // Halo cáustico de retroiluminação óptica através da lente
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.35f),
                                    Color(0xFF80D8FF).copy(alpha = 0.15f),
                                    Color.Transparent,
                                ),
                            ),
                            shape = CircleShape,
                        ),
                )
                // Sombra física de profundidade óptica interna
                Text(
                    text = letter.toString(),
                    color = Color(0x99000000),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = fontFamily,
                    modifier = Modifier.offset(y = 3.5.dp),
                )
                // Caractere cristalino em alto relevo com brilho superior
                Text(
                    text = letter.toString(),
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = fontFamily,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.White.copy(alpha = 0.60f),
                            offset = Offset(0f, -1f),
                            blurRadius = 3f,
                        ),
                    ),
                )
            }
        }
    }
}
