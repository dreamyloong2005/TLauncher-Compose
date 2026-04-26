package com.dreamyloong.tlauncher.core.platform

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.Composable

object SystemBackCoordinator {
    private val focusedTextInputs = mutableStateMapOf<String, Boolean>()

    val isTextInputFocused: Boolean
        get() = focusedTextInputs.values.any { it }

    fun updateTextInputFocus(
        nodeId: String,
        focused: Boolean,
    ) {
        if (focused) {
            focusedTextInputs[nodeId] = true
        } else {
            focusedTextInputs.remove(nodeId)
        }
    }

    fun clearTextInputFocus(nodeId: String) {
        focusedTextInputs.remove(nodeId)
    }
}

@Composable
expect fun BindSystemBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
)
