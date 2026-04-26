package com.dreamyloong.tlauncher.core.platform

import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalView

@Composable
actual fun BindTextInputDismissHandler(
    enabled: Boolean,
    onDismissWhileFocused: () -> Unit,
) {
    val view = LocalView.current
    val currentEnabled by rememberUpdatedState(enabled)
    val currentOnDismiss by rememberUpdatedState(onDismissWhileFocused)

    DisposableEffect(view) {
        val observedView = view.rootView
        var wasKeyboardVisible = isKeyboardVisible(observedView)
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val isKeyboardVisible = isKeyboardVisible(observedView)
            if (currentEnabled && wasKeyboardVisible && !isKeyboardVisible) {
                currentOnDismiss()
            }
            wasKeyboardVisible = isKeyboardVisible
        }
        observedView.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            val observer = observedView.viewTreeObserver
            if (observer.isAlive) {
                observer.removeOnGlobalLayoutListener(listener)
            }
        }
    }
}

private fun isKeyboardVisible(view: View): Boolean {
    if (view.height <= 0) {
        return false
    }
    val visibleRect = Rect()
    view.getWindowVisibleDisplayFrame(visibleRect)
    val heightDifference = view.height - visibleRect.height()
    val thresholdPx = view.resources.displayMetrics.density * 72f
    return heightDifference > thresholdPx
}
