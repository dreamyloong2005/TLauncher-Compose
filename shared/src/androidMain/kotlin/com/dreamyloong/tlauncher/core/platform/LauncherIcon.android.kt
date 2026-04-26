package com.dreamyloong.tlauncher.core.platform

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import com.dreamyloong.tlauncher.core.theme.ThemeLauncherIcon
import kotlin.system.exitProcess

actual fun applyLauncherIcon(icon: LauncherIconSource) = Unit

actual fun applyLauncherIconAndCloseLauncher(icon: LauncherIconSource) {
    val context = requireAndroidLauncherContext()
    context.applyLauncherIconVariant(icon.androidVariant())

    val activity = AndroidLauncherStorageContextHolder.currentActivity?.get()
    if (activity != null) {
        activity.finishAndRemoveTask()
    }
    Handler(Looper.getMainLooper()).postDelayed(
        {
            exitProcess(0)
        },
        250L,
    )
}

@Composable
actual fun BindLauncherIcon(icon: ThemeLauncherIcon) = Unit

private enum class LauncherIconVariant(val aliasName: String) {
    Day(".LauncherIconDay"),
    Night(".LauncherIconNight"),
}

private fun LauncherIconSource.androidVariant(): LauncherIconVariant {
    return when (this) {
        LauncherIconSource.Default -> LauncherIconVariant.Day
        LauncherIconSource.Night -> LauncherIconVariant.Night
        is LauncherIconSource.Asset -> LauncherIconVariant.Day
    }
}

private fun Context.applyLauncherIconVariant(variant: LauncherIconVariant) {
    val target = variant.componentName(this)
    val others = LauncherIconVariant.entries
        .filterNot { candidate -> candidate == variant }
        .map { candidate -> candidate.componentName(this) }

    if (packageManager.getComponentEnabledSetting(target) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
        packageManager.setComponentEnabledSetting(
            target,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP,
        )
    }

    others.forEach { component ->
        if (packageManager.getComponentEnabledSetting(component) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            packageManager.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP,
            )
        }
    }
}

private fun LauncherIconVariant.componentName(context: Context): ComponentName {
    return ComponentName(context.packageName, context.packageName + aliasName)
}
