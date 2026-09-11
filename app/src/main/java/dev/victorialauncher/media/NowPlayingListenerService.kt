// SPDX-License-Identifier: GPL-3.0-or-later
package dev.victorialauncher.media

import android.content.ComponentName
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dev.victorialauncher.notification.AppNotificationItem
import dev.victorialauncher.notification.NotificationBus

class NowPlayingListenerService : NotificationListenerService() {

    private lateinit var sessionManager: MediaSessionManager
    private lateinit var componentName: ComponentName

    private var watched: MediaController? = null

    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = republish()
        override fun onPlaybackStateChanged(state: PlaybackState?) = republish()
        override fun onSessionDestroyed() {
            detach()
            republish()
        }
    }

    private val sessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        attachTo(controllers)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        sessionManager = getSystemService(MediaSessionManager::class.java)
        componentName = ComponentName(this, NowPlayingListenerService::class.java)
        NotificationBus.registerCancelCallback { key ->
            runCatching { cancelNotification(key) }
        }
        try {
            sessionManager.addOnActiveSessionsChangedListener(sessionsListener, componentName)
            attachTo(sessionManager.getActiveSessions(componentName))
        } catch (e: SecurityException) {
            NowPlayingBus.update(null)
        }
        refreshNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        NotificationBus.registerCancelCallback(null)
        runCatching { sessionManager.removeOnActiveSessionsChangedListener(sessionsListener) }
        detach()
        NowPlayingBus.update(null)
        NotificationBus.updateNotifications(emptyList())
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        refresh()
        refreshNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        refresh()
        refreshNotifications()
    }

    private fun refreshNotifications() {
        runCatching {
            val active = activeNotifications ?: return@runCatching
            val list = active.mapNotNull { sbn ->
                val notification = sbn.notification ?: return@mapNotNull null
                val extras = notification.extras ?: return@mapNotNull null
                val title = (extras.getCharSequence(Notification.EXTRA_TITLE)
                    ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG))?.toString().orEmpty().trim()
                val text = (extras.getCharSequence(Notification.EXTRA_TEXT)
                    ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT))?.toString().orEmpty().trim()
                val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.trim()

                // If both title and text are blank, it's not useful to show
                if (title.isBlank() && text.isBlank()) return@mapNotNull null

                AppNotificationItem(
                    key = sbn.key,
                    packageName = sbn.packageName,
                    title = if (title.isNotBlank()) title else text,
                    text = if (title.isNotBlank()) text else "",
                    subText = subText,
                    postTime = sbn.postTime,
                    contentIntent = notification.contentIntent,
                    isClearable = sbn.isClearable,
                )
            }
            NotificationBus.updateNotifications(list)
        }
    }

    private fun refresh() {
        if (::sessionManager.isInitialized) {
            runCatching { attachTo(sessionManager.getActiveSessions(componentName)) }
        }
    }

    /** Follow whichever session is worth showing, and watch it for changes. */
    private fun attachTo(controllers: List<MediaController>?) {
        val next = controllers?.firstOrNull { isLive(it.playbackState) }
            ?: controllers?.firstOrNull { it.playbackState != null }

        if (next?.sessionToken != watched?.sessionToken) {
            detach()
            watched = next
            next?.registerCallback(controllerCallback)
        }
        republish()
    }

    private fun detach() {
        watched?.let { runCatching { it.unregisterCallback(controllerCallback) } }
        watched = null
    }

    private fun republish() {
        val controller = watched
        val state = controller?.playbackState
        if (controller == null || !isLive(state)) {
            NowPlayingBus.update(null)
            return
        }

        val metadata = controller.metadata
        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty()
        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST).orEmpty()
        if (title.isBlank() && artist.isBlank()) {
            NowPlayingBus.update(null)
            return
        }

        NowPlayingBus.update(
            NowPlaying(
                title = title,
                artist = artist,
                isPlaying = state?.state == PlaybackState.STATE_PLAYING,
                art = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                    ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART),
                controller = controller,
            )
        )
    }

    /** Playing, paused or buffering counts; stopped, errored or idle does not. */
    private fun isLive(state: PlaybackState?): Boolean = when (state?.state) {
        PlaybackState.STATE_PLAYING,
        PlaybackState.STATE_PAUSED,
        PlaybackState.STATE_BUFFERING,
        -> true

        else -> false
    }
}