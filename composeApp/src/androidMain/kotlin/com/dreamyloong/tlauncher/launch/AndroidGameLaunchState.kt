package com.dreamyloong.tlauncher.launch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.dreamyloong.tlauncher.core.platform.GameLaunchRequest
import com.dreamyloong.tlauncher.core.platform.GameLaunchState

@Composable
fun rememberAndroidGameLaunchState(): GameLaunchState {
    val context = LocalContext.current
    return remember(context) {
        GameLaunchState(
            isSupported = true,
            syncLaunchContext = { request -> syncAndroidLaunchContextEnvironment(context, request) },
            launch = { request ->
                when (request) {
                    is GameLaunchRequest.AndroidRuntime -> {
                        syncAndroidLaunchContextEnvironment(context, request)
                        AndroidExtensionHostActivity.intentFor(context, request).also { intent ->
                            if (context !is android.app.Activity) {
                                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    }
                }
            },
        )
    }
}
