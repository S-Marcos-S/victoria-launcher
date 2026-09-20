// SPDX-License-Identifier: GPL-3.0-or-later
package dev.victorialauncher.backup

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.victorialauncher.VictoriaApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BackupAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Schedule next check
        schedulePeriodicCheck(context)

        // Run auto backup check on background thread
        val app = context.applicationContext as? VictoriaApp ?: return
        CoroutineScope(Dispatchers.IO).launch {
            BackupManager.checkAndRunAutoBackup(app, app.prefs)
        }
    }

    companion object {
        private const val ACTION_CHECK_AUTO_BACKUP = "dev.victorialauncher.action.CHECK_AUTO_BACKUP"
        private const val REQUEST_CODE = 4091

        fun schedulePeriodicCheck(context: Context) {
            runCatching {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
                val intent = Intent(context, BackupAlarmReceiver::class.java).apply {
                    action = ACTION_CHECK_AUTO_BACKUP
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

                // Check roughly every 6 hours
                val intervalMillis = AlarmManager.INTERVAL_HOUR * 6
                val triggerAtMillis = System.currentTimeMillis() + intervalMillis

                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    intervalMillis,
                    pendingIntent,
                )
            }
        }
    }
}
