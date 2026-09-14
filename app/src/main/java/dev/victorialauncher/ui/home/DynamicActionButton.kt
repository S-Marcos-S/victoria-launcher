// SPDX-License-Identifier: GPL-3.0-or-later
package dev.victorialauncher.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.victorialauncher.data.AppInfo
import dev.victorialauncher.data.EdgeSide
import dev.victorialauncher.service.HapticUtil
import dev.victorialauncher.ui.common.AppIcon
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.tanh

/**
 * Modern dynamic action button placed next to the alphabet at the bottom of the home screen.
 *
 * Supports three gestures:
 * 1. Tap: Opens [clickApp] (or settings if unassigned) with elastic press/release bounce.
 * 2. Swipe Up: Opens [swipeUpApp] with realistic rubber-band stretch and spring return.
 * 3. Swipe Down: Opens [swipeDownApp] with rubber-band stretch and spring return.
 * 4. Long press: Opens dynamic button settings to customize actions.
 */
@Composable
fun DynamicActionButton(
    clickApp: AppInfo?,
    swipeUpApp: AppInfo?,
    swipeDownApp: AppInfo?,
    side: EdgeSide,
    hapticsEnabled: Boolean,
    contentColor: Color,
    onLaunch: (AppInfo) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveDemo: Boolean = false,
) {
    val view = LocalView.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val viewConfig = LocalViewConfiguration.current

    val buttonSizeDp = 54.dp
    val maxStretchPx = with(density) { 80.dp.toPx() }
    val thresholdPx = with(density) { 38.dp.toPx() }

    // Physical rubber-band state holders
    val dragOffsetY = remember { Animatable(0f) }
    val stretchScaleX = remember { Animatable(1f) }
    val stretchScaleY = remember { Animatable(1f) }
    val pressScale = remember { Animatable(1f) }

    var isDragging by remember { mutableStateOf(false) }
    var thresholdCrossed by remember { mutableStateOf(false) }
    var currentGesture by remember { mutableStateOf<DynamicGesture>(DynamicGesture.NONE) }

    val transformOrigin = remember(dragOffsetY.value) {
        if (dragOffsetY.value < 0f) {
            TransformOrigin(0.5f, 0.92f) // Ancorado na base ao esticar para cima
        } else if (dragOffsetY.value > 0f) {
            TransformOrigin(0.5f, 0.08f) // Ancorado no topo ao esticar para baixo
        } else {
            TransformOrigin.Center
        }
    }

    // Spring specs for rubber release bounce
    val rubberSpring = remember {
        spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        )
    }

    val pressSpring = remember {
        spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        )
    }

    fun releaseRubber(fireAction: Boolean) {
        val gesture = currentGesture
        val crossed = thresholdCrossed

        scope.launch {
            // Coordinate rubber bounce return
            launch { dragOffsetY.animateTo(0f, rubberSpring) }
            launch { stretchScaleX.animateTo(1f, rubberSpring) }
            launch { stretchScaleY.animateTo(1f, rubberSpring) }
            launch { pressScale.animateTo(1f, pressSpring) }
        }

        if (fireAction && crossed) {
            HapticUtil.tick(view, hapticsEnabled)
            when (gesture) {
                DynamicGesture.SWIPE_UP -> {
                    if (swipeUpApp != null) onLaunch(swipeUpApp) else onOpenSettings()
                }
                DynamicGesture.SWIPE_DOWN -> {
                    if (swipeDownApp != null) onLaunch(swipeDownApp) else onOpenSettings()
                }
                else -> Unit
            }
        }

        isDragging = false
        thresholdCrossed = false
        currentGesture = DynamicGesture.NONE
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        // Indicador flutuante para Swipe Up (arraste para cima)
        val showUpBubble = isDragging && dragOffsetY.value < -10f
        val upBubbleAlpha = (abs(dragOffsetY.value) / thresholdPx).coerceIn(0f, 1f)
        val upThresholdMet = thresholdCrossed && currentGesture == DynamicGesture.SWIPE_UP

        if (showUpBubble) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = 0,
                            y = (dragOffsetY.value - with(density) { 56.dp.toPx() }).roundToInt(),
                        )
                    }
                    .graphicsLayer {
                        alpha = upBubbleAlpha
                        scaleX = 0.75f + 0.25f * upBubbleAlpha
                        scaleY = 0.75f + 0.25f * upBubbleAlpha
                    },
            ) {
                ActionTeaserBubble(
                    app = swipeUpApp,
                    isThresholdMet = upThresholdMet,
                    gestureIcon = Icons.Filled.KeyboardArrowUp,
                    contentColor = contentColor,
                    fallbackLabel = "Puxar para cima",
                )
            }
        }

        // Indicador flutuante para Swipe Down (arraste para baixo)
        val showDownBubble = isDragging && dragOffsetY.value > 10f
        val downBubbleAlpha = (abs(dragOffsetY.value) / thresholdPx).coerceIn(0f, 1f)
        val downThresholdMet = thresholdCrossed && currentGesture == DynamicGesture.SWIPE_DOWN

        if (showDownBubble) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = 0,
                            y = (dragOffsetY.value + with(density) { 56.dp.toPx() }).roundToInt(),
                        )
                    }
                    .graphicsLayer {
                        alpha = downBubbleAlpha
                        scaleX = 0.75f + 0.25f * downBubbleAlpha
                        scaleY = 0.75f + 0.25f * downBubbleAlpha
                    },
            ) {
                ActionTeaserBubble(
                    app = swipeDownApp,
                    isThresholdMet = downThresholdMet,
                    gestureIcon = Icons.Filled.KeyboardArrowDown,
                    contentColor = contentColor,
                    fallbackLabel = "Puxar para baixo",
                )
            }
        }

        // Botão Dinâmico Principal com Squash & Stretch e física elástica
        val surfaceColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.88f)
        val activeBorderColor = if (thresholdCrossed) {
            MaterialTheme.colorScheme.primary
        } else {
            contentColor.copy(alpha = 0.22f)
        }

        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationY = dragOffsetY.value
                    scaleX = stretchScaleX.value * pressScale.value
                    scaleY = stretchScaleY.value * pressScale.value
                    this.transformOrigin = transformOrigin
                }
                .size(buttonSizeDp)
                .shadow(
                    elevation = if (thresholdCrossed) 12.dp else 6.dp,
                    shape = RoundedCornerShape(22.dp),
                    ambientColor = Color.Black.copy(alpha = 0.25f),
                    spotColor = if (thresholdCrossed) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.35f),
                )
                .clip(RoundedCornerShape(22.dp))
                .background(surfaceColor)
                .border(
                    BorderStroke(
                        width = if (thresholdCrossed) 2.dp else 1.dp,
                        color = activeBorderColor,
                    ),
                    shape = RoundedCornerShape(22.dp),
                )
                .pointerInput(clickApp, swipeUpApp, swipeDownApp, hapticsEnabled) {
                    val touchSlop = viewConfig.touchSlop.toFloat()
                    awaitEachGesture {
                        val down: PointerInputChange = awaitFirstDown(requireUnconsumed = false)
                        down.consume()

                        var totalDeltaY = 0f
                        var dragged = false
                        var longPressTriggered = false
                        thresholdCrossed = false
                        currentGesture = DynamicGesture.NONE

                        // Animação de compressão elástica ao tocar
                        scope.launch {
                            pressScale.animateTo(0.88f, pressSpring)
                        }

                        // Long press detector job
                        var longPressJob: Job? = scope.launch {
                            delay(viewConfig.longPressTimeoutMillis)
                            if (!dragged) {
                                longPressTriggered = true
                                HapticUtil.tick(view, hapticsEnabled)
                                onOpenSettings()
                            }
                        }

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break

                            val deltaY = change.position.y - down.position.y
                            totalDeltaY = deltaY

                            if (!dragged && abs(deltaY) > touchSlop) {
                                dragged = true
                                isDragging = true
                                longPressJob?.cancel()
                                longPressJob = null
                            }

                            if (dragged) {
                                change.consume()
                                // Resistência hiperbólica real de borracha (tanh dampening)
                                val dampedY = sign(deltaY) * maxStretchPx * tanh(abs(deltaY) / (maxStretchPx * 1.35f))
                                val ratio = (abs(dampedY) / maxStretchPx).coerceIn(0f, 1f)

                                // Deformação Squash & Stretch de volume conservado
                                val sy = 1.0f + 0.38f * ratio
                                val sx = 1.0f - 0.16f * ratio

                                val gesture = if (dampedY < 0) DynamicGesture.SWIPE_UP else DynamicGesture.SWIPE_DOWN
                                currentGesture = gesture

                                val reachedThreshold = abs(dampedY) >= thresholdPx
                                if (reachedThreshold && !thresholdCrossed) {
                                    thresholdCrossed = true
                                    HapticUtil.tick(view, hapticsEnabled)
                                } else if (!reachedThreshold && thresholdCrossed) {
                                    thresholdCrossed = false
                                }

                                scope.launch {
                                    dragOffsetY.snapTo(dampedY)
                                    stretchScaleY.snapTo(sy)
                                    stretchScaleX.snapTo(sx)
                                }
                            }
                        }

                        longPressJob?.cancel()
                        longPressJob = null

                        if (!dragged && !longPressTriggered) {
                            // Tap / Click rápido
                            HapticUtil.tick(view, hapticsEnabled)
                            scope.launch {
                                pressScale.animateTo(1.08f, pressSpring)
                                pressScale.animateTo(1.0f, pressSpring)
                            }
                            if (clickApp != null) {
                                onLaunch(clickApp)
                            } else {
                                onOpenSettings()
                            }
                            isDragging = false
                            thresholdCrossed = false
                            currentGesture = DynamicGesture.NONE
                        } else if (dragged) {
                            releaseRubber(fireAction = true)
                        } else {
                            // Long press já cuidou
                            scope.launch { pressScale.animateTo(1.0f, pressSpring) }
                            isDragging = false
                            thresholdCrossed = false
                            currentGesture = DynamicGesture.NONE
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            // Conteúdo interno do botão: ícone do app ou ícone de atalho moderno
            if (clickApp != null) {
                AppIcon(
                    app = clickApp,
                    sizeDp = 30,
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.TouchApp,
                    contentDescription = "Dynamic Button",
                    tint = contentColor.copy(alpha = 0.85f),
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

/**
 * Balão flutuante translúcido elegante que surge com mola durante o gesto de puxão elástico.
 */
@Composable
private fun ActionTeaserBubble(
    app: AppInfo?,
    isThresholdMet: Boolean,
    gestureIcon: androidx.compose.ui.graphics.vector.ImageVector,
    contentColor: Color,
    fallbackLabel: String,
) {
    val bubbleColor = if (isThresholdMet) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.88f)
    }

    val bubbleBorderColor = if (isThresholdMet) {
        MaterialTheme.colorScheme.primary
    } else {
        contentColor.copy(alpha = 0.20f)
    }

    Surface(
        shape = CircleShape,
        color = bubbleColor,
        border = BorderStroke(1.5.dp, bubbleBorderColor),
        shadowElevation = if (isThresholdMet) 8.dp else 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (app != null) {
                AppIcon(app = app, sizeDp = 20)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isThresholdMet) FontWeight.Bold else FontWeight.Medium,
                    color = if (isThresholdMet) MaterialTheme.colorScheme.onPrimaryContainer else contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Icon(
                    imageVector = gestureIcon,
                    contentDescription = null,
                    tint = if (isThresholdMet) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = fallbackLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.7f),
                    maxLines = 1,
                )
            }
        }
    }
}

private enum class DynamicGesture {
    NONE,
    SWIPE_UP,
    SWIPE_DOWN,
}
