// SPDX-License-Identifier: GPL-3.0-or-later
package dev.victorialauncher.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Manages an in-memory blurred version of the system wallpaper.
 * Allows instant, native rendering of a frosted-glass background for screens like AppListScreen
 * and SearchScreen without relying on unstable WindowManager FLAG_BLUR_BEHIND flags on the launcher activity.
 */
object WallpaperBlurManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _blurredWallpaper = MutableStateFlow<Bitmap?>(null)
    val blurredWallpaper: StateFlow<Bitmap?> = _blurredWallpaper.asStateFlow()

    @Volatile
    private var isInitialized = false
    @Volatile
    private var isListenerRegistered = false

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true
        loadAndBlur(context)
        registerListener(context)
    }

    fun refresh(context: Context) {
        loadAndBlur(context)
    }

    private fun registerListener(context: Context) {
        if (isListenerRegistered || Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return
        val appContext = context.applicationContext
        val manager = runCatching {
            appContext.getSystemService(Context.WALLPAPER_SERVICE) as? WallpaperManager
        }.getOrNull() ?: return

        val listener = WallpaperManager.OnColorsChangedListener { _, which ->
            if (which and WallpaperManager.FLAG_SYSTEM != 0) {
                loadAndBlur(appContext)
            }
        }
        runCatching {
            manager.addOnColorsChangedListener(listener, Handler(Looper.getMainLooper()))
            isListenerRegistered = true
        }
    }

    fun loadAndBlur(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                extractWallpaper(appContext)
            } ?: run {
                _blurredWallpaper.value = null
                return@launch
            }

            val targetWidth = 360
            val aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat().coerceAtLeast(1f)
            val targetHeight = (targetWidth * aspectRatio).toInt().coerceIn(100, 1920)

            // Render onto a software ARGB_8888 canvas to safely handle HARDWARE bitmaps
            val scaled = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(scaled)
            val paint = Paint(Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(bitmap, null, Rect(0, 0, targetWidth, targetHeight), paint)

            val blurred = FastBlur.blur(scaled, radius = 25, canReuseInBitmap = true) ?: scaled
            _blurredWallpaper.value = blurred
        }
    }

    private fun extractWallpaper(context: Context): Bitmap? {
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
}
