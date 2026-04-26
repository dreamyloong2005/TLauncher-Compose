package com.dreamyloong.tlauncher.account

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.cauth.android.CAuthClientOptions
import com.cauth.android.CAuthRouteSelection
import com.cauth.android.CAuthSessionStorageKind
import com.cauth.android.CAuthClient
import com.cauth.android.steam.depot.CAuthSteamDepotApi
import com.cauth.android.steam.depot.DepotDownloadTaskSnapshot
import com.cauth.android.steam.depot.DepotKeySnapshot
import com.cauth.android.steam.depot.DepotLocalVerifyEntrySnapshot
import com.cauth.android.steam.depot.DepotLocalVerifySnapshot
import com.cauth.android.steam.depot.DepotPreflightEntrySnapshot
import com.cauth.android.steam.depot.DepotPreflightSnapshot
import com.cauth.android.steam.depot.ManifestFileEntrySnapshot
import com.cauth.android.steam.depot.ManifestFileListSnapshot
import com.cauth.android.steam.depot.ManifestRequestCodeSnapshot
import com.cauth.android.steam.depot.steamDepot
import com.cauth.android.steam.auth.LoginPlatform
import com.cauth.android.steam.auth.LoginRequest
import com.cauth.android.steam.auth.LoginStatus
import com.cauth.android.steam.auth.SavedAccountSnapshot
import com.cauth.android.steam.auth.steamAuth
import com.dreamyloong.tlauncher.core.account.LauncherAccount
import com.dreamyloong.tlauncher.core.account.LauncherAccountLoginResult
import com.dreamyloong.tlauncher.core.account.LauncherAccountLoginStatus
import com.dreamyloong.tlauncher.core.account.LauncherAccountProvider
import com.dreamyloong.tlauncher.core.account.LauncherAccountService
import com.dreamyloong.tlauncher.core.account.SteamAccountLoginMode
import com.dreamyloong.tlauncher.core.account.SteamAccountLoginRequest
import com.dreamyloong.tlauncher.sdk.host.ExtensionHostServices
import com.dreamyloong.tlauncher.sdk.host.SteamDepotKey
import com.dreamyloong.tlauncher.sdk.host.SteamDepotLocalVerifyEntry
import com.dreamyloong.tlauncher.sdk.host.SteamDepotLocalVerifyResult
import com.dreamyloong.tlauncher.sdk.host.SteamDepotPreflight
import com.dreamyloong.tlauncher.sdk.host.SteamDepotPreflightEntry
import com.dreamyloong.tlauncher.sdk.host.SteamDepotService
import com.dreamyloong.tlauncher.sdk.host.SteamDepotTaskSnapshot
import com.dreamyloong.tlauncher.sdk.host.SteamManifestFileEntry
import com.dreamyloong.tlauncher.sdk.host.SteamManifestFileList
import com.dreamyloong.tlauncher.sdk.host.SteamManifestRequestCode
import java.io.Closeable

class AndroidCAuthAccountService(
    context: Context,
) : LauncherAccountService, Closeable {
    private val sessionTypeStore = CAuthAndroidSessionTypeStore(context.applicationContext)
    private val client = CAuthClient.create(
        CAuthClientOptions(
            sessionStorageKind = CAuthSessionStorageKind.Default,
            sessionStorageNamespace = CAUTH_SECURE_STORE_PREFERENCES,
            sessionStorageKey = CAUTH_SESSION_STORE_KEY,
        ),
    )
    private val steamAuth = client.steamAuth()

    override val isSupported: Boolean = true

    override suspend fun listAccounts(): List<LauncherAccount> {
        val loginModesBySteamId = sessionTypeStore.readSteamLoginModesBySteamId()
        return steamAuth.listSavedAccounts().map { account ->
            account.toLauncherAccount(
                loginModes = loginModesBySteamId[account.steamId].orEmpty(),
            )
        }
    }

    override suspend fun loginSteam(request: SteamAccountLoginRequest): LauncherAccountLoginResult {
        val result = steamAuth.loginPassword(
            LoginRequest(
                accountName = request.accountName,
                password = request.password,
                steamGuardCode = request.steamGuardCode?.trim()?.takeUnless { it.isBlank() },
                deviceName = "TLauncher",
                rememberSession = true,
                platform = request.mode.toCAuthLoginPlatform(),
                routeSelection = CAuthRouteSelection(),
            ),
        )
        val status = result.status.toLauncherStatus()
        val account = if (status == LauncherAccountLoginStatus.SUCCEEDED && result.steamId != 0L) {
            val loginModes = sessionTypeStore.readSteamLoginModes(result.steamId)
                .ifEmpty { setOf(request.mode) }
            LauncherAccount(
                provider = LauncherAccountProvider.STEAM,
                subjectId = result.steamId.toString(),
                displayName = result.accountName,
                active = true,
                hasRefreshToken = true,
                hasAccessToken = true,
                createdAtUnixSeconds = 0L,
                loginModes = loginModes,
            )
        } else {
            null
        }
        return LauncherAccountLoginResult(
            status = status,
            message = result.message.ifBlank {
                result.moduleStatus.ifBlank { status.name }
            },
            moduleStatus = result.moduleStatus,
            account = account,
        )
    }

    override suspend fun cancelSteamLogin() {
        steamAuth.requestLoginCancel()
    }

    override suspend fun deleteAccount(
        provider: LauncherAccountProvider,
        subjectId: String,
    ) {
        require(provider == LauncherAccountProvider.STEAM) {
            "Unsupported account provider: $provider"
        }
        val steamId = subjectId.toLongOrNull()
            ?: error("Invalid Steam account id: $subjectId")
        steamAuth.clearSavedAccount(steamId)
    }

    override fun close() {
        client.close()
    }
}

@Composable
fun rememberAndroidCAuthAccountService(): LauncherAccountService {
    val context = LocalContext.current
    val service = remember(context) { AndroidCAuthAccountService(context) }
    DisposableEffect(service) {
        onDispose { service.close() }
    }
    return service
}

class AndroidCAuthHostServices(
    context: Context,
) : ExtensionHostServices, Closeable {
    private val client = CAuthClient.create(
        CAuthClientOptions(
            sessionStorageKind = CAuthSessionStorageKind.Default,
            sessionStorageNamespace = CAUTH_SECURE_STORE_PREFERENCES,
            sessionStorageKey = CAUTH_SESSION_STORE_KEY,
        ),
    )

    override val steamDepot: SteamDepotService = AndroidCAuthSteamDepotService(client.steamDepot())

    override fun close() {
        client.close()
    }
}

@Composable
fun rememberAndroidCAuthHostServices(): ExtensionHostServices {
    val context = LocalContext.current
    val services = remember(context) { AndroidCAuthHostServices(context) }
    DisposableEffect(services) {
        onDispose { services.close() }
    }
    return services
}

private class AndroidCAuthSteamDepotService(
    private val delegate: CAuthSteamDepotApi,
) : SteamDepotService {
    override suspend fun fetchPreflight(
        steamId: Long,
        appId: Int,
        branch: String,
        maxCount: Int,
    ): SteamDepotPreflight {
        return delegate.fetchPreflight(
            steamId = steamId,
            appId = appId,
            branch = branch,
            maxCount = maxCount,
        ).toSdk()
    }

    override suspend fun fetchDepotKey(
        steamId: Long,
        appId: Int,
        depotId: Int,
        maxCount: Int,
    ): SteamDepotKey {
        return delegate.fetchDepotKey(
            steamId = steamId,
            appId = appId,
            depotId = depotId,
            maxCount = maxCount,
        ).toSdk()
    }

    override suspend fun fetchManifestRequestCode(
        steamId: Long,
        appId: Int,
        depotId: Int,
        manifestGid: Long,
        branch: String,
        branchPasswordHash: String,
        maxCount: Int,
    ): SteamManifestRequestCode {
        return delegate.fetchManifestRequestCode(
            steamId = steamId,
            appId = appId,
            depotId = depotId,
            manifestGid = manifestGid,
            branch = branch,
            branchPasswordHash = branchPasswordHash,
            maxCount = maxCount,
        ).toSdk()
    }

    override suspend fun downloadManifest(
        depotId: Int,
        manifestGid: Long,
        requestCode: Long,
        outputPath: String,
        maxCount: Int,
    ) {
        delegate.downloadManifest(
            depotId = depotId,
            manifestGid = manifestGid,
            requestCode = requestCode,
            outputPath = outputPath,
            maxCount = maxCount,
        )
    }

    override suspend fun listManifestFiles(
        inputPath: String,
        depotKeyHex: String,
        filterText: String,
        limit: Int,
    ): SteamManifestFileList {
        return delegate.listManifestFiles(
            inputPath = inputPath,
            depotKeyHex = depotKeyHex,
            filterText = filterText,
            limit = limit,
        ).toSdk()
    }

    override suspend fun startVerifyLocalFiles(
        inputPath: String,
        localRoot: String,
        depotKeyHex: String,
        filterText: String?,
    ): Long {
        return delegate.startVerifyLocalFiles(
            inputPath = inputPath,
            localRoot = localRoot,
            depotKeyHex = depotKeyHex,
            filterText = filterText,
        )
    }

    override suspend fun startFileDownload(
        inputPath: String,
        outputPath: String,
        depotKeyHex: String,
        filePath: String,
        fileIndex: Long?,
        maxCount: Int,
    ): Long {
        return delegate.startFileDownload(
            inputPath = inputPath,
            outputPath = outputPath,
            depotKeyHex = depotKeyHex,
            filePath = filePath,
            fileIndex = fileIndex,
            maxCount = maxCount,
        )
    }

    override suspend fun pollTask(handle: Long): SteamDepotTaskSnapshot {
        return delegate.pollDownloadTask(handle).toSdk()
    }

    override suspend fun pauseTask(handle: Long) {
        delegate.pauseDownloadTask(handle)
    }

    override suspend fun cancelTask(handle: Long) {
        delegate.cancelDownloadTask(handle)
    }

    override suspend fun disposeTask(handle: Long) {
        delegate.disposeDownloadTask(handle)
    }
}

private class CAuthAndroidSessionTypeStore(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(
        CAUTH_SECURE_STORE_PREFERENCES,
        Context.MODE_PRIVATE,
    )

    fun readSteamLoginModes(steamId: Long): Set<SteamAccountLoginMode> {
        return readSteamLoginModesBySteamId()[steamId].orEmpty()
    }

    fun readSteamLoginModesBySteamId(): Map<Long, Set<SteamAccountLoginMode>> {
        val sessions = readSessions()
        val modesBySteamId = linkedMapOf<Long, MutableSet<SteamAccountLoginMode>>()
        sessions.forEach { session ->
            if (session.provider.lowercase() != STEAM_PROVIDER) return@forEach
            val steamId = session.subjectId.toLongOrNull() ?: return@forEach
            val mode = session.toSteamAccountLoginMode() ?: return@forEach
            modesBySteamId.getOrPut(steamId) { linkedSetOf() }.add(mode)
        }
        return modesBySteamId.mapValues { (_, modes) -> modes.toSet() }
    }

    private fun readSessions(): List<CAuthStoredSession> {
        val hex = preferences.getString(CAUTH_SESSION_STORE_KEY, null) ?: return emptyList()
        val bytes = hexToBytes(hex) ?: return emptyList()
        val reader = CAuthSessionStoreReader(bytes)
        return when {
            reader.consumeMagic(CAUTH_STORE_V2_MAGIC) -> reader.readStoreV2()
            reader.consumeMagic(CAUTH_SESSION_V3_MAGIC) -> listOfNotNull(reader.readSingleSessionV3())
            else -> emptyList()
        }
    }
}

private class CAuthSessionStoreReader(
    private val bytes: ByteArray,
) {
    private var offset = 0

    fun consumeMagic(magic: ByteArray): Boolean {
        if (bytes.size < magic.size) return false
        for (index in magic.indices) {
            if (bytes[index] != magic[index]) return false
        }
        offset = magic.size
        return true
    }

    fun readStoreV2(): List<CAuthStoredSession> {
        return readSessionList()
    }

    fun readSingleSessionV3(): CAuthStoredSession? {
        return readSession()
            ?.takeIf { offset == bytes.size }
    }

    private fun readSessionList(): List<CAuthStoredSession> {
        val count = readU32() ?: return emptyList()
        if (count > MAX_STORED_SESSIONS) return emptyList()

        val sessions = mutableListOf<CAuthStoredSession>()
        repeat(count) {
            sessions += readSession() ?: return emptyList()
        }
        return if (offset == bytes.size) sessions else emptyList()
    }

    private fun readSession(): CAuthStoredSession? {
        readU64() ?: return null
        return CAuthStoredSession(
            provider = readString() ?: return null,
            subjectId = readString() ?: return null,
            accountName = readString() ?: return null,
            refreshToken = readString() ?: return null,
            accessToken = readString() ?: return null,
            sessionType = readString() ?: return null,
        )
    }

    private fun readU32(): Int? {
        if (bytes.size - offset < 4) return null
        var value = 0
        repeat(4) { shiftIndex ->
            value = value or ((bytes[offset++].toInt() and 0xff) shl (shiftIndex * 8))
        }
        return value
    }

    private fun readU64(): Long? {
        if (bytes.size - offset < 8) return null
        var value = 0L
        repeat(8) { shiftIndex ->
            value = value or ((bytes[offset++].toLong() and 0xffL) shl (shiftIndex * 8))
        }
        return value
    }

    private fun readString(): String? {
        val length = readU32() ?: return null
        if (length < 0 || bytes.size - offset < length) return null
        return String(bytes, offset, length, Charsets.UTF_8).also {
            offset += length
        }
    }
}

private data class CAuthStoredSession(
    val provider: String,
    val subjectId: String,
    val accountName: String,
    val refreshToken: String,
    val accessToken: String,
    val sessionType: String,
)

private fun SavedAccountSnapshot.toLauncherAccount(
    loginModes: Set<SteamAccountLoginMode>,
): LauncherAccount {
    return LauncherAccount(
        provider = LauncherAccountProvider.STEAM,
        subjectId = steamId.toString(),
        displayName = accountName,
        active = false,
        hasRefreshToken = hasRefreshToken,
        hasAccessToken = hasAccessToken,
        createdAtUnixSeconds = createdAtUnixSeconds,
        loginModes = loginModes,
    )
}

private fun CAuthStoredSession.toSteamAccountLoginMode(): SteamAccountLoginMode? {
    return when (sessionType) {
        STEAM_SESSION_TYPE_STEAM_CLIENT -> SteamAccountLoginMode.CM
        STEAM_SESSION_TYPE_WEB_BROWSER -> SteamAccountLoginMode.WEB
        STEAM_SESSION_TYPE_MOBILE_APP -> SteamAccountLoginMode.MOBILE
        else -> null
    }
}

private fun hexToBytes(hex: String): ByteArray? {
    if (hex.length % 2 != 0) return null
    val output = ByteArray(hex.length / 2)
    for (index in output.indices) {
        val high = hex[index * 2].hexValue()
        val low = hex[index * 2 + 1].hexValue()
        if (high < 0 || low < 0) return null
        output[index] = ((high shl 4) or low).toByte()
    }
    return output
}

private fun Char.hexValue(): Int {
    return when (this) {
        in '0'..'9' -> this - '0'
        in 'a'..'f' -> this - 'a' + 10
        in 'A'..'F' -> this - 'A' + 10
        else -> -1
    }
}

private fun SteamAccountLoginMode.toCAuthLoginPlatform(): LoginPlatform {
    return when (this) {
        SteamAccountLoginMode.CM -> LoginPlatform.SteamClient
        SteamAccountLoginMode.WEB -> LoginPlatform.WebBrowser
        SteamAccountLoginMode.MOBILE -> LoginPlatform.MobileApp
    }
}

private fun LoginStatus.toLauncherStatus(): LauncherAccountLoginStatus {
    return when (this) {
        LoginStatus.Succeeded -> LauncherAccountLoginStatus.SUCCEEDED
        LoginStatus.SteamGuardRequired -> LauncherAccountLoginStatus.STEAM_GUARD_REQUIRED
        LoginStatus.Canceled -> LauncherAccountLoginStatus.CANCELED
        LoginStatus.Failed -> LauncherAccountLoginStatus.FAILED
        LoginStatus.Unsupported -> LauncherAccountLoginStatus.UNSUPPORTED
    }
}

private fun DepotPreflightSnapshot.toSdk(): SteamDepotPreflight {
    return SteamDepotPreflight(
        present = present,
        appId = appId,
        branch = branch,
        buildId = buildId,
        depots = depots.map { it.toSdk() },
    )
}

private fun DepotPreflightEntrySnapshot.toSdk(): SteamDepotPreflightEntry {
    return SteamDepotPreflightEntry(
        depotId = depotId,
        manifestGid = manifestGid,
        size = size,
        downloadSize = downloadSize,
        encrypted = encrypted,
        platformLabel = platformLabel,
        osList = osList,
        osArch = osArch,
        depotFromApp = depotFromApp,
        sharedInstall = sharedInstall,
        accessStatus = accessStatus,
        keyEresult = keyEresult,
        keyAvailable = keyAvailable,
    )
}

private fun DepotKeySnapshot.toSdk(): SteamDepotKey {
    return SteamDepotKey(
        present = present,
        depotId = depotId,
        eresult = eresult,
        keyHex = keyHex,
    )
}

private fun ManifestRequestCodeSnapshot.toSdk(): SteamManifestRequestCode {
    return SteamManifestRequestCode(
        present = present,
        requestCode = requestCode,
    )
}

private fun ManifestFileListSnapshot.toSdk(): SteamManifestFileList {
    return SteamManifestFileList(
        present = present,
        matchedCount = matchedCount,
        printedCount = printedCount,
        totalCount = totalCount,
        files = files.map { it.toSdk() },
    )
}

private fun ManifestFileEntrySnapshot.toSdk(): SteamManifestFileEntry {
    return SteamManifestFileEntry(
        filename = filename,
        flags = flags,
        size = size,
        chunkCount = chunkCount,
    )
}

private fun DepotDownloadTaskSnapshot.toSdk(): SteamDepotTaskSnapshot {
    return SteamDepotTaskSnapshot(
        handle = handle,
        kindCode = kindCode,
        active = active,
        finished = finished,
        canceled = canceled,
        paused = paused,
        succeeded = succeeded,
        moduleStatus = moduleStatus,
        phase = phase,
        completedSteps = completedSteps,
        totalSteps = totalSteps,
        completedBytes = completedBytes,
        totalBytes = totalBytes,
        target = target,
        message = message,
        verifyResult = verifyResult?.toSdk(),
        kindLabel = kindLabel,
        progressFraction = progressFraction,
        progressSummary = progressSummary,
    )
}

private fun DepotLocalVerifySnapshot.toSdk(): SteamDepotLocalVerifyResult {
    return SteamDepotLocalVerifyResult(
        present = present,
        clean = clean,
        moduleStatus = moduleStatus,
        checkedCount = checkedCount,
        okCount = okCount,
        missingCount = missingCount,
        mismatchedCount = mismatchedCount,
        sizeOnlyCount = sizeOnlyCount,
        filteredOutCount = filteredOutCount,
        totalCount = totalCount,
        entries = entries.map { it.toSdk() },
    )
}

private fun DepotLocalVerifyEntrySnapshot.toSdk(): SteamDepotLocalVerifyEntry {
    return SteamDepotLocalVerifyEntry(
        manifestFilename = manifestFilename,
        localPath = localPath,
        statusCode = statusCode,
        expectedSize = expectedSize,
        actualSize = actualSize,
        expectedShaHex = expectedShaHex,
        actualShaHex = actualShaHex,
        reason = reason,
        statusLabel = statusLabel,
    )
}

private val CAUTH_STORE_V2_MAGIC = "CAUTHACCT2\r\n".toByteArray(Charsets.UTF_8)
private val CAUTH_SESSION_V3_MAGIC = "CAUTHSESS3\r\n".toByteArray(Charsets.UTF_8)
private const val CAUTH_SECURE_STORE_PREFERENCES = "cauth_secure_store"
private const val CAUTH_SESSION_STORE_KEY = "auth_session_v1"
private const val MAX_STORED_SESSIONS = 1024
private const val STEAM_PROVIDER = "steam"
private const val STEAM_SESSION_TYPE_STEAM_CLIENT = "steam-client"
private const val STEAM_SESSION_TYPE_WEB_BROWSER = "web-browser"
private const val STEAM_SESSION_TYPE_MOBILE_APP = "mobile-app"
