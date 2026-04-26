package com.dreamyloong.tlauncher.core.platform

import androidx.compose.runtime.Composable

data class ManageStorageAccessState(
    val isSupported: Boolean,
    val isGranted: Boolean,
    val requestAccess: () -> Unit,
)

@Composable
expect fun rememberManageStorageAccessState(): ManageStorageAccessState
