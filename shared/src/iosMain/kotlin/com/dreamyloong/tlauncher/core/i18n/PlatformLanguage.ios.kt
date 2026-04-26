package com.dreamyloong.tlauncher.core.i18n

import platform.Foundation.NSBundle

actual fun platformLanguageTag(): String {
    return (NSBundle.mainBundle.preferredLocalizations.firstOrNull() as? String)
        ?.replace('_', '-')
        ?: "en-US"
}

