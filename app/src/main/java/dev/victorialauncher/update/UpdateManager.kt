// SPDX-License-Identifier: GPL-3.0-or-later
package dev.victorialauncher.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import dev.victorialauncher.BuildConfig
import dev.victorialauncher.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant

data class UpdateInfo(
    val hasUpdate: Boolean,
    val tagName: String,
    val releaseName: String,
    val commitSha: String?,
    val changelog: String,
    val apkDownloadUrl: String,
    val apkSize: Long,
    val publishedAtMs: Long,
)

object UpdateManager {
    private const val GITHUB_REPO = "S-Marcos-S/victoria-launcher"
    private const val RELEASES_API_URL = "https://api.github.com/repos/$GITHUB_REPO/releases/tags/latest"

    private val _updateAvailable = MutableStateFlow<UpdateInfo?>(null)
    val updateAvailable: StateFlow<UpdateInfo?> = _updateAvailable.asStateFlow()

    private var lastCheckTimeMs = 0L
    private const val CHECK_INTERVAL_MS = 5 * 60 * 1000L // 5 minutos de cache

    fun checkForUpdates(coroutineScope: CoroutineScope, force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && now - lastCheckTimeMs < CHECK_INTERVAL_MS && _updateAvailable.value != null) {
            return
        }

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val url = URL(RELEASES_API_URL)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    setRequestProperty("User-Agent", "VictoriaLauncher/${BuildConfig.VERSION_NAME}")
                }

                if (conn.responseCode == 200) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)
                    val tagName = json.optString("tag_name", "latest")
                    val releaseName = json.optString("name", "")
                    val body = json.optString("body", "")

                    // Extrai commit SHA da descrição se presente: "Built automatically from commit `...`."
                    val shaRegex = Regex("""commit [`']?([a-f0-9]{7,40})""", RegexOption.IGNORE_CASE)
                    val remoteSha = shaRegex.find(body)?.groupValues?.get(1)?.trim()

                    val assets = json.optJSONArray("assets")
                    var apkUrl: String? = null
                    var apkSize: Long = 0L
                    var updatedAtMs: Long = 0L

                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.optString("name", "")
                            if (name.contains("release") && name.endsWith(".apk")) {
                                apkUrl = asset.optString("browser_download_url")
                                apkSize = asset.optLong("size", 0L)
                                val updatedStr = asset.optString("updated_at", "")
                                if (updatedStr.isNotBlank()) {
                                    try {
                                        updatedAtMs = Instant.parse(updatedStr).toEpochMilli()
                                    } catch (_: Exception) {}
                                }
                                break
                            }
                        }
                    }

                    if (apkUrl.isNullOrBlank()) {
                        apkUrl = "https://github.com/$GITHUB_REPO/releases/download/latest/victoria-launcher-release.apk"
                    }

                    val currentSha = BuildConfig.GIT_SHA.trim()
                    val currentBuildTime = BuildConfig.BUILD_TIME_MILLIS

                    val hasNewerBuild = when {
                        // Se o commit SHA é conhecido nos dois lados, compara diretamente:
                        !remoteSha.isNullOrBlank() && currentSha.isNotBlank() -> {
                            val isSameSha = remoteSha.startsWith(currentSha, ignoreCase = true) ||
                                    currentSha.startsWith(remoteSha, ignoreCase = true)
                            !isSameSha
                        }
                        // Fallback para comparação por timestamp:
                        updatedAtMs > 0L && currentBuildTime > 0L -> {
                            updatedAtMs > currentBuildTime + 30_000L
                        }
                        else -> false
                    }

                    lastCheckTimeMs = now
                    if (hasNewerBuild) {
                        // Obtém informações detalhadas de mudanças (body do CHANGELOG ou commit message)
                        var changelog = ""
                        if (body.isNotBlank()) {
                            val lines = body.lines().filter {
                                !it.contains("Direct APK Download") &&
                                        !it.contains("releases/download") &&
                                        !it.startsWith("- [Download") &&
                                        !it.contains("Built automatically from commit")
                            }
                            val candidate = lines.joinToString("\n").trim()
                            if (candidate.isNotBlank() && candidate.lines().size >= 2) {
                                changelog = candidate
                            }
                        }

                        if (changelog.isBlank() && !remoteSha.isNullOrBlank()) {
                            try {
                                val commitUrl = URL("https://api.github.com/repos/$GITHUB_REPO/commits/$remoteSha")
                                val commitConn = (commitUrl.openConnection() as HttpURLConnection).apply {
                                    connectTimeout = 5000
                                    readTimeout = 5000
                                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                                    setRequestProperty("User-Agent", "VictoriaLauncher/${BuildConfig.VERSION_NAME}")
                                }
                                if (commitConn.responseCode == 200) {
                                    val commitResp = commitConn.inputStream.bufferedReader().use { it.readText() }
                                    val commitJson = JSONObject(commitResp)
                                    val msg = commitJson.optJSONObject("commit")?.optString("message", "")
                                    if (!msg.isNullOrBlank()) {
                                        changelog = msg.trim()
                                    }
                                }
                            } catch (_: Exception) {}
                        }

                        if (changelog.isBlank()) {
                            changelog = "Nova versão compilada automaticamente via GitHub Actions."
                        }

                        _updateAvailable.value = UpdateInfo(
                            hasUpdate = true,
                            tagName = tagName,
                            releaseName = releaseName,
                            commitSha = remoteSha,
                            changelog = changelog,
                            apkDownloadUrl = apkUrl,
                            apkSize = apkSize,
                            publishedAtMs = updatedAtMs,
                        )
                    } else {
                        _updateAvailable.value = null
                    }
                }
            } catch (_: Exception) {
                // Silencia erros de conexão offline
            }
        }
    }

    fun startDownload(context: Context, update: UpdateInfo) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            if (downloadManager == null) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(update.apkDownloadUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                return
            }

            try {
                val existingFile = java.io.File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "victoria-launcher-release.apk",
                )
                if (existingFile.exists()) {
                    existingFile.delete()
                }
            } catch (_: Exception) {}

            val request = DownloadManager.Request(Uri.parse(update.apkDownloadUrl)).apply {
                setTitle(context.getString(R.string.app_name))
                setDescription(context.getString(R.string.update_downloading_notification_desc, update.commitSha?.let { " (${it.take(7)})" } ?: ""))
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "victoria-launcher-release.apk"
                )
                setMimeType("application/vnd.android.package-archive")
            }

            downloadManager.enqueue(request)
            Toast.makeText(
                context,
                context.getString(R.string.update_toast_downloading),
                Toast.LENGTH_LONG,
            ).show()
        } catch (e: Exception) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(update.apkDownloadUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
            } catch (_: Exception) {
                Toast.makeText(
                    context,
                    context.getString(R.string.update_toast_error, e.message ?: ""),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }
}
