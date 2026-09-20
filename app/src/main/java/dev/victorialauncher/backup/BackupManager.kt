// SPDX-License-Identifier: GPL-3.0-or-later
package dev.victorialauncher.backup

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.core.content.FileProvider
import dev.victorialauncher.BuildConfig
import dev.victorialauncher.data.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupManager {
    const val BACKUP_EXTENSION = ".victoriabackup"
    private const val META_ENTRY = "meta.json"
    private const val PREFS_ENTRY = "preferences.json"
    private const val WALLPAPER_ENTRY = "wallpaper.png"

    fun getBackupsDir(context: Context): File {
        val dir = File(context.filesDir, "backups")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Extracts current wallpaper bitmap if accessible.
     */
    fun extractWallpaperBitmap(context: Context): Bitmap? {
        val wallpaperManager = WallpaperManager.getInstance(context)
        return runCatching {
            val drawable = wallpaperManager.drawable ?: wallpaperManager.peekDrawable() ?: return null
            if (drawable is BitmapDrawable && drawable.bitmap != null) {
                drawable.bitmap
            } else {
                val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 1080
                val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 1920
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
        }.getOrNull()
    }

    /**
     * Creates a full backup containing all preferences and optionally the wallpaper.
     */
    suspend fun createBackup(
        context: Context,
        prefs: Prefs,
        type: BackupType = BackupType.MANUAL,
        includeWallpaper: Boolean = true,
        maxKeep: Int = 5,
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val now = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            val prettyFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dateStr = dateFormat.format(Date(now))
            val prettyDateStr = prettyFormat.format(Date(now))

            val dir = getBackupsDir(context)
            val prefix = if (type == BackupType.AUTOMATIC) "victoria_autobackup_" else "victoria_backup_"
            val targetFile = File(dir, "$prefix$dateStr$BACKUP_EXTENSION")

            // 1. Export DataStore preferences to JSON
            val preferencesJson = prefs.exportAllPreferencesJson()

            // 2. Extract wallpaper if requested
            val wallpaperBitmap = if (includeWallpaper) extractWallpaperBitmap(context) else null
            val hasWallpaper = wallpaperBitmap != null

            // 3. Build metadata
            val metaJson = JSONObject().apply {
                put("versionCode", BuildConfig.VERSION_CODE)
                put("versionName", BuildConfig.VERSION_NAME)
                put("timestamp", now)
                put("formattedDate", prettyDateStr)
                put("hasWallpaper", hasWallpaper)
                put("type", type.name)
            }.toString()

            // 4. Write zip archive
            FileOutputStream(targetFile).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    // Entry 1: meta.json
                    zos.putNextEntry(ZipEntry(META_ENTRY))
                    zos.write(metaJson.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()

                    // Entry 2: preferences.json
                    zos.putNextEntry(ZipEntry(PREFS_ENTRY))
                    zos.write(preferencesJson.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()

                    // Entry 3: wallpaper.png
                    if (wallpaperBitmap != null) {
                        zos.putNextEntry(ZipEntry(WALLPAPER_ENTRY))
                        val baos = ByteArrayOutputStream()
                        wallpaperBitmap.compress(Bitmap.CompressFormat.PNG, 90, baos)
                        zos.write(baos.toByteArray())
                        zos.closeEntry()
                    }
                }
            }

            // 5. Prune backups to respect user retention limit
            pruneOldBackups(context, maxKeep)

            targetFile
        }
    }

    /**
     * Restores preferences and wallpaper from a backup file.
     */
    suspend fun restoreBackup(
        context: Context,
        prefs: Prefs,
        backupFile: File,
    ): Result<BackupMeta> = withContext(Dispatchers.IO) {
        runCatching {
            FileInputStream(backupFile).use { fis ->
                restoreFromInputStream(context, prefs, fis)
            }
        }
    }

    /**
     * Restores preferences and wallpaper from an external content URI.
     */
    suspend fun restoreBackupFromUri(
        context: Context,
        prefs: Prefs,
        uri: Uri,
    ): Result<BackupMeta> = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                restoreFromInputStream(context, prefs, stream)
            } ?: throw IllegalStateException("Could not open input stream from URI")
        }
    }

    private suspend fun restoreFromInputStream(
        context: Context,
        prefs: Prefs,
        inputStream: InputStream,
    ): BackupMeta {
        var meta: BackupMeta? = null
        var preferencesJson: String? = null
        var wallpaperBytes: ByteArray? = null

        ZipInputStream(inputStream).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                when (entry.name) {
                    META_ENTRY -> {
                        val text = zis.bufferedReader(Charsets.UTF_8).readText()
                        val json = JSONObject(text)
                        meta = BackupMeta(
                            versionCode = json.optInt("versionCode", 1),
                            versionName = json.optString("versionName", ""),
                            timestamp = json.optLong("timestamp", 0L),
                            formattedDate = json.optString("formattedDate", ""),
                            hasWallpaper = json.optBoolean("hasWallpaper", false),
                            type = BackupType.fromName(json.optString("type")),
                            preferenceCount = 0,
                        )
                    }
                    PREFS_ENTRY -> {
                        preferencesJson = zis.bufferedReader(Charsets.UTF_8).readText()
                    }
                    WALLPAPER_ENTRY -> {
                        val baos = ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        var len: Int
                        while (zis.read(buffer).also { len = it } != -1) {
                            baos.write(buffer, 0, len)
                        }
                        wallpaperBytes = baos.toByteArray()
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        // Apply preferences
        if (preferencesJson != null) {
            val count = prefs.importPreferencesJson(preferencesJson)
            if (meta != null) {
                meta = meta.copy(preferenceCount = count)
            }
        } else {
            throw IllegalArgumentException("No preferences.json found in backup archive")
        }

        // Apply wallpaper if present
        if (wallpaperBytes != null) {
            runCatching {
                val bitmap = BitmapFactory.decodeByteArray(wallpaperBytes, 0, wallpaperBytes.size)
                if (bitmap != null) {
                    val wallpaperManager = WallpaperManager.getInstance(context)
                    wallpaperManager.setBitmap(bitmap)
                }
            }
        }

        return meta ?: BackupMeta(
            versionCode = BuildConfig.VERSION_CODE,
            versionName = BuildConfig.VERSION_NAME,
            timestamp = System.currentTimeMillis(),
            formattedDate = "",
            hasWallpaper = wallpaperBytes != null,
            type = BackupType.MANUAL,
            preferenceCount = 0,
        )
    }

    /**
     * Lists all local backup files sorted by creation date descending.
     */
    fun listBackups(context: Context): List<BackupItem> {
        val dir = getBackupsDir(context)
        val files = dir.listFiles { file ->
            file.isFile && (file.name.endsWith(BACKUP_EXTENSION) || file.name.endsWith(".zip"))
        } ?: return emptyList()

        return files.map { file ->
            val meta = readMetaOnly(file)
            val size = file.length()
            BackupItem(
                file = file,
                meta = meta,
                sizeBytes = size,
                formattedSize = formatFileSize(size),
            )
        }.sortedByDescending { it.meta?.timestamp ?: it.file.lastModified() }
    }

    /**
     * Reads metadata without fully decompressing the entire backup.
     */
    fun readMetaOnly(file: File): BackupMeta? {
        return runCatching {
            ZipInputStream(FileInputStream(file)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (entry.name == META_ENTRY) {
                        val text = zis.bufferedReader(Charsets.UTF_8).readText()
                        val json = JSONObject(text)
                        return BackupMeta(
                            versionCode = json.optInt("versionCode", 1),
                            versionName = json.optString("versionName", ""),
                            timestamp = json.optLong("timestamp", file.lastModified()),
                            formattedDate = json.optString("formattedDate", ""),
                            hasWallpaper = json.optBoolean("hasWallpaper", false),
                            type = BackupType.fromName(json.optString("type")),
                            preferenceCount = json.optInt("itemCount", 0),
                        )
                    }
                    entry = zis.nextEntry
                }
            }
            null
        }.getOrNull()
    }

    /**
     * Deletes a local backup file.
     */
    fun deleteBackup(file: File): Boolean {
        return runCatching { file.delete() }.getOrDefault(false)
    }

    /**
     * Shares a backup file via Android system share sheet.
     */
    fun shareBackup(context: Context, file: File) {
        runCatching {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, file.name).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    /**
     * Enforces the maximum retention limit of backups.
     */
    fun pruneOldBackups(context: Context, maxKeep: Int) {
        if (maxKeep <= 0) return
        val dir = getBackupsDir(context)
        val files = dir.listFiles { file ->
            file.isFile && (file.name.endsWith(BACKUP_EXTENSION) || file.name.endsWith(".zip"))
        } ?: return

        if (files.size > maxKeep) {
            val sorted = files.sortedByDescending { it.lastModified() }
            val toRemove = sorted.drop(maxKeep)
            toRemove.forEach { it.delete() }
        }
    }

    /**
     * Checks if an automatic backup is due and runs it if enabled.
     */
    suspend fun checkAndRunAutoBackup(context: Context, prefs: Prefs) = withContext(Dispatchers.IO) {
        runCatching {
            val enabled = prefs.autoBackupEnabled.first()
            if (!enabled) return@runCatching

            val freq = prefs.autoBackupFrequency.first()
            val lastTime = prefs.lastAutoBackupTimestamp.first()
            val intervalMillis = freq.days * 24L * 60L * 60L * 1000L
            val now = System.currentTimeMillis()

            if (now - lastTime >= intervalMillis) {
                val includeWallpaper = prefs.backupIncludeWallpaper.first()
                val maxKeep = prefs.backupMaxKeep.first()
                createBackup(
                    context = context,
                    prefs = prefs,
                    type = BackupType.AUTOMATIC,
                    includeWallpaper = includeWallpaper,
                    maxKeep = maxKeep,
                )
                prefs.setLastAutoBackupTimestamp(now)
            }
        }
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024f)
            else -> String.format(Locale.US, "%.1f MB", bytes / (1024f * 1024f))
        }
    }
}
