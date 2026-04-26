package com.dreamyloong.tlauncher.core.platform

import com.dreamyloong.tlauncher.core.model.GameInstance
import com.dreamyloong.tlauncher.core.model.Template
import com.dreamyloong.tlauncher.core.model.PlatformTarget
import com.dreamyloong.tlauncher.core.plugin.LaunchInterceptorExtension

data class GameLaunchContext(
    val target: PlatformTarget,
    val currentGame: GameInstance?,
    val currentTemplate: Template?,
)

fun dispatchGameLaunch(
    request: GameLaunchRequest,
    context: GameLaunchContext,
    interceptors: List<LaunchInterceptorExtension>,
    terminal: (GameLaunchRequest) -> Unit,
) {
    fun dispatchAt(
        index: Int,
        currentRequest: GameLaunchRequest,
    ) {
        if (index >= interceptors.size) {
            terminal(currentRequest)
            return
        }

        interceptors[index].interceptLaunch(currentRequest, context) { nextRequest ->
            dispatchAt(index + 1, nextRequest)
        }
    }

    dispatchAt(0, request)
}
