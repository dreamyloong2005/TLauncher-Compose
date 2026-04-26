package com.dreamyloong.tlauncher.core.template

import com.dreamyloong.tlauncher.core.model.GameInstance
import com.dreamyloong.tlauncher.core.model.ExtensionIdentityId
import com.dreamyloong.tlauncher.core.model.PlatformTarget
import com.dreamyloong.tlauncher.core.platform.GameLaunchRequest
import com.dreamyloong.tlauncher.core.plugin.TemplateLaunchPreparationInterceptorExtension

data class TemplateLaunchPreparationContext(
    val templatePackageId: ExtensionIdentityId,
    val target: PlatformTarget,
    val currentGame: GameInstance?,
    val selectedGameDirectory: String? = null,
)

data class TemplateFileCheckResult(
    val passed: Boolean,
    val subtitle: String,
    val path: String?,
)

fun dispatchTemplateLaunchPreparation(
    request: GameLaunchRequest?,
    context: TemplateLaunchPreparationContext,
    interceptors: List<TemplateLaunchPreparationInterceptorExtension>,
): GameLaunchRequest? {
    var current = request
    interceptors.forEach { interceptor ->
        interceptor.interceptPreparedLaunchRequest(current, context) { next ->
            current = next
        }
    }
    return current
}
