// SPDX-License-Identifier: GPL-3.0-or-later
package dev.victorialauncher.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.provider.AlarmClock
import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import dev.victorialauncher.data.ClockStyle
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

/** Distance from the top of the screen: ~2.5 cm (25 mm = ~158 dp at 160 dpi). */
val CLOCK_TOP_PADDING_DP = 158.dp

/**
 * Niagara-style clock and date widget with multiple style options.
 * Sits on the home screen above favorite apps.
 * - Tapping the time launches the system Clock/Alarm app.
 * - Tapping the date launches the system Calendar app.
 */
@Composable
fun NiagaraClockWidget(
    clockStyle: ClockStyle = ClockStyle.CLASSIC,
    contentColor: Color,
    sidePaddingDp: Int,
    alignRight: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(Date()) }

    // Listen for system time broadcast ticks to update the clock efficiently without polling loops
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                currentTime = Date()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        context.registerReceiver(receiver, filter)
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {
                // Receiver might not have been registered or already unregistered
            }
        }
    }

    val is24Hour = DateFormat.is24HourFormat(context)
    val timePattern = if (is24Hour) "HH:mm" else "h:mm"
    val timeString = remember(currentTime, is24Hour) {
        SimpleDateFormat(timePattern, Locale.getDefault()).format(currentTime)
    }

    val hoursPattern = if (is24Hour) "HH" else "h"
    val hoursString = remember(currentTime, is24Hour) {
        SimpleDateFormat(hoursPattern, Locale.getDefault()).format(currentTime)
    }
    val minutesString = remember(currentTime) {
        SimpleDateFormat("mm", Locale.getDefault()).format(currentTime)
    }

    val datePattern = if (Locale.getDefault().language == "pt") {
        "EEEE, d 'de' MMMM"
    } else {
        "EEEE, MMMM d"
    }
    val rawDateString = remember(currentTime) {
        SimpleDateFormat(datePattern, Locale.getDefault()).format(currentTime)
    }
    val dateString = remember(rawDateString) {
        rawDateString.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    val weekdayShortPattern = if (Locale.getDefault().language == "pt") "EEEE, d" else "EEEE, MMM d"
    val dayFocusHeader = remember(currentTime) {
        SimpleDateFormat(weekdayShortPattern, Locale.getDefault()).format(currentTime).uppercase()
    }

    val horizontalAlignment = if (alignRight) Alignment.End else Alignment.Start

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = sidePaddingDp.dp),
        horizontalAlignment = horizontalAlignment,
    ) {
        when (clockStyle) {
            ClockStyle.CLASSIC -> {
                ClassicClockContent(
                    timeString = timeString,
                    dateString = dateString,
                    contentColor = contentColor,
                    horizontalAlignment = horizontalAlignment,
                    onClockClick = { launchClockApp(context) },
                    onDateClick = { launchCalendarApp(context) },
                )
            }
            ClockStyle.STACKED -> {
                StackedClockContent(
                    hoursString = hoursString,
                    minutesString = minutesString,
                    dateString = dateString,
                    contentColor = contentColor,
                    horizontalAlignment = horizontalAlignment,
                    onClockClick = { launchClockApp(context) },
                    onDateClick = { launchCalendarApp(context) },
                )
            }
            ClockStyle.MINIMAL -> {
                MinimalClockContent(
                    timeString = timeString,
                    dateString = dateString,
                    contentColor = contentColor,
                    horizontalAlignment = horizontalAlignment,
                    onClockClick = { launchClockApp(context) },
                    onDateClick = { launchCalendarApp(context) },
                )
            }
            ClockStyle.ANALOG -> {
                AnalogClockContent(
                    currentTime = currentTime,
                    timeString = timeString,
                    dateString = dateString,
                    contentColor = contentColor,
                    horizontalAlignment = horizontalAlignment,
                    onClockClick = { launchClockApp(context) },
                    onDateClick = { launchCalendarApp(context) },
                )
            }
            ClockStyle.DIGITAL_CARD -> {
                DigitalCardClockContent(
                    timeString = timeString,
                    dateString = dateString,
                    contentColor = contentColor,
                    horizontalAlignment = horizontalAlignment,
                    onClockClick = { launchClockApp(context) },
                    onDateClick = { launchCalendarApp(context) },
                )
            }
            ClockStyle.DAY_FOCUS -> {
                DayFocusClockContent(
                    headerString = dayFocusHeader,
                    timeString = timeString,
                    dateString = dateString,
                    contentColor = contentColor,
                    horizontalAlignment = horizontalAlignment,
                    onClockClick = { launchClockApp(context) },
                    onDateClick = { launchCalendarApp(context) },
                )
            }
        }
    }
}

@Composable
private fun ClassicClockContent(
    timeString: String,
    dateString: String,
    contentColor: Color,
    horizontalAlignment: Alignment.Horizontal,
    onClockClick: () -> Unit,
    onDateClick: () -> Unit,
) {
    Column(horizontalAlignment = horizontalAlignment) {
        Text(
            text = timeString,
            color = contentColor,
            fontSize = 58.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-1.5).sp,
            lineHeight = 58.sp,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClockClick,
            ),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = dateString,
            color = contentColor.copy(alpha = 0.85f),
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 22.sp,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDateClick,
            ),
        )
    }
}

@Composable
private fun StackedClockContent(
    hoursString: String,
    minutesString: String,
    dateString: String,
    contentColor: Color,
    horizontalAlignment: Alignment.Horizontal,
    onClockClick: () -> Unit,
    onDateClick: () -> Unit,
) {
    Column(horizontalAlignment = horizontalAlignment) {
        Column(
            horizontalAlignment = horizontalAlignment,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClockClick,
            ),
        ) {
            Text(
                text = hoursString,
                color = contentColor,
                fontSize = 50.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 46.sp,
            )
            Text(
                text = minutesString,
                color = contentColor.copy(alpha = 0.9f),
                fontSize = 50.sp,
                fontWeight = FontWeight.Light,
                lineHeight = 46.sp,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = dateString,
            color = contentColor.copy(alpha = 0.85f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDateClick,
            ),
        )
    }
}

@Composable
private fun MinimalClockContent(
    timeString: String,
    dateString: String,
    contentColor: Color,
    horizontalAlignment: Alignment.Horizontal,
    onClockClick: () -> Unit,
    onDateClick: () -> Unit,
) {
    Column(horizontalAlignment = horizontalAlignment) {
        Text(
            text = timeString,
            color = contentColor,
            fontSize = 46.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 1.sp,
            lineHeight = 46.sp,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClockClick,
            ),
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = dateString,
            color = contentColor.copy(alpha = 0.75f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDateClick,
            ),
        )
    }
}

@Composable
private fun AnalogClockContent(
    currentTime: Date,
    timeString: String,
    dateString: String,
    contentColor: Color,
    horizontalAlignment: Alignment.Horizontal,
    onClockClick: () -> Unit,
    onDateClick: () -> Unit,
) {
    Column(horizontalAlignment = horizontalAlignment) {
        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClockClick,
                )
                .padding(vertical = 4.dp),
        ) {
            AnalogDial(
                time = currentTime,
                tint = contentColor,
                sizeDp = 84,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDateClick,
            ),
        ) {
            Text(
                text = timeString,
                color = contentColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "  ·  $dateString",
                color = contentColor.copy(alpha = 0.8f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun DigitalCardClockContent(
    timeString: String,
    dateString: String,
    contentColor: Color,
    horizontalAlignment: Alignment.Horizontal,
    onClockClick: () -> Unit,
    onDateClick: () -> Unit,
) {
    Column(horizontalAlignment = horizontalAlignment) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(contentColor.copy(alpha = 0.08f))
                .border(1.dp, contentColor.copy(alpha = 0.16f), RoundedCornerShape(18.dp))
                .padding(horizontal = 18.dp, vertical = 12.dp),
        ) {
            Column(horizontalAlignment = horizontalAlignment) {
                Text(
                    text = timeString,
                    color = contentColor,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    lineHeight = 42.sp,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClockClick,
                    ),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateString,
                    color = contentColor.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDateClick,
                    ),
                )
            }
        }
    }
}

@Composable
private fun DayFocusClockContent(
    headerString: String,
    timeString: String,
    dateString: String,
    contentColor: Color,
    horizontalAlignment: Alignment.Horizontal,
    onClockClick: () -> Unit,
    onDateClick: () -> Unit,
) {
    Column(horizontalAlignment = horizontalAlignment) {
        Text(
            text = headerString,
            color = contentColor.copy(alpha = 0.7f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.8.sp,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDateClick,
            ),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = timeString,
            color = contentColor,
            fontSize = 54.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-1).sp,
            lineHeight = 54.sp,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClockClick,
            ),
        )
    }
}

@Composable
fun AnalogDial(
    time: Date,
    tint: Color,
    sizeDp: Int = 84,
    modifier: Modifier = Modifier,
) {
    val cal = remember(time) {
        Calendar.getInstance().apply { this.time = time }
    }
    val hours = cal.get(Calendar.HOUR)
    val minutes = cal.get(Calendar.MINUTE)
    val seconds = cal.get(Calendar.SECOND)

    Canvas(modifier = modifier.size(sizeDp.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f

        // Draw subtle outer dial circle
        drawCircle(
            color = tint.copy(alpha = 0.2f),
            radius = radius * 0.95f,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()),
        )

        // Draw hour markers at 12, 3, 6, 9
        for (i in 0 until 12) {
            val angle = Math.toRadians((i * 30.0) - 90.0)
            val isMajor = i % 3 == 0
            val markerLength = if (isMajor) radius * 0.16f else radius * 0.08f
            val startDist = radius * 0.92f - markerLength
            val endDist = radius * 0.92f

            val startX = center.x + (startDist * cos(angle)).toFloat()
            val startY = center.y + (startDist * sin(angle)).toFloat()
            val endX = center.x + (endDist * cos(angle)).toFloat()
            val endY = center.y + (endDist * sin(angle)).toFloat()

            drawLine(
                color = if (isMajor) tint.copy(alpha = 0.7f) else tint.copy(alpha = 0.3f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = (if (isMajor) 2.dp else 1.2.dp).toPx(),
                cap = StrokeCap.Round,
            )
        }

        // Hour Hand
        val hourAngle = Math.toRadians(((hours + minutes / 60f) * 30.0) - 90.0)
        val hourLength = radius * 0.52f
        val hourEnd = Offset(
            x = center.x + (hourLength * cos(hourAngle)).toFloat(),
            y = center.y + (hourLength * sin(hourAngle)).toFloat(),
        )
        drawLine(
            color = tint,
            start = center,
            end = hourEnd,
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round,
        )

        // Minute Hand
        val minuteAngle = Math.toRadians(((minutes + seconds / 60f) * 6.0) - 90.0)
        val minuteLength = radius * 0.75f
        val minuteEnd = Offset(
            x = center.x + (minuteLength * cos(minuteAngle)).toFloat(),
            y = center.y + (minuteLength * sin(minuteAngle)).toFloat(),
        )
        drawLine(
            color = tint.copy(alpha = 0.9f),
            start = center,
            end = minuteEnd,
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )

        // Center Pivot
        drawCircle(
            color = tint,
            radius = 3.5.dp.toPx(),
        )
    }
}

/**
 * Scaled mini preview of a clock style used in the 2-column grid picker.
 */
@Composable
fun ClockStylePreview(
    style: ClockStyle,
    currentTime: Date,
    is24Hour: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val timePattern = if (is24Hour) "HH:mm" else "h:mm"
    val timeString = remember(currentTime, is24Hour) {
        SimpleDateFormat(timePattern, Locale.getDefault()).format(currentTime)
    }
    val hoursPattern = if (is24Hour) "HH" else "h"
    val hoursString = remember(currentTime, is24Hour) {
        SimpleDateFormat(hoursPattern, Locale.getDefault()).format(currentTime)
    }
    val minutesString = remember(currentTime) {
        SimpleDateFormat("mm", Locale.getDefault()).format(currentTime)
    }
    val weekdayPattern = if (Locale.getDefault().language == "pt") "EEE, d MMM" else "EEE, MMM d"
    val shortDateString = remember(currentTime) {
        SimpleDateFormat(weekdayPattern, Locale.getDefault()).format(currentTime)
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        when (style) {
            ClockStyle.CLASSIC -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeString,
                        color = tint,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp,
                    )
                    Text(
                        text = shortDateString,
                        color = tint.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                    )
                }
            }
            ClockStyle.STACKED -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = hoursString,
                        color = tint,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 20.sp,
                    )
                    Text(
                        text = minutesString,
                        color = tint.copy(alpha = 0.85f),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Light,
                        lineHeight = 20.sp,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = shortDateString,
                        color = tint.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                    )
                }
            }
            ClockStyle.MINIMAL -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeString,
                        color = tint,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.sp,
                    )
                    Text(
                        text = shortDateString,
                        color = tint.copy(alpha = 0.65f),
                        fontSize = 10.sp,
                    )
                }
            }
            ClockStyle.ANALOG -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnalogDial(
                        time = currentTime,
                        tint = tint,
                        sizeDp = 48,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = timeString,
                        color = tint.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            ClockStyle.DIGITAL_CARD -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(tint.copy(alpha = 0.08f))
                        .border(1.dp, tint.copy(alpha = 0.16f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = timeString,
                            color = tint,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = shortDateString,
                            color = tint.copy(alpha = 0.75f),
                            fontSize = 10.sp,
                        )
                    }
                }
            }
            ClockStyle.DAY_FOCUS -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = shortDateString.uppercase(),
                        color = tint.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                    Text(
                        text = timeString,
                        color = tint,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

/** Attempts to launch the device clock or alarms activity. */
private fun launchClockApp(context: Context) {
    val candidates = listOf(
        Intent(AlarmClock.ACTION_SHOW_ALARMS),
        Intent(AlarmClock.ACTION_SET_ALARM),
    )
    for (intent in candidates) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        } catch (_: Exception) {}
    }
    val clockPkgs = listOf(
        "com.google.android.deskclock",
        "com.android.deskclock",
        "com.sec.android.app.clockpackage",
    )
    for (pkg in clockPkgs) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(launchIntent)
                return
            } catch (_: Exception) {}
        }
    }
}

/** Attempts to launch the device calendar activity. */
private fun launchCalendarApp(context: Context) {
    val candidates = listOf(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR),
        Intent(Intent.ACTION_VIEW).setData(Uri.parse("content://com.android.calendar/time")),
    )
    for (intent in candidates) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        } catch (_: Exception) {}
    }
}
