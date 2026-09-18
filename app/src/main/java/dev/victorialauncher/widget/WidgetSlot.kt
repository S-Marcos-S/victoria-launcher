// SPDX-License-Identifier: GPL-3.0-or-later
package dev.victorialauncher.widget

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.os.Bundle
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import dev.victorialauncher.R
import dev.victorialauncher.VictoriaApp
import dev.victorialauncher.service.HapticUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

data class WidgetSlotActions(
    val onAddWidget: () -> Unit,
    val onRemoveWidget: (Int) -> Unit = {},
    val onWidgetSettings: (Int) -> Unit = {},
    val onAppInfo: (Int) -> Unit = {},
    val onResize: (Int) -> Unit = {},
    val onOpenSettings: () -> Unit = {},
)

@Composable
fun WidgetSlot(
    widgetIds: List<Int>,
    heightDp: Int,
    hapticsEnabled: Boolean,
    onEditLayout: () -> Unit,
    actions: WidgetSlotActions,
    modifier: Modifier = Modifier,
    editMode: Boolean = false,
    contentColor: Color = Color.White,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val app = context.applicationContext as VictoriaApp
    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val validWidgetIds = remember(widgetIds) { widgetIds.filter { it > 0 } }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { validWidgetIds.size },
    )

    // Automatically scroll to newly added widget if list grows
    var previousCount by remember { mutableIntStateOf(validWidgetIds.size) }
    LaunchedEffect(validWidgetIds.size) {
        if (validWidgetIds.size > previousCount) {
            pagerState.animateScrollToPage(validWidgetIds.size - 1)
        } else if (validWidgetIds.isNotEmpty() && pagerState.currentPage >= validWidgetIds.size) {
            pagerState.scrollToPage((validWidgetIds.size - 1).coerceAtLeast(0))
        }
        previousCount = validWidgetIds.size
    }

    var menuExpanded by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(DpOffset.Zero) }
    var selectedMenuWidgetId by remember { mutableIntStateOf(-1) }

    var slotSizeDp by remember { mutableStateOf(0 to 0) }
    val reportedSizesDp = remember { mutableStateMapOf<Int, Pair<Int, Int>>() }

    var isResizing by remember { mutableStateOf(false) }
    var liveHeightDp by remember(heightDp) { mutableFloatStateOf(heightDp.toFloat()) }
    val effectiveHeightDp = if (isResizing) liveHeightDp.roundToInt() else heightDp

    var isSwiping by remember { mutableStateOf(false) }
    val viewConfig = LocalViewConfiguration.current

    // Dots indicator visibility: appears during scrolling or in editMode, fades out after 2.2s
    var showDots by remember { mutableStateOf(false) }
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress, isSwiping, editMode) {
        if (isSwiping || pagerState.isScrollInProgress || editMode) {
            showDots = true
        } else {
            showDots = true
            delay(2200)
            showDots = false
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(effectiveHeightDp.dp)
                .then(
                    if (validWidgetIds.size > 1) {
                        Modifier.pointerInput(validWidgetIds.size, pagerState) {
                            val touchSlop = viewConfig.touchSlop
                            val pageCount = validWidgetIds.size
                            awaitEachGesture {
                                val down = awaitFirstDown(pass = PointerEventPass.Initial, requireUnconsumed = false)
                                val startPage = pagerState.currentPage
                                var isHorizontalDrag = false
                                var totalDx = 0f
                                var totalDy = 0f
                                val velocityTracker = VelocityTracker()
                                velocityTracker.addPointerInputChange(down)

                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    velocityTracker.addPointerInputChange(change)

                                    if (!change.pressed) {
                                        if (isHorizontalDrag) {
                                            change.consume()
                                            isSwiping = false
                                            val velocityX = velocityTracker.calculateVelocity().x
                                            val currentOffset = (pagerState.currentPage - startPage) + pagerState.currentPageOffsetFraction

                                            val targetPage = when {
                                                velocityX < -600f -> (startPage + 1).coerceAtMost(pageCount - 1)
                                                velocityX > 600f -> (startPage - 1).coerceAtLeast(0)
                                                else -> {
                                                    val sign = if (currentOffset >= 0f) 1 else -1
                                                    val absOffset = abs(currentOffset)
                                                    val wholePages = absOffset.toInt()
                                                    val frac = absOffset - wholePages
                                                    val extra = if (frac >= 0.25f) 1 else 0
                                                    (startPage + sign * (wholePages + extra)).coerceIn(0, pageCount - 1)
                                                }
                                            }
                                            scope.launch {
                                                pagerState.animateScrollToPage(targetPage)
                                            }
                                        }
                                        break
                                    }

                                    val dx = change.position.x - change.previousPosition.x
                                    val dy = change.position.y - change.previousPosition.y
                                    totalDx += dx
                                    totalDy += dy

                                    if (!isHorizontalDrag) {
                                        if (abs(totalDx) > touchSlop && abs(totalDx) > abs(totalDy) * 1.15f) {
                                            isHorizontalDrag = true
                                            isSwiping = true
                                        } else if (abs(totalDy) > touchSlop && abs(totalDy) > abs(totalDx) * 1.15f) {
                                            break
                                        }
                                    }

                                    if (isHorizontalDrag) {
                                        change.consume()
                                        pagerState.dispatchRawDelta(-dx)
                                    }
                                }
                            }
                        }
                    } else {
                        Modifier
                    }
                ),
        ) {
            if (validWidgetIds.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    pageSpacing = 12.dp,
                    key = { index -> validWidgetIds.getOrNull(index) ?: index },
                ) { pageIndex ->
                    val currentId = validWidgetIds.getOrNull(pageIndex) ?: return@HorizontalPager
                    val providerInfo: AppWidgetProviderInfo? = remember(currentId) {
                        appWidgetManager.getAppWidgetInfo(currentId)
                    }

                    // Smooth transition animation when swiping between widgets
                    val pageOffset = abs(
                        (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                    ).coerceIn(0f, 1f)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val scale = 1f - 0.05f * pageOffset
                                scaleX = scale
                                scaleY = scale
                                alpha = 1f - 0.35f * pageOffset
                            },
                    ) {
                        if (providerInfo != null) {
                            AndroidView(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .onSizeChanged { size ->
                                        slotSizeDp = with(density) { size.width.toDp().value.toInt() to size.height.toDp().value.toInt() }
                                    },
                                factory = { ctx ->
                                    val hostView = app.widgetHost.createView(ctx, currentId, providerInfo).apply {
                                        setAppWidget(currentId, providerInfo)
                                        setPadding(0, 0, 0, 0)
                                    }
                                    LongPressFrameLayout(ctx).apply {
                                        clipChildren = false
                                        clipToPadding = false
                                        addView(
                                            hostView,
                                            android.widget.FrameLayout.LayoutParams(
                                                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                                                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                                            ),
                                        )
                                        onLongPress = { x, y ->
                                            HapticUtil.tick(this, hapticsEnabled)
                                            selectedMenuWidgetId = currentId
                                            menuOffset = with(density) { DpOffset(x.toDp(), y.toDp()) }
                                            menuExpanded = true
                                        }
                                    }
                                },
                                update = { container ->
                                    val hostView = container.getChildAt(0) as? AppWidgetHostView
                                    hostView?.setPadding(0, 0, 0, 0)
                                    hostView?.setAppWidget(currentId, providerInfo)
                                    val (wDp, hDp) = slotSizeDp
                                    if (hostView != null && wDp > 0 && hDp > 0 && reportedSizesDp[currentId] != slotSizeDp) {
                                        reportedSizesDp[currentId] = slotSizeDp
                                        val options = runCatching { appWidgetManager.getAppWidgetOptions(currentId) }
                                            .getOrNull() ?: Bundle()
                                        hostView.updateAppWidgetSize(options, wDp, hDp, wDp, hDp)
                                    }
                                    container.onLongPress = { x, y ->
                                        HapticUtil.tick(container, hapticsEnabled)
                                        selectedMenuWidgetId = currentId
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
                                                selectedMenuWidgetId = currentId
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
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { actions.onAddWidget() },
                                onLongPress = { offset ->
                                    HapticUtil.tick(view, hapticsEnabled)
                                    selectedMenuWidgetId = -1
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
                if (selectedMenuWidgetId > 0) {
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
                        onClick = { menuExpanded = false; actions.onAppInfo(selectedMenuWidgetId) },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_edit_layout)) },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = { menuExpanded = false; onEditLayout() },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.widget_change_settings)) },
                        leadingIcon = { Icon(Icons.Filled.Tune, contentDescription = null) },
                        onClick = { menuExpanded = false; actions.onWidgetSettings(selectedMenuWidgetId) },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.widget_add_custom)) },
                        leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
                        onClick = { menuExpanded = false; actions.onAddWidget() },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_remove)) },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                        onClick = { menuExpanded = false; actions.onRemoveWidget(selectedMenuWidgetId) },
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

        // Small indicator dots showing placed widgets count and active page
        if (validWidgetIds.size > 1) {
            Spacer(Modifier.height(6.dp))
            WidgetDotsIndicator(
                pageCount = validWidgetIds.size,
                pagerState = pagerState,
                contentColor = contentColor,
                visible = showDots || editMode,
                onSelectPage = { page ->
                    scope.launch { pagerState.animateScrollToPage(page) }
                },
            )
        }
    }
}

/** Legacy overload maintaining compatibility with single widgetId callers. */
@Composable
fun WidgetSlot(
    widgetId: Int,
    heightDp: Int,
    hapticsEnabled: Boolean,
    onEditLayout: () -> Unit,
    actions: WidgetSlotActions,
    modifier: Modifier = Modifier,
    editMode: Boolean = false,
    contentColor: Color = Color.White,
) {
    val ids = if (widgetId > 0) listOf(widgetId) else emptyList()
    WidgetSlot(
        widgetIds = ids,
        heightDp = heightDp,
        hapticsEnabled = hapticsEnabled,
        onEditLayout = onEditLayout,
        actions = actions,
        modifier = modifier,
        editMode = editMode,
        contentColor = contentColor,
    )
}

@Composable
private fun WidgetDotsIndicator(
    pageCount: Int,
    pagerState: PagerState,
    contentColor: Color,
    visible: Boolean,
    modifier: Modifier = Modifier,
    onSelectPage: (Int) -> Unit,
) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(350),
        label = "widgetDotsAlpha",
    )

    if (alpha > 0.01f && pageCount > 1) {
        Row(
            modifier = modifier
                .graphicsLayer { this.alpha = alpha }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val currentProgress = pagerState.currentPage + pagerState.currentPageOffsetFraction
            val activeColor = contentColor.copy(alpha = 0.95f)
            val inactiveColor = contentColor.copy(alpha = 0.28f)

            for (i in 0 until pageCount) {
                val distance = abs(currentProgress - i).coerceIn(0f, 1f)
                val activeFraction = 1f - distance

                val dotWidth = androidx.compose.ui.unit.lerp(6.dp, 16.dp, activeFraction)
                val dotHeight = 6.dp
                val dotColor = androidx.compose.ui.graphics.lerp(inactiveColor, activeColor, activeFraction)

                Box(
                    modifier = Modifier
                        .size(width = dotWidth, height = dotHeight)
                        .clip(RoundedCornerShape(3.dp))
                        .background(dotColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectPage(i) },
                        ),
                )
            }
        }
    }
}