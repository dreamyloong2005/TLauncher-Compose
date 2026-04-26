package com.dreamyloong.tlauncher.core.action

import com.dreamyloong.tlauncher.core.model.GameInstance
import com.dreamyloong.tlauncher.core.model.GameInstanceId
import com.dreamyloong.tlauncher.core.model.Template
import com.dreamyloong.tlauncher.core.model.ExtensionIdentityId
import com.dreamyloong.tlauncher.core.model.PlatformTarget
import com.dreamyloong.tlauncher.core.i18n.LanguagePreference
import com.dreamyloong.tlauncher.core.platform.GameLaunchRequest
import com.dreamyloong.tlauncher.core.plugin.ActionInterceptorExtension
import com.dreamyloong.tlauncher.core.theme.ThemeLauncherIcon
import com.dreamyloong.tlauncher.core.theme.ThemePreference

sealed interface LauncherAction {
    data class OpenPage(val pageId: String) : LauncherAction

    data class ReplaceCurrentPage(val pageId: String) : LauncherAction

    data class OpenGameDetail(val instanceId: GameInstanceId) : LauncherAction

    data object LoadExtensionPackage : LauncherAction

    data class DeleteExtensionPackage(val sourceName: String) : LauncherAction

    data object NavigateBack : LauncherAction

    data object Refresh : LauncherAction

    data class OpenExternalUrl(val url: String) : LauncherAction

    data class LaunchGame(val request: GameLaunchRequest) : LauncherAction

    data class SelectCurrentGame(val instanceId: GameInstanceId) : LauncherAction

    data class CreateGameInstance(
        val templatePackageId: ExtensionIdentityId,
        val displayName: String,
        val description: String,
    ) : LauncherAction

    data class UpdateGameInstanceDetails(
        val instanceId: GameInstanceId,
        val displayName: String,
        val description: String,
    ) : LauncherAction

    data class DeleteGameInstance(val instanceId: GameInstanceId) : LauncherAction

    data class SetThemePreference(val preference: ThemePreference) : LauncherAction

    data class ApplyLauncherIconAndClose(val icon: ThemeLauncherIcon) : LauncherAction

    data class SetLanguagePreference(val preference: LanguagePreference) : LauncherAction

    data class ChangeExtensionPriority(
        val identityId: String,
        val direction: ExtensionPriorityDirection,
    ) : LauncherAction

    data object CheckForUpdates : LauncherAction
}

enum class ExtensionPriorityDirection {
    INCREASE,
    DECREASE,
}

data class LauncherActionContext(
    val target: PlatformTarget,
    val currentGame: GameInstance?,
    val currentTemplate: Template?,
)

fun interface LauncherActionDispatcher {
    fun dispatch(action: LauncherAction)
}

object NoopLauncherActionDispatcher : LauncherActionDispatcher {
    override fun dispatch(action: LauncherAction) = Unit
}

fun dispatchLauncherAction(
    action: LauncherAction,
    context: LauncherActionContext,
    interceptors: List<ActionInterceptorExtension>,
    terminal: (LauncherAction) -> Unit,
) {
    fun dispatchAt(
        index: Int,
        currentAction: LauncherAction,
    ) {
        if (index >= interceptors.size) {
            terminal(currentAction)
            return
        }

        interceptors[index].interceptAction(currentAction, context) { nextAction ->
            dispatchAt(index + 1, nextAction)
        }
    }

    dispatchAt(0, action)
}
