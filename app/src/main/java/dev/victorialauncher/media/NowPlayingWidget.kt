// SPDX-License-Identifier: GPL-3.0-or-later
package dev.victorialauncher.media

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import dev.victorialauncher.R
import androidx.compose.ui.res.stringResource

fun isListenerEnabled(context: Context) =
    NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

/**
 * Brings up whatever is playing, and reports whether anything could be.
 *
 * The launcher intent goes first even though a session may nominate its own activity, which
 * would land on the player's own screen. Sending that PendingIntent starts an activity on
 * behalf of an app that is in the background, which Android drops on sight from 12 onwards
 * unless the sender explicitly grants the privilege — and `send()` reports success either
 * way, so there is no way to notice it went nowhere and fall back. Starting the intent
 * ourselves has no such problem: we are the foreground app. In practice the launcher intent
 * resumes the player's existing task anyway, which is the same screen.
 */
fun openNowPlayingApp(context: Context): Boolean {
    val controller = NowPlayingBus.state.value?.controller ?: return false

    context.packageManager.getLaunchIntentForPackage(controller.packageName)?.let { launch ->
        if (runCatching {
                context.startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }.isSuccess
        ) {
            return true
        }
    }

    // Nothing launchable — a background service publishing a session, say. The session's own
    // activity is all that is left, so it is worth a try even knowing it may be dropped.
    val sessionActivity = controller.sessionActivity ?: return false
    return runCatching { sessionActivity.send() }.isSuccess
}

@Composable
fun NowPlayingWidget(
    heightDp: Int = 64,
    iconSizeDp: Int = 48,
    labelSizeSp: Int = 14,
    contentColor: Color = Color.White,
    alignRight: Boolean = false,
    editMode: Boolean = false,
    onDismissPermissionPrompt: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var listenerEnabled by remember { mutableStateOf(isListenerEnabled(context)) }

    // The permission is granted in a separate system Settings screen, so re-check whenever
    // this screen comes back into the foreground instead of only once at first composition.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                listenerEnabled = isListenerEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (!listenerEnabled && !editMode) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = heightDp.dp),
            color = contentColor.copy(alpha = 0.08f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, contentColor.copy(alpha = 0.12f)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (alignRight) {
                    Button(
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.now_playing_prompt_grant),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    if (onDismissPermissionPrompt != null) {
                        Spacer(Modifier.width(4.dp))
                        TextButton(
                            onClick = onDismissPermissionPrompt,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.now_playing_prompt_deny),
                                color = contentColor.copy(alpha = 0.75f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.now_playing_prompt_title),
                        color = contentColor,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(contentColor.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(contentColor.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.now_playing_prompt_title),
                        color = contentColor,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(6.dp))
                    if (onDismissPermissionPrompt != null) {
                        TextButton(
                            onClick = onDismissPermissionPrompt,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.now_playing_prompt_deny),
                                color = contentColor.copy(alpha = 0.75f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                    Button(
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.now_playing_prompt_grant),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
        return
    }

    // Null unless something is actually playing, paused or buffering — see the listener
    // service. In edit mode, show a placeholder if nothing is currently playing.
    val nowPlaying by NowPlayingBus.state.collectAsState()
    val current = nowPlaying

    val isEditModePlaceholder = current == null || (!listenerEnabled && editMode)

    val scope = rememberCoroutineScope()
    val dismissX = remember(current?.controller?.sessionToken) { Animatable(0f) }
    val dismissThresholdPx = with(LocalDensity.current) { 120.dp.toPx() }

    // Artwork size matches the app icon size, title text matches the app label text size
    val artSize = iconSizeDp.dp
    val titleSp = labelSizeSp.sp
    val artistSp = (labelSizeSp - 2).coerceAtLeast(10).sp
    val controlSize = (heightDp * 0.42f).coerceIn(18f, 64f).dp

    if (isEditModePlaceholder) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(heightDp.dp),
            color = contentColor.copy(alpha = 0.08f),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (alignRight) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy((heightDp * 0.06f).coerceIn(4f, 16f).dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TransportButton(Icons.Filled.SkipPrevious, stringResource(R.string.now_playing_previous), controlSize, contentColor) {}
                            TransportButton(Icons.Filled.PlayArrow, stringResource(R.string.now_playing_play_pause), controlSize, contentColor) {}
                            TransportButton(Icons.Filled.SkipNext, stringResource(R.string.now_playing_next), controlSize, contentColor) {}
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End,
                        ) {
                            Text(
                                stringResource(R.string.settings_section_now_playing),
                                color = contentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = titleSp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                            )
                            Text(
                                stringResource(R.string.settings_now_playing_show),
                                color = contentColor.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = artistSp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Box(
                            modifier = Modifier.size(artSize),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = contentColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(artSize * 0.75f),
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier.size(artSize),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = contentColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(artSize * 0.75f),
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.settings_section_now_playing),
                                color = contentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = titleSp,
                            )
                            Text(
                                stringResource(R.string.settings_now_playing_show),
                                color = contentColor.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = artistSp,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy((heightDp * 0.06f).coerceIn(4f, 16f).dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TransportButton(Icons.Filled.SkipPrevious, stringResource(R.string.now_playing_previous), controlSize, contentColor) {}
                            TransportButton(Icons.Filled.PlayArrow, stringResource(R.string.now_playing_play_pause), controlSize, contentColor) {}
                            TransportButton(Icons.Filled.SkipNext, stringResource(R.string.now_playing_next), controlSize, contentColor) {}
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { 0.45f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = contentColor.copy(alpha = 0.85f),
                    trackColor = contentColor.copy(alpha = 0.15f),
                )
            }
        }
        return
    }

    var currentPosition by remember(current.controller.sessionToken, current.positionMs, current.lastUpdateTime) {
        mutableStateOf(current.positionMs)
    }

    // Advance position locally while playing
    LaunchedEffect(current.isPlaying, current.positionMs, current.lastUpdateTime, current.speed) {
        if (!current.isPlaying || current.durationMs <= 0L) return@LaunchedEffect
        while (true) {
            val elapsed = System.currentTimeMillis() - current.lastUpdateTime
            val calculated = current.positionMs + (elapsed * current.speed).toLong()
            currentPosition = calculated.coerceIn(0L, current.durationMs)
            kotlinx.coroutines.delay(500L)
        }
    }

    val progressFraction = if (current.durationMs > 0L) {
        (currentPosition.toFloat() / current.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .offset { IntOffset(dismissX.value.roundToInt(), 0) }
            .graphicsLayer { alpha = (1f - (dismissX.value / (dismissThresholdPx * 2.5f))).coerceIn(0f, 1f) }
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    scope.launch { dismissX.snapTo((dismissX.value + delta).coerceAtLeast(0f)) }
                },
                onDragStopped = { velocity ->
                    if (dismissX.value > dismissThresholdPx || velocity > 1200f) {
                        dismissX.animateTo(dismissThresholdPx * 6f, tween(180))
                        runCatching { current.controller.transportControls.stop() }
                        NowPlayingBus.update(null)
                    } else {
                        dismissX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    }
                },
            ),
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 6.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            val artBox: @Composable () -> Unit = {
                Box(
                    modifier = Modifier.size(artSize),
                    contentAlignment = Alignment.Center,
                ) {
                    val art = current.art
                    if (art != null) {
                        Image(
                            bitmap = art.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                        )
                    } else {
                        Icon(
                            Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = contentColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(artSize * 0.75f),
                        )
                    }
                }
            }

            val textColumn: @Composable (Modifier) -> Unit = { mod ->
                Column(
                    modifier = mod,
                    horizontalAlignment = if (alignRight) Alignment.End else Alignment.Start,
                ) {
                    Text(
                        current.title.ifBlank { stringResource(R.string.now_playing_unknown_title) },
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = titleSp,
                        textAlign = if (alignRight) androidx.compose.ui.text.style.TextAlign.End else androidx.compose.ui.text.style.TextAlign.Start,
                    )
                    Text(
                        current.artist,
                        color = contentColor.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = artistSp,
                        textAlign = if (alignRight) androidx.compose.ui.text.style.TextAlign.End else androidx.compose.ui.text.style.TextAlign.Start,
                    )
                }
            }

            val controlsRow: @Composable () -> Unit = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((heightDp * 0.06f).coerceIn(4f, 16f).dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TransportButton(
                        Icons.Filled.SkipPrevious,
                        stringResource(R.string.now_playing_previous),
                        controlSize,
                        contentColor,
                    ) {
                        current.controller.transportControls.skipToPrevious()
                    }
                    TransportButton(
                        if (current.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        stringResource(R.string.now_playing_play_pause),
                        controlSize,
                        contentColor,
                    ) {
                        if (current.isPlaying) {
                            current.controller.transportControls.pause()
                        } else {
                            current.controller.transportControls.play()
                        }
                    }
                    TransportButton(
                        Icons.Filled.SkipNext,
                        stringResource(R.string.now_playing_next),
                        controlSize,
                        contentColor,
                    ) {
                        current.controller.transportControls.skipToNext()
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (alignRight) {
                    controlsRow()
                    Spacer(Modifier.width(8.dp))
                    textColumn(Modifier.weight(1f))
                    Spacer(Modifier.width(16.dp))
                    artBox()
                } else {
                    artBox()
                    Spacer(Modifier.width(16.dp))
                    textColumn(Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    controlsRow()
                }
            }
            if (current.durationMs > 0L) {
                Spacer(Modifier.height(6.dp))
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = contentColor.copy(alpha = 0.85f),
                    trackColor = contentColor.copy(alpha = 0.15f),
                )
            }
        }
    }
}

@Composable
private fun RowScope.TransportButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    size: Dp,
    tint: Color,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, modifier = Modifier.size(size)) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(size * 0.7f))
    }
}