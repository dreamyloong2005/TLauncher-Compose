package com.dreamyloong.tlauncher.feature.navigation

import com.dreamyloong.tlauncher.core.model.GameInstanceId

sealed interface TLauncherScreen {
    data object Home : TLauncherScreen
    data object Library : TLauncherScreen
    data object CreateGame : TLauncherScreen
    data object Settings : TLauncherScreen
    data object AccountManager : TLauncherScreen
    data object AddAccount : TLauncherScreen
    data object ExtensionManager : TLauncherScreen
    data object About : TLauncherScreen
    data class GameDetail(val instanceId: GameInstanceId) : TLauncherScreen
    data class ExtensionDetail(val identityId: String) : TLauncherScreen
    data class ContributedPage(val pageId: String) : TLauncherScreen
}
