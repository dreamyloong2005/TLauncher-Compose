package com.dreamyloong.tlauncher.core.platform

import androidx.compose.runtime.Composable
import com.dreamyloong.tlauncher.core.theme.ThemeLauncherIcon

sealed interface LauncherIconSource {
    data object Default : LauncherIconSource

    data object Night : LauncherIconSource

    class Asset(
        val path: String,
        val bytes: ByteArray,
    ) : LauncherIconSource {
        private val fingerprint = bytes.contentHashCode()

        override fun equals(other: Any?): Boolean {
            return other is Asset &&
                path == other.path &&
                fingerprint == other.fingerprint &&
                bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int {
            return 31 * path.hashCode() + fingerprint
        }

        override fun toString(): String {
            return "LauncherIconSource.Asset(path=$path, bytes=${bytes.size})"
        }
    }
}

object LauncherIconController {
    fun setIcon(icon: LauncherIconSource) {
        applyLauncherIcon(icon)
    }

    fun setIconAndCloseLauncher(icon: LauncherIconSource) {
        applyLauncherIconAndCloseLauncher(icon)
    }

    fun resetIcon() {
        setIcon(LauncherIconSource.Default)
    }
}

fun ThemeLauncherIcon.toLauncherIconSource(): LauncherIconSource {
    return when (this) {
        ThemeLauncherIcon.DEFAULT -> LauncherIconSource.Default
        ThemeLauncherIcon.NIGHT -> LauncherIconSource.Night
    }
}

expect fun applyLauncherIcon(icon: LauncherIconSource)

expect fun applyLauncherIconAndCloseLauncher(icon: LauncherIconSource)

@Composable
expect fun BindLauncherIcon(icon: ThemeLauncherIcon)
