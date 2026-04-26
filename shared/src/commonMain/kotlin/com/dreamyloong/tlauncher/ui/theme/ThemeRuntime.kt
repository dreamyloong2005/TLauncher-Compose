package com.dreamyloong.tlauncher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

@Composable
fun rememberSystemDarkTheme(): Boolean = isSystemInDarkTheme()

