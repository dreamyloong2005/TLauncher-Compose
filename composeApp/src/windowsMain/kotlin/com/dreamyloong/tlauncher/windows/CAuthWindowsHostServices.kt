package com.dreamyloong.tlauncher.windows

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
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
import com.sun.jna.Library
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import java.io.Closeable
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CAuthWindowsHostServices : ExtensionHostServices, Closeable {
    private val client = CAuthWindowsRuntime.createClient()

    override val steamDepot: SteamDepotService = CAuthWindowsSteamDepotService(client)

    override fun close() {
        CAuthWindowsRuntime.destroyClient(client)
    }
}

@Composable
fun rememberCAuthWindowsHostServices(): ExtensionHostServices {
    val services = remember { CAuthWindowsHostServices() }
    DisposableEffect(services) {
        onDispose { services.close() }
    }
    return services
}

private class CAuthWindowsSteamDepotService(
    private val client: Pointer,
) : SteamDepotService {
    override suspend fun fetchPreflight(
        steamId: Long,
        appId: Int,
        branch: String,
        maxCount: Int,
    ): SteamDepotPreflight = withContext(Dispatchers.IO) {
        CAuthWindowsDepotRuntime.fetchPreflight(client, steamId, appId, branch, maxCount)
    }

    override suspend fun fetchDepotKey(
        steamId: Long,
        appId: Int,
        depotId: Int,
        maxCount: Int,
    ): SteamDepotKey = withContext(Dispatchers.IO) {
        CAuthWindowsDepotRuntime.fetchDepotKey(client, steamId, appId, depotId, maxCount)
    }

    override suspend fun fetchManifestRequestCode(
        steamId: Long,
        appId: Int,
        depotId: Int,
        manifestGid: Long,
        branch: String,
        branchPasswordHash: String,
        maxCount: Int,
    ): SteamManifestRequestCode = withContext(Dispatchers.IO) {
        CAuthWindowsDepotRuntime.fetchManifestRequestCode(
            client = client,
            steamId = steamId,
            appId = appId,
            depotId = depotId,
            manifestGid = manifestGid,
            branch = branch,
            branchPasswordHash = branchPasswordHash,
            maxCount = maxCount,
        )
    }

    override suspend fun downloadManifest(
        depotId: Int,
        manifestGid: Long,
        requestCode: Long,
        outputPath: String,
        maxCount: Int,
    ) {
        withContext(Dispatchers.IO) {
            CAuthWindowsDepotRuntime.downloadManifest(
                depotId = depotId,
                manifestGid = manifestGid,
                requestCode = requestCode,
                outputPath = outputPath,
                maxCount = maxCount,
            )
        }
    }

    override suspend fun listManifestFiles(
        inputPath: String,
        depotKeyHex: String,
        filterText: String,
        limit: Int,
    ): SteamManifestFileList = withContext(Dispatchers.IO) {
        CAuthWindowsDepotRuntime.listManifestFiles(inputPath, depotKeyHex, filterText, limit)
    }

    override suspend fun startVerifyLocalFiles(
        inputPath: String,
        localRoot: String,
        depotKeyHex: String,
        filterText: String?,
    ): Long = withContext(Dispatchers.IO) {
        CAuthWindowsDepotRuntime.startVerifyLocalFiles(
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
    ): Long = withContext(Dispatchers.IO) {
        CAuthWindowsDepotRuntime.startFileDownload(
            inputPath = inputPath,
            outputPath = outputPath,
            depotKeyHex = depotKeyHex,
            filePath = filePath,
            fileIndex = fileIndex,
            maxCount = maxCount,
        )
    }

    override suspend fun pollTask(handle: Long): SteamDepotTaskSnapshot = withContext(Dispatchers.IO) {
        CAuthWindowsDepotRuntime.pollTask(handle)
    }

    override suspend fun pauseTask(handle: Long) {
        withContext(Dispatchers.IO) {
            CAuthWindowsDepotRuntime.pauseTask(handle)
        }
    }

    override suspend fun cancelTask(handle: Long) {
        withContext(Dispatchers.IO) {
            CAuthWindowsDepotRuntime.cancelTask(handle)
        }
    }

    override suspend fun disposeTask(handle: Long) {
        withContext(Dispatchers.IO) {
            CAuthWindowsDepotRuntime.disposeTask(handle)
        }
    }
}

private object CAuthWindowsDepotRuntime {
    private val libraryResult: Result<CAuthWindowsSteamDepotLibrary> by lazy {
        runCatching {
            val directory = Files.createTempDirectory("tlauncher-cauth-windows-depot-")
            directory.toFile().deleteOnExit()
            val target = directory.resolve(STEAM_DEPOT_LIBRARY_NAME)
            val resourcePath = "/cauth/windows/x64/$STEAM_DEPOT_LIBRARY_NAME"
            val input = CAuthWindowsDepotRuntime::class.java.getResourceAsStream(resourcePath)
                ?: error("Missing CAuth Windows depot runtime resource: $resourcePath")
            input.use { stream ->
                Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING)
            }
            target.toFile().deleteOnExit()
            System.load(target.toString())
            Native.load(target.toString(), CAuthWindowsSteamDepotLibrary::class.java)
        }
    }

    fun fetchPreflight(
        client: Pointer,
        steamId: Long,
        appId: Int,
        branch: String,
        maxCount: Int,
    ): SteamDepotPreflight {
        val outResponse = CAuthWindowsDepotPreflightReport()
        requireOk(
            library().cauth_depot_fetch_preflight(client, steamId, appId, branch, maxCount, outResponse),
            "Steam depot preflight failed.",
        )
        outResponse.read()
        return outResponse.toSdk()
    }

    fun fetchDepotKey(
        client: Pointer,
        steamId: Long,
        appId: Int,
        depotId: Int,
        maxCount: Int,
    ): SteamDepotKey {
        val outResponse = CAuthWindowsDepotKeyResponse()
        requireOk(
            library().cauth_depot_fetch_key(client, steamId, appId, depotId, maxCount, outResponse),
            "Steam depot key request failed.",
        )
        outResponse.read()
        return SteamDepotKey(
            present = outResponse.present != 0,
            eresult = outResponse.eresult,
            depotId = outResponse.depotId,
            keyHex = outResponse.keyHex.orEmpty(),
        )
    }

    fun fetchManifestRequestCode(
        client: Pointer,
        steamId: Long,
        appId: Int,
        depotId: Int,
        manifestGid: Long,
        branch: String,
        branchPasswordHash: String,
        maxCount: Int,
    ): SteamManifestRequestCode {
        val outResponse = CAuthWindowsManifestRequestCodeResponse()
        requireOk(
            library().cauth_depot_fetch_manifest_request_code(
                client,
                steamId,
                appId,
                depotId,
                manifestGid,
                branch,
                branchPasswordHash,
                maxCount,
                outResponse,
            ),
            "Steam manifest request code request failed.",
        )
        outResponse.read()
        return SteamManifestRequestCode(
            present = outResponse.present != 0,
            requestCode = outResponse.manifestRequestCode,
        )
    }

    fun downloadManifest(
        depotId: Int,
        manifestGid: Long,
        requestCode: Long,
        outputPath: String,
        maxCount: Int,
    ) {
        requireOk(
            library().cauth_depot_download_manifest(
                depotId,
                manifestGid,
                requestCode,
                maxCount,
                outputPath,
                CAUTH_FILE_WRITE_OVERWRITE,
                1,
            ),
            "Steam manifest download failed.",
        )
    }

    fun listManifestFiles(
        inputPath: String,
        depotKeyHex: String,
        filterText: String,
        limit: Int,
    ): SteamManifestFileList {
        val outResponse = CAuthWindowsManifestFileList()
        requireOk(
            library().cauth_depot_list_manifest_files(inputPath, depotKeyHex, filterText, limit, outResponse),
            "Reading the manifest file list failed.",
        )
        outResponse.read()
        return outResponse.toSdk()
    }

    fun startVerifyLocalFiles(
        inputPath: String,
        localRoot: String,
        depotKeyHex: String,
        filterText: String?,
    ): Long {
        val outHandle = Memory(Native.LONG_SIZE.toLong())
        requireOk(
            library().cauth_depot_start_verify_local_files(
                inputPath,
                depotKeyHex,
                localRoot,
                filterText,
                outHandle,
            ),
            "Starting the Steam local verification task failed.",
        )
        return readNativeLong(outHandle)
    }

    fun startFileDownload(
        inputPath: String,
        outputPath: String,
        depotKeyHex: String,
        filePath: String,
        fileIndex: Long?,
        maxCount: Int,
    ): Long {
        val outHandle = Memory(Native.LONG_SIZE.toLong())
        requireOk(
            library().cauth_depot_start_download_file(
                inputPath,
                depotKeyHex,
                filePath,
                fileIndex ?: 0L,
                if (fileIndex != null) 1 else 0,
                maxCount,
                outputPath,
                null,
                CAUTH_FILE_WRITE_OVERWRITE,
                1,
                outHandle,
            ),
            "Starting the Steam file download task failed.",
        )
        return readNativeLong(outHandle)
    }

    fun pollTask(handle: Long): SteamDepotTaskSnapshot {
        val outSnapshot = CAuthWindowsDepotTaskSnapshot()
        requireOk(
            library().cauth_depot_poll_task(handle, outSnapshot),
            "Polling the Steam depot task failed.",
        )
        outSnapshot.read()
        return outSnapshot.toSdk()
    }

    fun cancelTask(handle: Long) {
        library().cauth_depot_cancel_task(handle)
    }

    fun pauseTask(handle: Long) {
        library().cauth_depot_pause_task(handle)
    }

    fun disposeTask(handle: Long) {
        library().cauth_depot_dispose_task(handle)
    }

    private fun library(): CAuthWindowsSteamDepotLibrary = libraryResult.getOrThrow()

    private fun buildErrorMessage(
        result: Int,
        fallback: String,
    ): String {
        val detail = runCatching { library().cauth_depot_last_error_detail() }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
        return if (detail != null) "$fallback (code=$result, detail=$detail)" else "$fallback (code=$result)"
    }

    private fun requireOk(
        result: Int,
        fallback: String,
    ) {
        if (result != CAUTH_OK) {
            error(buildErrorMessage(result, fallback))
        }
    }

    private fun readNativeLong(memory: Memory): Long {
        return if (Native.LONG_SIZE == 8) memory.getLong(0) else memory.getInt(0).toLong()
    }

    private const val CAUTH_OK = 0
    private const val CAUTH_FILE_WRITE_OVERWRITE = 0
    private const val STEAM_DEPOT_LIBRARY_NAME = "cauth_steam_depot_ffi.dll"
}

private interface CAuthWindowsSteamDepotLibrary : Library {
    fun cauth_depot_fetch_preflight(
        client: Pointer,
        steamId: Long,
        appId: Int,
        branch: String,
        maxCount: Int,
        outResponse: CAuthWindowsDepotPreflightReport,
    ): Int

    fun cauth_depot_fetch_key(
        client: Pointer,
        steamId: Long,
        appId: Int,
        depotId: Int,
        maxCount: Int,
        outResponse: CAuthWindowsDepotKeyResponse,
    ): Int

    fun cauth_depot_fetch_manifest_request_code(
        client: Pointer,
        steamId: Long,
        appId: Int,
        depotId: Int,
        manifestGid: Long,
        branch: String,
        branchPasswordHash: String,
        maxCount: Int,
        outResponse: CAuthWindowsManifestRequestCodeResponse,
    ): Int

    fun cauth_depot_download_manifest(
        depotId: Int,
        manifestGid: Long,
        requestCode: Long,
        maxCount: Int,
        outputPath: String,
        writeMode: Int,
        atomicWrite: Int,
    ): Int

    fun cauth_depot_list_manifest_files(
        inputPath: String,
        depotKeyHex: String,
        filterText: String,
        limit: Int,
        outResponse: CAuthWindowsManifestFileList,
    ): Int

    fun cauth_depot_start_verify_local_files(
        inputPath: String,
        depotKeyHex: String,
        localRoot: String,
        filterText: String?,
        outHandle: Pointer,
    ): Int

    fun cauth_depot_start_download_file(
        inputPath: String,
        depotKeyHex: String,
        filePath: String,
        fileIndex: Long,
        hasFileIndex: Int,
        maxCount: Int,
        outputPath: String,
        routeSelection: Pointer?,
        writeMode: Int,
        atomicWrite: Int,
        outHandle: Pointer,
    ): Int

    fun cauth_depot_poll_task(
        handle: Long,
        outSnapshot: CAuthWindowsDepotTaskSnapshot,
    ): Int

    fun cauth_depot_cancel_task(handle: Long)

    fun cauth_depot_pause_task(handle: Long)

    fun cauth_depot_dispose_task(handle: Long)

    fun cauth_depot_last_error_detail(): String?
}

class CAuthWindowsDepotKeyResponse : Structure() {
    @JvmField var present: Int = 0
    @JvmField var eresult: Int = 0
    @JvmField var depotId: Int = 0
    @JvmField var keyHex: String? = null

    override fun getFieldOrder(): List<String> = listOf("present", "eresult", "depotId", "keyHex")
}

class CAuthWindowsDepotPreflightEntry() : Structure() {
    constructor(pointer: Pointer) : this() {
        useMemory(pointer)
    }

    @JvmField var depotId: Int = 0
    @JvmField var manifestGid: Long = 0
    @JvmField var size: Long = 0
    @JvmField var downloadSize: Long = 0
    @JvmField var encrypted: Int = 0
    @JvmField var platformLabel: String? = null
    @JvmField var osList: String? = null
    @JvmField var osArch: String? = null
    @JvmField var depotFromApp: String? = null
    @JvmField var sharedInstall: Int = 0
    @JvmField var accessStatus: String? = null
    @JvmField var keyEresult: Int = 0
    @JvmField var keyAvailable: Int = 0

    override fun getFieldOrder(): List<String> = listOf(
        "depotId",
        "manifestGid",
        "size",
        "downloadSize",
        "encrypted",
        "platformLabel",
        "osList",
        "osArch",
        "depotFromApp",
        "sharedInstall",
        "accessStatus",
        "keyEresult",
        "keyAvailable",
    )
}

class CAuthWindowsDepotPreflightReport : Structure() {
    @JvmField var present: Int = 0
    @JvmField var appId: Int = 0
    @JvmField var branch: String? = null
    @JvmField var buildId: String? = null
    @JvmField var depotCount: Long = 0
    @JvmField var depots: Pointer? = null

    override fun getFieldOrder(): List<String> = listOf("present", "appId", "branch", "buildId", "depotCount", "depots")
}

class CAuthWindowsManifestFileEntry() : Structure() {
    constructor(pointer: Pointer) : this() {
        useMemory(pointer)
    }

    @JvmField var filename: String? = null
    @JvmField var flags: Int = 0
    @JvmField var size: Long = 0
    @JvmField var chunkCount: Long = 0

    override fun getFieldOrder(): List<String> = listOf("filename", "flags", "size", "chunkCount")
}

class CAuthWindowsManifestFileList : Structure() {
    @JvmField var present: Int = 0
    @JvmField var matchedCount: Long = 0
    @JvmField var printedCount: Long = 0
    @JvmField var totalCount: Long = 0
    @JvmField var files: Pointer? = null

    override fun getFieldOrder(): List<String> = listOf("present", "matchedCount", "printedCount", "totalCount", "files")
}

class CAuthWindowsManifestRequestCodeResponse : Structure() {
    @JvmField var present: Int = 0
    @JvmField var manifestRequestCode: Long = 0

    override fun getFieldOrder(): List<String> = listOf("present", "manifestRequestCode")
}

class CAuthWindowsDepotLocalVerifyEntry() : Structure() {
    constructor(pointer: Pointer) : this() {
        useMemory(pointer)
    }

    @JvmField var manifestFilename: String? = null
    @JvmField var localPath: String? = null
    @JvmField var status: Int = 0
    @JvmField var expectedSize: Long = 0
    @JvmField var actualSize: Long = 0
    @JvmField var expectedShaHex: String? = null
    @JvmField var actualShaHex: String? = null
    @JvmField var reason: String? = null

    override fun getFieldOrder(): List<String> = listOf(
        "manifestFilename",
        "localPath",
        "status",
        "expectedSize",
        "actualSize",
        "expectedShaHex",
        "actualShaHex",
        "reason",
    )
}

class CAuthWindowsDepotLocalVerifyReport : Structure() {
    @JvmField var present: Int = 0
    @JvmField var clean: Int = 0
    @JvmField var moduleStatus: String? = null
    @JvmField var checkedCount: Long = 0
    @JvmField var okCount: Long = 0
    @JvmField var missingCount: Long = 0
    @JvmField var mismatchedCount: Long = 0
    @JvmField var sizeOnlyCount: Long = 0
    @JvmField var filteredOutCount: Long = 0
    @JvmField var totalCount: Long = 0
    @JvmField var entryCount: Long = 0
    @JvmField var entries: Pointer? = null

    override fun getFieldOrder(): List<String> = listOf(
        "present",
        "clean",
        "moduleStatus",
        "checkedCount",
        "okCount",
        "missingCount",
        "mismatchedCount",
        "sizeOnlyCount",
        "filteredOutCount",
        "totalCount",
        "entryCount",
        "entries",
    )
}

class CAuthWindowsDepotTaskSnapshot : Structure() {
    @JvmField var handle: Long = 0
    @JvmField var active: Int = 0
    @JvmField var succeeded: Int = 0
    @JvmField var canceled: Int = 0
    @JvmField var paused: Int = 0
    @JvmField var kind: Int = 0
    @JvmField var moduleStatus: String? = null
    @JvmField var phase: String? = null
    @JvmField var target: String? = null
    @JvmField var message: String? = null
    @JvmField var completedSteps: Long = 0
    @JvmField var totalSteps: Long = 0
    @JvmField var completedBytes: Long = 0
    @JvmField var totalBytes: Long = 0
    @JvmField var hasVerifyReport: Int = 0
    @JvmField var verifyReport: CAuthWindowsDepotLocalVerifyReport = CAuthWindowsDepotLocalVerifyReport()

    override fun getFieldOrder(): List<String> = listOf(
        "handle",
        "active",
        "succeeded",
        "canceled",
        "paused",
        "kind",
        "moduleStatus",
        "phase",
        "target",
        "message",
        "completedSteps",
        "totalSteps",
        "completedBytes",
        "totalBytes",
        "hasVerifyReport",
        "verifyReport",
    )
}

private fun CAuthWindowsDepotPreflightReport.toSdk(): SteamDepotPreflight {
    val entrySize = CAuthWindowsDepotPreflightEntry().size().toLong()
    val depotEntries = depots?.let { entriesPointer ->
        (0 until depotCount.toInt()).map { index ->
            CAuthWindowsDepotPreflightEntry(entriesPointer.share(entrySize * index)).apply { read() }.toSdk()
        }
    }.orEmpty()
    return SteamDepotPreflight(
        present = present != 0,
        appId = appId,
        branch = branch.orEmpty(),
        buildId = buildId.orEmpty(),
        depots = depotEntries,
    )
}

private fun CAuthWindowsDepotPreflightEntry.toSdk(): SteamDepotPreflightEntry {
    return SteamDepotPreflightEntry(
        depotId = depotId,
        manifestGid = manifestGid,
        size = size,
        downloadSize = downloadSize,
        encrypted = encrypted != 0,
        platformLabel = platformLabel,
        osList = osList.orEmpty(),
        osArch = osArch.orEmpty(),
        depotFromApp = depotFromApp.orEmpty(),
        sharedInstall = sharedInstall != 0,
        accessStatus = accessStatus.orEmpty(),
        keyEresult = keyEresult,
        keyAvailable = keyAvailable != 0,
    )
}

private fun CAuthWindowsManifestFileList.toSdk(): SteamManifestFileList {
    val entrySize = CAuthWindowsManifestFileEntry().size().toLong()
    val manifestFiles = files?.let { filesPointer ->
        (0 until printedCount.toInt()).map { index ->
            CAuthWindowsManifestFileEntry(filesPointer.share(entrySize * index)).apply { read() }.toSdk()
        }
    }.orEmpty()
    return SteamManifestFileList(
        present = present != 0,
        matchedCount = matchedCount,
        printedCount = printedCount,
        totalCount = totalCount,
        files = manifestFiles,
    )
}

private fun CAuthWindowsManifestFileEntry.toSdk(): SteamManifestFileEntry {
    return SteamManifestFileEntry(
        filename = filename.orEmpty(),
        flags = flags,
        size = size,
        chunkCount = chunkCount,
    )
}

private fun CAuthWindowsDepotLocalVerifyReport.toSdk(): SteamDepotLocalVerifyResult {
    val entrySize = CAuthWindowsDepotLocalVerifyEntry().size().toLong()
    val verifyEntries = entries?.let { entriesPointer ->
        (0 until entryCount.toInt()).map { index ->
            CAuthWindowsDepotLocalVerifyEntry(entriesPointer.share(entrySize * index)).apply { read() }.toSdk()
        }
    }.orEmpty()
    return SteamDepotLocalVerifyResult(
        present = present != 0,
        clean = clean != 0,
        moduleStatus = moduleStatus.orEmpty(),
        checkedCount = checkedCount,
        okCount = okCount,
        missingCount = missingCount,
        mismatchedCount = mismatchedCount,
        sizeOnlyCount = sizeOnlyCount,
        filteredOutCount = filteredOutCount,
        totalCount = totalCount,
        entries = verifyEntries,
    )
}

private fun CAuthWindowsDepotLocalVerifyEntry.toSdk(): SteamDepotLocalVerifyEntry {
    return SteamDepotLocalVerifyEntry(
        manifestFilename = manifestFilename.orEmpty(),
        localPath = localPath.orEmpty(),
        statusCode = status,
        expectedSize = expectedSize,
        actualSize = actualSize,
        expectedShaHex = expectedShaHex.orEmpty(),
        actualShaHex = actualShaHex.orEmpty(),
        reason = reason.orEmpty(),
        statusLabel = reason.orEmpty(),
    )
}

private fun CAuthWindowsDepotTaskSnapshot.toSdk(): SteamDepotTaskSnapshot {
    val activeValue = active != 0
    val canceledValue = canceled != 0
    val succeededValue = succeeded != 0
    val pausedValue = paused != 0
    return SteamDepotTaskSnapshot(
        handle = handle,
        kindCode = kind,
        active = activeValue,
        finished = !activeValue || succeededValue || canceledValue,
        canceled = canceledValue,
        paused = pausedValue,
        succeeded = succeededValue,
        moduleStatus = moduleStatus.orEmpty(),
        phase = phase.orEmpty(),
        completedSteps = completedSteps,
        totalSteps = totalSteps,
        completedBytes = completedBytes,
        totalBytes = totalBytes,
        target = target.orEmpty(),
        message = message.orEmpty(),
        verifyResult = verifyReport.takeIf { hasVerifyReport != 0 }?.toSdk(),
        kindLabel = depotTaskKindLabel(kind),
        progressFraction = depotTaskProgressFraction(),
        progressSummary = "",
    )
}

private fun CAuthWindowsDepotTaskSnapshot.depotTaskProgressFraction(): Float? {
    if (totalBytes > 0L) {
        return (completedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
    }
    if (totalSteps > 0L) {
        return (completedSteps.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)
    }
    return null
}

private fun depotTaskKindLabel(kind: Int): String {
    return when (kind) {
        1 -> "verify"
        2 -> "download"
        else -> ""
    }
}
