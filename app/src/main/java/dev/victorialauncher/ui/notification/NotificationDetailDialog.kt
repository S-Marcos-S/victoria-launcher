// SPDX-License-Identifier: GPL-3.0-or-later
package dev.victorialauncher.ui.notification

import android.content.Context
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.victorialauncher.R
import dev.victorialauncher.data.AppInfo
import dev.victorialauncher.notification.AppNotificationItem
import dev.victorialauncher.ui.common.AppIcon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A frosted-glass popup dialog with transparency and blur displaying the full notification content.
 */
@Composable
fun NotificationDetailDialog(
    item: AppNotificationItem,
    appInfo: AppInfo?,
    appName: String,
    onDismissRequest: () -> Unit,
    onOpen: () -> Unit,
    onDismissNotification: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        // Scrim background with transparency
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(onClick = onDismissRequest),
            contentAlignment = Alignment.Center,
        ) {
            val timeText = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(item.postTime))

            // Glassmorphism card
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clickable(enabled = false) {}, // absorb clicks inside dialog
                color = Color(0xDD1E232A), // Deep dark translucent glass
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    // Header: App icon, App name, subtext and timestamp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (appInfo != null) {
                            AppIcon(app = appInfo, sizeDp = 36)
                            Spacer(Modifier.width(12.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = appName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                            )
                            if (!item.subText.isNullOrBlank()) {
                                Text(
                                    text = item.subText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f),
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f),
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Content: Title and full body with scrolling
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (item.title.isNotBlank()) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                        if (item.text.isNotBlank()) {
                            Text(
                                text = item.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.88f),
                                lineHeight = 20.sp,
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (item.isClearable) {
                            TextButton(
                                onClick = onDismissNotification,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color.White.copy(alpha = 0.75f)
                                ),
                            ) {
                                Text(stringResource(R.string.notification_dismiss))
                            }
                            Spacer(Modifier.width(8.dp))
                        }

                        if (item.contentIntent != null) {
                            OutlinedButton(
                                onClick = onOpen,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    )
                                ),
                            ) {
                                Text(stringResource(R.string.notification_open))
                            }
                        }
                    }
                }
            }
        }
    }
}
