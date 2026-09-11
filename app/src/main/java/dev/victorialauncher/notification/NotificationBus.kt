// SPDX-License-Identifier: GPL-3.0-or-later
package dev.victorialauncher.notification

import android.app.PendingIntent
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Clean snapshot of an active system notification for a specific app package.
 */
@Immutable
data class AppNotificationItem(
    val key: String,
    val packageName: String,
    val title: String,
    val text: String,
    val subText: String? = null,
    val postTime: Long = System.currentTimeMillis(),
    val contentIntent: PendingIntent? = null,
    val isClearable: Boolean = true,
)

/**
 * Central event bus bridging the NotificationListenerService with the Compose UI layers.
 */
object NotificationBus {
    private val _notifications = MutableStateFlow<Map<String, List<AppNotificationItem>>>(emptyMap())
    val notifications: StateFlow<Map<String, List<AppNotificationItem>>> = _notifications

    private var cancelCallback: ((String) -> Unit)? = null

    fun registerCancelCallback(callback: ((String) -> Unit)?) {
        cancelCallback = callback
    }

    fun updateNotifications(items: List<AppNotificationItem>) {
        val grouped = items
            .sortedByDescending { it.postTime }
            .groupBy { it.packageName }
        _notifications.value = grouped
    }

    fun dismissNotification(key: String) {
        cancelCallback?.invoke(key)
    }
}
