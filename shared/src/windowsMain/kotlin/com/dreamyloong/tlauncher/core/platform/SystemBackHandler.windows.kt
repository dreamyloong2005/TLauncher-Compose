package com.dreamyloong.tlauncher.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import java.awt.KeyEventDispatcher
import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent

@Composable
actual fun BindSystemBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    val textInputFocused = SystemBackCoordinator.isTextInputFocused
    val currentOnBack by rememberUpdatedState(onBack)
    val currentTextInputFocused by rememberUpdatedState(textInputFocused)
    DisposableEffect(enabled) {
        if (!enabled) {
            return@DisposableEffect onDispose { }
        }
        val keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager()
        val dispatcher = KeyEventDispatcher { event ->
            if (
                event.id == KeyEvent.KEY_PRESSED &&
                event.keyCode == KeyEvent.VK_ESCAPE &&
                !event.isAltDown &&
                !event.isAltGraphDown &&
                !event.isControlDown &&
                !event.isMetaDown &&
                !currentTextInputFocused
            ) {
                currentOnBack()
                true
            } else {
                false
            }
        }
        keyboardFocusManager.addKeyEventDispatcher(dispatcher)
        onDispose {
            keyboardFocusManager.removeKeyEventDispatcher(dispatcher)
        }
    }
}
