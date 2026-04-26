package com.dreamyloong.tlauncher.core.platform

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberManageStorageAccessState(): ManageStorageAccessState {
    val context = LocalContext.current
    val activity = context.findActivity()
    val hostActivity = activity
    val application = context.applicationContext as? Application
    var granted by remember(activity) {
        mutableStateOf(checkManageStorageAccess())
    }

    DisposableEffect(application, activity) {
        if (application == null || activity == null) {
            onDispose {}
        } else {
            val callbacks = object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: android.os.Bundle?) = Unit

                override fun onActivityStarted(activity: Activity) = Unit

                override fun onActivityResumed(activity: Activity) {
                    if (activity === hostActivity) {
                        granted = checkManageStorageAccess()
                    }
                }

                override fun onActivityPaused(activity: Activity) = Unit

                override fun onActivityStopped(activity: Activity) = Unit

                override fun onActivitySaveInstanceState(activity: Activity, outState: android.os.Bundle) = Unit

                override fun onActivityDestroyed(activity: Activity) = Unit
            }
            application.registerActivityLifecycleCallbacks(callbacks)
            onDispose {
                application.unregisterActivityLifecycleCallbacks(callbacks)
            }
        }
    }

    return ManageStorageAccessState(
        isSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && activity != null,
        isGranted = granted,
        requestAccess = {
            val host = activity ?: return@ManageStorageAccessState
            granted = checkManageStorageAccess()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:${host.packageName}"),
                )
                host.startActivity(intent)
            }
        },
    )
}

private fun checkManageStorageAccess(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager()
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext?.findActivity()
        else -> null
    }
}
