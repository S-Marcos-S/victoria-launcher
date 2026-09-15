// SPDX-License-Identifier: GPL-3.0-or-later
package dev.victorialauncher.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * Material 3 Emphasized Decelerate motion curve for premium, organic deceleration.
 */
private val EasingEmphasizedDecelerate = CubicBezierEasing(0.08f, 0.85f, 0.18f, 1.0f)

private const val TOTAL_DURATION_MS = 620
private const val ITEM_DURATION_MS = 400
private const val MAX_DELAY_FRACTION = (TOTAL_DURATION_MS - ITEM_DURATION_MS).toFloat() / TOTAL_DURATION_MS
private const val ITEM_FRACTION = ITEM_DURATION_MS.toFloat() / TOTAL_DURATION_MS

/**
 * Controller driving the staggered bottom-to-top entrance animation for the home screen.
 */
@Stable
class HomeScreenEntranceController(
    val enabled: Boolean,
    private val progressProvider: () -> Float,
    private val defaultOffsetPx: Float,
) {
    val isSettled: Boolean get() = !enabled || progressProvider() >= 1f

    /**
     * Applies a staggered bottom-to-top entrance translation and alpha fade.
     * @param slotIndex 0 represents the lowest element on screen (starts first),
     *                  while (totalSlots - 1) represents the topmost element (starts last).
     */
    fun modifierForSlot(
        slotIndex: Int,
        totalSlots: Int,
        customOffsetPx: Float = defaultOffsetPx,
    ): Modifier {
        if (!enabled) return Modifier

        val safeTotal = totalSlots.coerceAtLeast(1)
        val clampedIndex = slotIndex.coerceIn(0, safeTotal - 1)
        val progressFraction = clampedIndex.toFloat() / (safeTotal - 1).coerceAtLeast(1)

        val startFraction = progressFraction * MAX_DELAY_FRACTION
        val endFraction = (startFraction + ITEM_FRACTION).coerceAtMost(1.0f)

        return Modifier.graphicsLayer {
            val animProgress = progressProvider()
            if (animProgress >= 1f) {
                translationY = 0f
                alpha = 1f
            } else {
                val localFraction = when {
                    animProgress <= startFraction -> 0f
                    animProgress >= endFraction -> 1f
                    else -> ((animProgress - startFraction) / (endFraction - startFraction)).coerceIn(0f, 1f)
                }

                val eased = EasingEmphasizedDecelerate.transform(localFraction)
                translationY = customOffsetPx * (1f - eased)
                alpha = (eased * 1.35f).coerceIn(0f, 1f)
            }
        }
    }
}

/**
 * Creates and remembers a [HomeScreenEntranceController] that automatically triggers
 * the bottom-to-top wave animation when the user unlocks the phone or returns to the home screen.
 */
@Composable
fun rememberHomeScreenEntranceController(
    enabled: Boolean,
): HomeScreenEntranceController {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current
    val defaultOffsetPx = with(density) { 52.dp.toPx() }

    var triggerKey by remember { mutableIntStateOf(0) }
    var wasStopped by remember { mutableStateOf(false) }

    DisposableEffect(context, lifecycleOwner) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    wasStopped = true
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (wasStopped) {
                        wasStopped = false
                        triggerKey++
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_USER_PRESENT) {
                    wasStopped = false
                    triggerKey++
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        context.registerReceiver(receiver, filter)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    val progress = remember { Animatable(if (enabled) 0f else 1f) }

    LaunchedEffect(triggerKey, enabled) {
        if (enabled) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = TOTAL_DURATION_MS, easing = LinearEasing),
            )
        } else {
            progress.snapTo(1f)
        }
    }

    return remember(enabled, defaultOffsetPx) {
        HomeScreenEntranceController(
            enabled = enabled,
            progressProvider = { progress.value },
            defaultOffsetPx = defaultOffsetPx,
        )
    }
}
