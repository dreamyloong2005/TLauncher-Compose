package com.dreamyloong.tlauncher.sdk.account

enum class LauncherAccountProvider {
    STEAM,
}

enum class SteamAccountLoginMode {
    CM,
    WEB,
    MOBILE,
}

data class LauncherAccount(
    val provider: LauncherAccountProvider,
    val subjectId: String,
    val displayName: String?,
    val active: Boolean,
    val hasRefreshToken: Boolean,
    val hasAccessToken: Boolean,
    val createdAtUnixSeconds: Long,
    val loginModes: Set<SteamAccountLoginMode> = emptySet(),
)
