package com.dreamyloong.tlauncher.core.platform

import androidx.compose.runtime.Composable
import com.dreamyloong.tlauncher.core.theme.ThemeLauncherIcon

actual fun applyLauncherIcon(icon: LauncherIconSource) = Unit

actual fun applyLauncherIconAndCloseLauncher(icon: LauncherIconSource) = Unit

@Composable
actual fun BindLauncherIcon(icon: ThemeLauncherIcon) = Unit
