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

/** Distance from the top of the screen: ~2.5 cm (25 mm = ~158 dp at 160 dpi). */
val CLOCK_TOP_PADDING_DP = 158.dp

/**
 * Niagara-style clock and date widget.
 * Sits on the home screen above favorite apps.
 * - Tapping the time launches the system Clock/Alarm app.
 * - Tapping the date launches the system Calendar app.
 */
@Composable
fun NiagaraClockWidget(
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

    val datePattern = if (Locale.getDefault().language == "pt") {
        "EEEE, d 'de' MMMM"
    } else {
        "EEEE, MMMM d"
    }
    val rawDateString = remember(currentTime) {
        SimpleDateFormat(datePattern, Locale.getDefault()).format(currentTime)
    }
    // Capitalize the first letter of the weekday
    val dateString = remember(rawDateString) {
        rawDateString.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    val horizontalAlignment = if (alignRight) Alignment.End else Alignment.Start

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = sidePaddingDp.dp),
        horizontalAlignment = horizontalAlignment,
    ) {
        // Large Clock Display
        Text(
            text = timeString,
            color = contentColor,
            fontSize = 58.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-1.5).sp,
            lineHeight = 58.sp,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { launchClockApp(context) },
                ),
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Full Date / Calendar Display
        Text(
            text = dateString,
            color = contentColor.copy(alpha = 0.85f),
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 22.sp,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { launchCalendarApp(context) },
                ),
        )
    }
}

/** Attempts to launch the device's clock or alarms activity. */
private fun launchClockApp(context: Context) {
    val candidates = listOf(
        Intent(AlarmClock.ACTION_SHOW_ALARMS),
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CLOCK),
        Intent(AlarmClock.ACTION_SET_ALARM),
    )
    for (intent in candidates) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        } catch (_: Exception) {}
    }
}

/** Attempts to launch the device's calendar activity. */
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
