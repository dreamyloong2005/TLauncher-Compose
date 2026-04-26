package com.dreamyloong.tlauncher.windows

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.dreamyloong.tlauncher.app.TLauncherApp
import com.dreamyloong.tlauncher.core.account.UnsupportedLauncherAccountService
import com.dreamyloong.tlauncher.core.model.PlatformTarget
import com.dreamyloong.tlauncher.core.platform.LauncherIconSource
import com.dreamyloong.tlauncher.core.platform.rememberWindowsLauncherIcon
import com.dreamyloong.tlauncher.sdk.host.EmptyExtensionHostServices
import launcher_compose.composeapp.generated.resources.Res
import launcher_compose.composeapp.generated.resources.window_icon_night
import launcher_compose.composeapp.generated.resources.window_icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skia.Image

fun main() {
    val cauthStatus = CAuthWindowsRuntime.status()
    if (!cauthStatus.available) {
        System.err.println("CAuth Windows runtime is unavailable: ${cauthStatus.message}")
    }

    application {
        val launcherIcon by rememberWindowsLauncherIcon()
        Window(
            onCloseRequest = ::exitApplication,
            title = "TLauncher",
            icon = rememberLauncherWindowIconPainter(launcherIcon),
        ) {
            TLauncherApp(
                target = PlatformTarget.WINDOWS,
                accountService = if (cauthStatus.available) {
                    rememberCAuthWindowsAccountService()
                } else {
                    UnsupportedLauncherAccountService
                },
                hostServices = if (cauthStatus.available) {
                    rememberCAuthWindowsHostServices()
                } else {
                    EmptyExtensionHostServices
                },
            )
        }
    }
}

@Composable
private fun rememberLauncherWindowIconPainter(icon: LauncherIconSource): Painter {
    val customIcon = remember(icon) {
        (icon as? LauncherIconSource.Asset)?.let(::loadAssetIconPainter)
    }
    return customIcon ?: painterResource(
        when (icon) {
            LauncherIconSource.Default -> Res.drawable.window_icon
            LauncherIconSource.Night -> Res.drawable.window_icon_night
            is LauncherIconSource.Asset -> Res.drawable.window_icon
        },
    )
}

private fun loadAssetIconPainter(icon: LauncherIconSource.Asset): Painter? {
    return runCatching {
        BitmapPainter(Image.makeFromEncoded(icon.bytes).toComposeImageBitmap())
    }.getOrNull()
}
