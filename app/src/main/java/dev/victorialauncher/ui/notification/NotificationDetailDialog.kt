// SPDX-License-Identifier: GPL-3.0-or-later
package dev.victorialauncher.ui.notification

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
 * When a notification contains multiple messages (e.g. from MessagingStyle or InboxStyle),
 * they are presented as a clean vertical message list.
 */
@Composable
fun NotificationDetailDialog(
    item: AppNotificationItem,
    appInfo: AppInfo?,
    appName: String,
    onDismissRequest: () -> Unit,
    onOpen: (AppNotificationItem) -> Unit,
    onDismissNotification: (AppNotificationItem) -> Unit,
    allNotifications: List<AppNotificationItem> = emptyList(),
) {
    var currentKey by remember(item.key) { mutableStateOf(item.key) }
    val effectiveList = if (allNotifications.any { it.key == item.key }) allNotifications else (listOf(item) + allNotifications)
    val selectedItem = effectiveList.find { it.key == currentKey } ?: item

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
            val timeText = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(selectedItem.postTime))

            // Glassmorphism card
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
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
                        .padding(20.dp)
                ) {
                    // If multiple conversations / notifications exist for this app, display selection chips
                    if (effectiveList.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            effectiveList.forEach { notif ->
                                val isSelected = notif.key == selectedItem.key
                                val countSuffix = if (notif.messages.size > 1) " (${notif.messages.size})" else ""
                                val chipTitle = (if (notif.title.isNotBlank()) notif.title else appName) + countSuffix

                                Surface(
                                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(16.dp),
                                    border = if (isSelected) {
                                        BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                                    } else null,
                                    modifier = Modifier.clickable { currentKey = notif.key },
                                ) {
                                    Text(
                                        text = chipTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                    }

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
                            if (!selectedItem.subText.isNullOrBlank()) {
                                Text(
                                    text = selectedItem.subText,
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

                    // Content area: Multiple messages in a list or single notification text
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (selectedItem.messages.isNotEmpty()) {
                            if (selectedItem.title.isNotBlank()) {
                                Text(
                                    text = selectedItem.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                                Spacer(Modifier.height(8.dp))
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                selectedItem.messages.forEach { msg ->
                                    Surface(
                                        color = Color.White.copy(alpha = 0.07f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            if (!msg.sender.isNullOrBlank() && msg.sender != selectedItem.title) {
                                                Text(
                                                    text = msg.sender,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                                )
                                                Spacer(Modifier.height(2.dp))
                                            }
                                            Text(
                                                text = msg.text,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.White.copy(alpha = 0.92f),
                                                lineHeight = 20.sp,
                                            )
                                            if (msg.timestamp > 0L) {
                                                val msgTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    text = msgTime,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White.copy(alpha = 0.45f),
                                                    modifier = Modifier.align(Alignment.End),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            if (selectedItem.title.isNotBlank()) {
                                Text(
                                    text = selectedItem.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                                Spacer(Modifier.height(6.dp))
                            }
                            if (selectedItem.text.isNotBlank()) {
                                Text(
                                    text = selectedItem.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.88f),
                                    lineHeight = 20.sp,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Action buttons: Dismiss and Open
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (selectedItem.isClearable) {
                            TextButton(
                                onClick = { onDismissNotification(selectedItem) },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color.White.copy(alpha = 0.75f)
                                ),
                            ) {
                                Text(stringResource(R.string.notification_dismiss))
                            }
                            Spacer(Modifier.width(8.dp))
                        }

                        OutlinedButton(
                            onClick = { onOpen(selectedItem) },
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
