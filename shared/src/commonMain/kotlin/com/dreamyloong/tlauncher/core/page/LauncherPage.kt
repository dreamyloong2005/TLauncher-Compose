package com.dreamyloong.tlauncher.core.page

abstract class LauncherPage(
    val pageId: String,
) {
    abstract fun buildBaseContribution(context: PageContext): PageContributionBundle
}

class HostedLauncherPage(
    pageId: String,
) : LauncherPage(pageId) {
    override fun buildBaseContribution(context: PageContext): PageContributionBundle {
        return PageContributionBundle(
            sourceId = "core.page.host.$pageId",
            nodes = emptyList(),
        )
    }
}

object PageIds {
    const val HOME = "page.home"
    const val LIBRARY = "page.library"
    const val CREATE_GAME = "page.create_game"
    const val GAME_DETAIL = "page.game_detail"
    const val SETTINGS = "page.settings"
    const val ACCOUNT_MANAGER = "page.account_manager"
    const val ADD_ACCOUNT = "page.add_account"
    const val EXTENSION_MANAGER = "page.extension_manager"
    const val ABOUT = "page.about"
}
