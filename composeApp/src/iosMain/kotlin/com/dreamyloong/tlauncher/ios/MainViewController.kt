package com.dreamyloong.tlauncher.ios

import androidx.compose.ui.window.ComposeUIViewController
import com.dreamyloong.tlauncher.app.TLauncherApp
import com.dreamyloong.tlauncher.core.model.PlatformTarget

fun MainViewController() = ComposeUIViewController {
    TLauncherApp(target = PlatformTarget.IOS)
}

