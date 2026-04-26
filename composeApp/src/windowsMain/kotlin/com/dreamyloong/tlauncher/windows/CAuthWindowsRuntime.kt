package com.dreamyloong.tlauncher.windows

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.dreamyloong.tlauncher.core.account.LauncherAccount
import com.dreamyloong.tlauncher.core.account.LauncherAccountLoginResult
import com.dreamyloong.tlauncher.core.account.LauncherAccountLoginStatus
import com.dreamyloong.tlauncher.core.account.LauncherAccountProvider
import com.dreamyloong.tlauncher.core.account.LauncherAccountService
import com.dreamyloong.tlauncher.core.account.SteamAccountLoginMode
import com.dreamyloong.tlauncher.core.account.SteamAccountLoginRequest
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.ptr.PointerByReference
import java.io.Closeable
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CAuthWindowsRuntime {
    data class Status(
        val available: Boolean,
        val version: String? = null,
        val message: String? = null,
    )

    private data class Libraries(
        val core: CAuthCoreLibrary,
        val steamAuth: CAuthSteamAuthLibrary,
    )

    private val librariesResult: Result<Libraries> by lazy {
        runCatching {
            val directory = extractRuntimeLibraries()
            val corePath = directory.resolve(CORE_LIBRARY_NAME)
            val authPath = directory.resolve(STEAM_AUTH_LIBRARY_NAME)

            System.load(corePath.toString())
            System.load(authPath.toString())

            val core = Native.load(corePath.toString(), CAuthCoreLibrary::class.java)
            val steamAuth = Native.load(authPath.toString(), CAuthSteamAuthLibrary::class.java)
            Libraries(core = core, steamAuth = steamAuth)
        }
    }

    private val statusResult: Result<Status> by lazy {
        runCatching {
            val libraries = librariesResult.getOrThrow()
            val capabilities = CAuthCapabilities()
            val capabilitiesResult = libraries.steamAuth.cauth_get_capabilities(capabilities)
            check(capabilitiesResult == 0) {
                "CAuth Steam auth capabilities check failed with result $capabilitiesResult."
            }

            val version = libraries.core.cauth_get_version()
            Status(
                available = true,
                version = version.text ?: "${version.major}.${version.minor}.${version.patch}",
            )
        }
    }

    fun status(): Status {
        return statusResult.getOrElse { error ->
            Status(
                available = false,
                message = error.message ?: error::class.java.simpleName,
            )
        }
    }

    fun createClient(): Pointer {
        val outClient = PointerByReference()
        val options = CAuthClientOptions().apply {
            sessionStorageKind = CAUTH_SESSION_STORAGE_DEFAULT
            write()
        }
        val result = libraries().core.cauth_client_create_with_options(options, outClient)
        check(result == CAUTH_OK) { "CAuth client creation failed with result $result." }
        return outClient.value ?: error("CAuth returned a null client.")
    }

    fun destroyClient(client: Pointer?) {
        if (client != null) {
            libraries().core.cauth_client_destroy(client)
        }
    }

    fun listAccounts(client: Pointer): List<LauncherAccount> {
        val sessionList = CAuthSessionList()
        val result = libraries().core.cauth_session_list_saved(client, sessionList)
        check(result == CAUTH_OK) { "CAuth account list failed with result $result." }
        sessionList.read()
        val sessionsPointer = sessionList.sessions ?: return emptyList()
        val recordSize = CAuthSessionRecord().size().toLong()
        val records = (0 until sessionList.count.toInt()).mapNotNull { index ->
            val record = CAuthSessionRecord(sessionsPointer.share(recordSize * index))
            record.read()
            record.toSteamSessionRecord()
        }
        return records
            .groupBy { it.subjectId }
            .values
            .map { sessions ->
                sessions.toLauncherAccount()
            }
            .sortedByDescending { it.createdAtUnixSeconds }
    }

    private data class SteamSessionRecord(
        val subjectId: String,
        val accountName: String?,
        val hasRefreshToken: Boolean,
        val hasAccessToken: Boolean,
        val createdAtUnixSeconds: Long,
        val loginMode: SteamAccountLoginMode?,
    )

    private fun List<SteamSessionRecord>.toLauncherAccount(): LauncherAccount {
        val newest = maxByOrNull { it.createdAtUnixSeconds } ?: error("Expected at least one Steam session.")
        return LauncherAccount(
            provider = LauncherAccountProvider.STEAM,
            subjectId = newest.subjectId,
            displayName = mapNotNull { it.accountName?.takeIf(String::isNotBlank) }
                .maxByOrNull { it.length }
                ?: newest.accountName,
            active = false,
            hasRefreshToken = any { it.hasRefreshToken },
            hasAccessToken = any { it.hasAccessToken },
            createdAtUnixSeconds = newest.createdAtUnixSeconds,
            loginModes = mapNotNull { it.loginMode }.toSet(),
        )
    }

    private fun inferLoginMode(
        refreshToken: String?,
        accessToken: String?,
    ): SteamAccountLoginMode? {
        return inferLoginModeFromToken(refreshToken)
            ?: inferLoginModeFromToken(accessToken)
    }

    private fun inferLoginModeFromToken(token: String?): SteamAccountLoginMode? {
        if (token.isNullOrBlank()) return null
        val payloadSegment = token.split('.').getOrNull(1) ?: return SteamAccountLoginMode.WEB
        val normalized = buildString(payloadSegment.length + 4) {
            payloadSegment.forEach { char ->
                append(
                    when (char) {
                        '-' -> '+'
                        '_' -> '/'
                        else -> char
                    },
                )
            }
            while (length % 4 != 0) {
                append('=')
            }
        }
        val decoded = runCatching {
            String(Base64.getDecoder().decode(normalized), Charsets.UTF_8)
        }.getOrNull() ?: return SteamAccountLoginMode.WEB
        val audience = runCatching { JSONObject(decoded).opt("aud") }.getOrNull()
        return when {
            audience.containsAudience("mobile") -> SteamAccountLoginMode.MOBILE
            audience.containsAudience("client") -> SteamAccountLoginMode.CM
            else -> SteamAccountLoginMode.WEB
        }
    }

    fun loginSteam(
        client: Pointer,
        request: SteamAccountLoginRequest,
    ): LauncherAccountLoginResult {
        val nativeRequest = CAuthLoginRequest().apply {
            accountName = request.accountName
            password = request.password
            steamGuardCode = request.steamGuardCode
            deviceName = "TLauncher"
            rememberSession = 1
            platformType = request.mode.toNativePlatformType()
            write()
        }
        val nativeResult = CAuthLoginResult()
        val result = libraries().steamAuth.cauth_auth_login_password(client, nativeRequest, nativeResult)
        nativeResult.read()
        val status = if (result == CAUTH_OK) {
            nativeResult.status.toLauncherLoginStatus()
        } else {
            LauncherAccountLoginStatus.FAILED
        }
        val account = if (status == LauncherAccountLoginStatus.SUCCEEDED && nativeResult.steamId != 0L) {
            LauncherAccount(
                provider = LauncherAccountProvider.STEAM,
                subjectId = nativeResult.steamId.toString(),
                displayName = nativeResult.accountName,
                active = true,
                hasRefreshToken = true,
                hasAccessToken = true,
                createdAtUnixSeconds = 0L,
                loginModes = setOf(request.mode),
            )
        } else {
            null
        }
        return LauncherAccountLoginResult(
            status = status,
            message = nativeResult.message?.ifBlank { null } ?: libraries().core.cauth_result_message(result),
            moduleStatus = nativeResult.moduleStatus?.orEmpty().orEmpty(),
            account = account,
        )
    }

    fun requestLoginCancel(client: Pointer) {
        val result = libraries().steamAuth.cauth_auth_request_login_cancel(client)
        check(result == CAUTH_OK) {
            "CAuth login cancel failed with result $result: ${libraries().core.cauth_result_message(result)}"
        }
    }

    fun deleteAccount(
        client: Pointer,
        provider: LauncherAccountProvider,
        subjectId: String,
    ) {
        val providerName = when (provider) {
            LauncherAccountProvider.STEAM -> "steam"
        }
        val result = libraries().core.cauth_session_clear_account(client, providerName, subjectId)
        check(result == CAUTH_OK) {
            "CAuth account delete failed with result $result: ${libraries().core.cauth_result_message(result)}"
        }
    }

    private fun libraries(): Libraries = librariesResult.getOrThrow()

    private fun extractRuntimeLibraries(): Path {
        val directory = Files.createTempDirectory("tlauncher-cauth-windows-")
        directory.toFile().deleteOnExit()

        listOf(CORE_LIBRARY_NAME, STEAM_AUTH_LIBRARY_NAME).forEach { name ->
            val target = directory.resolve(name)
            val resourcePath = "/cauth/windows/x64/$name"
            val input = CAuthWindowsRuntime::class.java.getResourceAsStream(resourcePath)
                ?: error("Missing CAuth Windows runtime resource: $resourcePath")
            input.use { stream ->
                Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING)
            }
            target.toFile().deleteOnExit()
        }

        return directory
    }

    private interface CAuthCoreLibrary : Library {
        fun cauth_get_version(): CAuthVersion.ByValue
        fun cauth_client_create_with_options(options: CAuthClientOptions, outClient: PointerByReference): Int
        fun cauth_client_destroy(client: Pointer)
        fun cauth_result_message(result: Int): String
        fun cauth_session_list_saved(client: Pointer, outSessions: CAuthSessionList): Int
        fun cauth_session_clear_account(client: Pointer, provider: String, subjectId: String): Int
    }

    private interface CAuthSteamAuthLibrary : Library {
        fun cauth_get_capabilities(outCapabilities: CAuthCapabilities): Int
        fun cauth_auth_login_password(
            client: Pointer,
            request: CAuthLoginRequest,
            outResult: CAuthLoginResult,
        ): Int
        fun cauth_auth_request_login_cancel(client: Pointer): Int
    }

    open class CAuthVersion : Structure() {
        @JvmField
        var major: Int = 0

        @JvmField
        var minor: Int = 0

        @JvmField
        var patch: Int = 0

        @JvmField
        var text: String? = null

        override fun getFieldOrder(): List<String> = listOf(
            "major",
            "minor",
            "patch",
            "text",
        )

        class ByValue : CAuthVersion(), Structure.ByValue
    }

    class CAuthCapabilities : Structure() {
        @JvmField
        var webApiAuthTransport: Int = 0

        @JvmField
        var cmWebsocketTransport: Int = 0

        @JvmField
        var passwordRsaEncryptor: Int = 0

        @JvmField
        var depotContentDecrypt: Int = 0

        @JvmField
        var androidSecureStoreBridge: Int = 0

        override fun getFieldOrder(): List<String> = listOf(
            "webApiAuthTransport",
            "cmWebsocketTransport",
            "passwordRsaEncryptor",
            "depotContentDecrypt",
            "androidSecureStoreBridge",
        )
    }

    class CAuthClientOptions : Structure() {
        @JvmField
        var sessionStorageKind: Int = CAUTH_SESSION_STORAGE_DEFAULT

        @JvmField
        var sessionStoragePath: String? = null

        @JvmField
        var sessionStorageNamespace: String? = null

        @JvmField
        var sessionStorageKey: String? = null

        override fun getFieldOrder(): List<String> = listOf(
            "sessionStorageKind",
            "sessionStoragePath",
            "sessionStorageNamespace",
            "sessionStorageKey",
        )
    }

    class CAuthSessionRecord() : Structure() {
        constructor(pointer: Pointer) : this() {
            useMemory(pointer)
        }

        @JvmField
        var present: Int = 0

        @JvmField
        var provider: String? = null

        @JvmField
        var subjectId: String? = null

        @JvmField
        var accountName: String? = null

        @JvmField
        var refreshToken: String? = null

        @JvmField
        var accessToken: String? = null

        @JvmField
        var hasRefreshToken: Int = 0

        @JvmField
        var hasAccessToken: Int = 0

        @JvmField
        var createdAtUnixSeconds: Long = 0

        override fun getFieldOrder(): List<String> = listOf(
            "present",
            "provider",
            "subjectId",
            "accountName",
            "refreshToken",
            "accessToken",
            "hasRefreshToken",
            "hasAccessToken",
            "createdAtUnixSeconds",
        )
    }

    class CAuthSessionList : Structure() {
        @JvmField
        var sessions: Pointer? = null

        @JvmField
        var count: Long = 0

        override fun getFieldOrder(): List<String> = listOf(
            "sessions",
            "count",
        )
    }

    class CAuthRouteSelection : Structure() {
        @JvmField
        var endpoint: String? = null

        @JvmField
        var protocol: String? = null

        @JvmField
        var role: String? = null

        override fun getFieldOrder(): List<String> = listOf(
            "endpoint",
            "protocol",
            "role",
        )
    }

    class CAuthLoginRequest : Structure() {
        @JvmField
        var accountName: String? = null

        @JvmField
        var password: String? = null

        @JvmField
        var steamGuardCode: String? = null

        @JvmField
        var deviceName: String? = null

        @JvmField
        var rememberSession: Int = 1

        @JvmField
        var platformType: Int = 0

        @JvmField
        var routeSelection: CAuthRouteSelection = CAuthRouteSelection()

        override fun getFieldOrder(): List<String> = listOf(
            "accountName",
            "password",
            "steamGuardCode",
            "deviceName",
            "rememberSession",
            "platformType",
            "routeSelection",
        )
    }

    class CAuthLoginResult : Structure() {
        @JvmField
        var status: Int = 2

        @JvmField
        var result: Int = CAUTH_ERROR_INTERNAL

        @JvmField
        var moduleStatus: String? = null

        @JvmField
        var message: String? = null

        @JvmField
        var steamId: Long = 0

        @JvmField
        var accountName: String? = null

        override fun getFieldOrder(): List<String> = listOf(
            "status",
            "result",
            "moduleStatus",
            "message",
            "steamId",
            "accountName",
        )
    }

    private fun CAuthSessionRecord.toSteamSessionRecord(): SteamSessionRecord? {
        if (present == 0) {
            return null
        }
        val providerName = provider.orEmpty()
        when (providerName.lowercase()) {
            "steam" -> Unit
            else -> return null
        }
        val subject = subjectId?.takeIf { it.isNotBlank() } ?: return null
        return SteamSessionRecord(
            subjectId = subject,
            accountName = accountName,
            hasRefreshToken = hasRefreshToken != 0,
            hasAccessToken = hasAccessToken != 0,
            createdAtUnixSeconds = createdAtUnixSeconds,
            loginMode = inferLoginMode(
                refreshToken = refreshToken,
                accessToken = accessToken,
            ),
        )
    }

    private fun SteamAccountLoginMode.toNativePlatformType(): Int {
        return when (this) {
            SteamAccountLoginMode.CM -> 0
            SteamAccountLoginMode.WEB -> 1
            SteamAccountLoginMode.MOBILE -> 2
        }
    }

    private fun Int.toLauncherLoginStatus(): LauncherAccountLoginStatus {
        return when (this) {
            0 -> LauncherAccountLoginStatus.SUCCEEDED
            1 -> LauncherAccountLoginStatus.STEAM_GUARD_REQUIRED
            4 -> LauncherAccountLoginStatus.CANCELED
            2 -> LauncherAccountLoginStatus.FAILED
            else -> LauncherAccountLoginStatus.UNSUPPORTED
        }
    }

    private const val CAUTH_OK = 0
    private const val CAUTH_ERROR_INTERNAL = 100
    private const val CAUTH_SESSION_STORAGE_DEFAULT = 0
    private const val CORE_LIBRARY_NAME = "cauth_core_ffi.dll"
    private const val STEAM_AUTH_LIBRARY_NAME = "cauth_steam_auth_ffi.dll"
}

private fun Any?.containsAudience(expected: String): Boolean {
    return when (this) {
        is String -> this == expected
        is JSONArray -> {
            for (index in 0 until length()) {
                if (optString(index) == expected) return true
            }
            false
        }

        else -> false
    }
}

class CAuthWindowsAccountService : LauncherAccountService, Closeable {
    private val client = CAuthWindowsRuntime.createClient()

    override val isSupported: Boolean = true

    override suspend fun listAccounts(): List<LauncherAccount> {
        return withContext(Dispatchers.IO) {
            CAuthWindowsRuntime.listAccounts(client)
        }
    }

    override suspend fun loginSteam(request: SteamAccountLoginRequest): LauncherAccountLoginResult {
        return withContext(Dispatchers.IO) {
            CAuthWindowsRuntime.loginSteam(client, request)
        }
    }

    override suspend fun cancelSteamLogin() {
        withContext(Dispatchers.IO) {
            CAuthWindowsRuntime.requestLoginCancel(client)
        }
    }

    override suspend fun deleteAccount(
        provider: LauncherAccountProvider,
        subjectId: String,
    ) {
        withContext(Dispatchers.IO) {
            CAuthWindowsRuntime.deleteAccount(client, provider, subjectId)
        }
    }

    override fun close() {
        CAuthWindowsRuntime.destroyClient(client)
    }
}

@Composable
fun rememberCAuthWindowsAccountService(): LauncherAccountService {
    val service = remember { CAuthWindowsAccountService() }
    DisposableEffect(service) {
        onDispose { service.close() }
    }
    return service
}
