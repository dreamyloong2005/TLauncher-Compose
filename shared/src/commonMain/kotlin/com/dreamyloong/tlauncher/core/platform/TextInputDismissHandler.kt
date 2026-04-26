package com.dreamyloong.tlauncher.core.platform

import androidx.compose.runtime.Composable

@Composable
expect fun BindTextInputDismissHandler(
    enabled: Boolean,
    onDismissWhileFocused: () -> Unit,
)
