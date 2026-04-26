package com.dreamyloong.tlauncher.core.platform

import java.awt.Desktop
import java.net.URI

actual fun launcherAppVersionName(): String {
    return System.getProperty("tlauncher.version")
        ?.takeIf { it.isNotBlank() }
        ?: "dev"
}

actual fun openExternalUrl(url: String) {
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(URI(url))
    }
}
