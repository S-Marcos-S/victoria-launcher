// SPDX-License-Identifier: GPL-3.0-or-later
package dev.victorialauncher.widget

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.os.Bundle
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import dev.victorialauncher.VictoriaApp
import dev.victorialauncher.R
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.victorialauncher.service.HapticUtil
import kotlin.math.roundToInt

data class WidgetSlotActions(
    val onAddWidget: () -> Unit,
    val onRemoveWidget: () -> Unit,
    val onWidgetSettings: () -> Unit,
    val onAppInfo: () -> Unit,
    val onResize: (Int) -> Unit,
    val onOpenSettings: () -> Unit,
)

@Composable
fun WidgetSlot(
    widgetId: Int,
    heightDp: Int,
    hapticsEnabled: Boolean,
    onEditLayout: () -> Unit,
    actions: WidgetSlotActions,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val app = context.applicationContext as VictoriaApp
    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }
    val providerInfo: AppWidgetProviderInfo? = remember(widgetId) {
        if (widgetId > 0) appWidgetManager.getAppWidgetInfo(widgetId) else null
    }

    var menuExpanded by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(DpOffset.Zero) }
    val density = LocalDensity.current
    var slotSizeDp by remember { mutableStateOf(0 to 0) }
    var reportedSizeDp by remember(widgetId) { mutableStateOf(0 to 0) }

    var isResizing by remember { mutableStateOf(false) }
    var liveHeightDp by remember(heightDp) { mutableFloatStateOf(heightDp.toFloat()) }
    val effectiveHeightDp = if (isResizing) liveHeightDp.roundToInt() else heightDp

    Box(modifier = modifier.height(effectiveHeightDp.dp)) {
        if (widgetId > 0 && providerInfo != null) {
            // A real long-press (finger held still) opens the edit menu or directly triggers resize
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { size ->
                        slotSizeDp = with(density) { size.width.toDp().value.toInt() to size.height.toDp().value.toInt() }
                    },
                factory = { ctx ->
                    val hostView = app.widgetHost.createView(ctx, widgetId, providerInfo).apply {
                        setAppWidget(widgetId, providerInfo)
                        setPadding(0, 0, 0, 0)
                    }
                    LongPressFrameLayout(ctx).apply {
                        clipChildren = false
                        clipToPadding = false
                        addView(
                            hostView,
                            android.widget.FrameLayout.LayoutParams(
                                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                            )
                        )
                        onLongPress = { x, y ->
                            HapticUtil.tick(this, hapticsEnabled)
                            menuOffset = with(density) { DpOffset(x.toDp(), y.toDp()) }
                            menuExpanded = true
                        }
                    }
                },
                update = { container ->
                    val hostView = container.getChildAt(0) as? AppWidgetHostView
                    hostView?.setPadding(0, 0, 0, 0)
                    hostView?.setAppWidget(widgetId, providerInfo)
                    val (wDp, hDp) = slotSizeDp
                    if (hostView != null && wDp > 0 && hDp > 0 && slotSizeDp != reportedSizeDp) {
                        reportedSizeDp = slotSizeDp
                        val options = runCatching { appWidgetManager.getAppWidgetOptions(widgetId) }
                            .getOrNull() ?: Bundle()
                        hostView.updateAppWidgetSize(options, wDp, hDp, wDp, hDp)
                    }
                    container.onLongPress = { x, y ->
                        HapticUtil.tick(container, hapticsEnabled)
                        menuOffset = with(density) { DpOffset(x.toDp(), y.toDp()) }
                        menuExpanded = true
                    }
                },
            )
        } else {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { actions.onAddWidget() },
                            onLongPress = { offset ->
                                HapticUtil.tick(view, hapticsEnabled)
                                menuOffset = with(density) { DpOffset(offset.x.toDp(), offset.y.toDp()) }
                                menuExpanded = true
                            },
                        )
                    },
                color = Color.Black.copy(alpha = 0.25f),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.widget_add), tint = Color.White)
                    Text(stringResource(R.string.widget_add), color = Color.White.copy(alpha = 0.8f))
                }
            }
        }

        // Resizing visual overlay with handles
        if (isResizing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.20f), RoundedCornerShape(16.dp)),
            ) {
                // Top drag handle
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                        .size(width = 44.dp, height = 12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.8f))
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta ->
                                val deltaDp = with(density) { delta.toDp().value }
                                val newH = (liveHeightDp - deltaDp).coerceIn(70f, 800f)
                                if (newH.roundToInt() != liveHeightDp.roundToInt()) {
                                    HapticUtil.tick(view, hapticsEnabled)
                                }
                                liveHeightDp = newH
                            },
                            onDragStopped = { actions.onResize(liveHeightDp.roundToInt()) },
                        ),
                )

                // Center indicator badge with current height and done button
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${liveHeightDp.roundToInt()} dp",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            actions.onResize(liveHeightDp.roundToInt())
                            isResizing = false
                        },
                        modifier = Modifier.size(24.dp),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.action_done), modifier = Modifier.size(18.dp))
                    }
                }

                // Bottom drag handle
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                        .size(width = 44.dp, height = 12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.8f))
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta ->
                                val deltaDp = with(density) { delta.toDp().value }
                                val newH = (liveHeightDp + deltaDp).coerceIn(70f, 800f)
                                if (newH.roundToInt() != liveHeightDp.roundToInt()) {
                                    HapticUtil.tick(view, hapticsEnabled)
                                }
                                liveHeightDp = newH
                            },
                            onDragStopped = { actions.onResize(liveHeightDp.roundToInt()) },
                        ),
                )
            }
        }

        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, offset = menuOffset) {
            if (widgetId > 0) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_resize)) },
                    leadingIcon = { Icon(Icons.Filled.OpenInFull, contentDescription = null) },
                    onClick = {
                        menuExpanded = false
                        liveHeightDp = heightDp.toFloat()
                        isResizing = true
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_app_info)) },
                    leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) },
                    onClick = { menuExpanded = false; actions.onAppInfo() },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_edit_layout)) },
                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                    onClick = { menuExpanded = false; onEditLayout() },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.widget_change_settings)) },
                    leadingIcon = { Icon(Icons.Filled.Tune, contentDescription = null) },
                    onClick = { menuExpanded = false; actions.onWidgetSettings() },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.widget_add_custom)) },
                    leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    onClick = { menuExpanded = false; actions.onAddWidget() },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_remove)) },
                    leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                    onClick = { menuExpanded = false; actions.onRemoveWidget() },
                )
            } else {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.widget_add_custom)) },
                    leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    onClick = { menuExpanded = false; actions.onAddWidget() },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_edit_layout)) },
                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                    onClick = { menuExpanded = false; onEditLayout() },
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_open_settings)) },
                leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                onClick = { menuExpanded = false; actions.onOpenSettings() },
            )
        }

    }
}