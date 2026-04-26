package com.dreamyloong.tlauncher.core.platform

import androidx.compose.runtime.Composable

@Composable
actual fun BindTextInputDismissHandler(
    enabled: Boolean,
    onDismissWhileFocused: () -> Unit,
) = Unit
