package com.dreamyloong.tlauncher.ui

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import androidx.core.view.WindowCompat

internal enum class SystemBarMode {
    Launcher,
    GameHost,
}

@Suppress("DEPRECATION")
internal fun Activity.applyTLauncherSystemBars(mode: SystemBarMode) {
    val inNightMode =
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    val controller = WindowCompat.getInsetsController(window, window.decorView)

    when (mode) {
        SystemBarMode.Launcher -> {
            window.statusBarColor = if (inNightMode) Color.parseColor("#101418") else Color.parseColor("#F7F9FC")
            window.navigationBarColor = if (inNightMode) Color.parseColor("#101418") else Color.parseColor("#FFFFFF")
            controller.isAppearanceLightStatusBars = !inNightMode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                controller.isAppearanceLightNavigationBars = !inNightMode
            }
        }

        SystemBarMode.GameHost -> {
            window.statusBarColor = Color.BLACK
            window.navigationBarColor = Color.BLACK
            controller.isAppearanceLightStatusBars = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }
}
