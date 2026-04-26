package com.dreamyloong.tlauncher.core.platform

import androidx.compose.runtime.Composable

@Composable
actual fun rememberManageStorageAccessState(): ManageStorageAccessState {
    return ManageStorageAccessState(
        isSupported = false,
        isGranted = false,
        requestAccess = {},
    )
}
