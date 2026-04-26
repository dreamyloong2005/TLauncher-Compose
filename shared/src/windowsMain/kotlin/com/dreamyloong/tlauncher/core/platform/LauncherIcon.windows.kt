package com.dreamyloong.tlauncher.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.dreamyloong.tlauncher.core.theme.ThemeLauncherIcon
import javax.swing.SwingUtilities

private object WindowsLauncherIconBinding {
    val iconState = mutableStateOf<LauncherIconSource>(LauncherIconSource.Default)
}

@Composable
fun rememberWindowsLauncherIcon(): State<LauncherIconSource> {
    return WindowsLauncherIconBinding.iconState
}

actual fun applyLauncherIcon(icon: LauncherIconSource) {
    if (SwingUtilities.isEventDispatchThread()) {
        WindowsLauncherIconBinding.iconState.value = icon
    } else {
        SwingUtilities.invokeLater {
            WindowsLauncherIconBinding.iconState.value = icon
        }
    }
}

@Composable
actual fun BindLauncherIcon(icon: ThemeLauncherIcon) {
    LaunchedEffect(icon) {
        applyLauncherIcon(icon.toLauncherIconSource())
    }
}

actual fun applyLauncherIconAndCloseLauncher(icon: LauncherIconSource) {
    applyLauncherIcon(icon)
}
