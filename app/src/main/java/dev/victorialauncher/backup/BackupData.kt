// SPDX-License-Identifier: GPL-3.0-or-later
package dev.victorialauncher.backup

import dev.victorialauncher.R
import java.io.File

enum class BackupFrequency(val days: Int, val labelRes: Int) {
    DAILY(1, R.string.backup_freq_daily),
    WEEKLY(7, R.string.backup_freq_weekly),
    MONTHLY(30, R.string.backup_freq_monthly);

    companion object {
        fun fromName(name: String?): BackupFrequency =
            runCatching { valueOf(name ?: WEEKLY.name) }.getOrDefault(WEEKLY)
    }
}

enum class BackupType {
    MANUAL,
    AUTOMATIC;

    companion object {
        fun fromName(name: String?): BackupType =
            runCatching { valueOf(name ?: MANUAL.name) }.getOrDefault(MANUAL)
    }
}

data class BackupMeta(
    val versionCode: Int,
    val versionName: String,
    val timestamp: Long,
    val formattedDate: String,
    val hasWallpaper: Boolean,
    val type: BackupType,
    val preferenceCount: Int,
)

data class BackupItem(
    val file: File,
    val meta: BackupMeta?,
    val sizeBytes: Long,
    val formattedSize: String,
)
