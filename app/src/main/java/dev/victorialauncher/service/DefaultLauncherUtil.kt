// SPDX-License-Identifier: GPL-3.0-or-later
package dev.victorialauncher.service

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings

object DefaultLauncherUtil {

    /**
     * Checks whether Victoria Launcher is currently the system's default home launcher.
     */
    fun isDefaultLauncher(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                return roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            }
        }
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.resolveActivity(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        }
        return resolveInfo?.activityInfo?.packageName == context.packageName
    }

    /**
     * Prompts the user to set Victoria Launcher as the default home app.
     * Uses RoleManager on Android 10+ (API 29+) or fallback Settings intents.
     */
    fun requestSetDefaultLauncher(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                try {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return
                } catch (e: Exception) {
                    // Fall back to standard settings if RoleManager intent fails
                }
            }
        }

        try {
            val homeIntent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (homeIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(homeIntent)
                return
            }
        } catch (e: Exception) {
            // Ignore and fallback
        }

        try {
            val manageDefaultAppsIntent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (manageDefaultAppsIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(manageDefaultAppsIntent)
                return
            }
        } catch (e: Exception) {
            // Ignore and fallback
        }

        try {
            context.startActivity(
                Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
            )
        } catch (e: Exception) {
            // Unable to open settings
        }
    }
}
