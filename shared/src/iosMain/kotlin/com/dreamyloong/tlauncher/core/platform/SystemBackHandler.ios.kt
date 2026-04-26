package com.dreamyloong.tlauncher.core.platform

import androidx.compose.runtime.Composable

@Composable
actual fun BindSystemBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) = Unit
