package com.dreamyloong.tlauncher.core.platform

import android.content.Intent
import android.net.Uri

actual fun launcherAppVersionName(): String {
    val context = requireAndroidLauncherContext()
    @Suppress("DEPRECATION")
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    return packageInfo.versionName ?: "dev"
}

actual fun openExternalUrl(url: String) {
    val context = requireAndroidLauncherContext()
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
