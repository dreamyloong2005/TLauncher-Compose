package com.dreamyloong.tlauncher.core.account

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

data class SteamAccountLoginRequest(
    val accountName: String,
    val password: String,
    val steamGuardCode: String?,
    val mode: SteamAccountLoginMode,
)

enum class LauncherAccountLoginStatus {
    SUCCEEDED,
    STEAM_GUARD_REQUIRED,
    CANCELED,
    FAILED,
    UNSUPPORTED,
}

data class LauncherAccountLoginResult(
    val status: LauncherAccountLoginStatus,
    val message: String,
    val moduleStatus: String = "",
    val account: LauncherAccount? = null,
)

interface LauncherAccountService {
    val isSupported: Boolean

    suspend fun listAccounts(): List<LauncherAccount>

    suspend fun loginSteam(request: SteamAccountLoginRequest): LauncherAccountLoginResult

    suspend fun cancelSteamLogin()

    suspend fun deleteAccount(
        provider: LauncherAccountProvider,
        subjectId: String,
    )
}

object UnsupportedLauncherAccountService : LauncherAccountService {
    override val isSupported: Boolean = false

    override suspend fun listAccounts(): List<LauncherAccount> = emptyList()

    override suspend fun loginSteam(request: SteamAccountLoginRequest): LauncherAccountLoginResult {
        return LauncherAccountLoginResult(
            status = LauncherAccountLoginStatus.UNSUPPORTED,
            message = "Account management is not available on this platform.",
        )
    }

    override suspend fun cancelSteamLogin() = Unit

    override suspend fun deleteAccount(
        provider: LauncherAccountProvider,
        subjectId: String,
    ) = Unit
}
