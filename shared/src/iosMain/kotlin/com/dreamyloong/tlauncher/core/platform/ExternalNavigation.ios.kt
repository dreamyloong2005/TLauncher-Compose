package com.dreamyloong.tlauncher.core.platform

import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun launcherAppVersionName(): String {
    return NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "dev"
}

actual fun openExternalUrl(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}
