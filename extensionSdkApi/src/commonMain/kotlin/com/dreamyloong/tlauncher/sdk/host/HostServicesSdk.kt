package com.dreamyloong.tlauncher.sdk.host

interface ExtensionHostServices {
    val steamDepot: SteamDepotService?
}

object EmptyExtensionHostServices : ExtensionHostServices {
    override val steamDepot: SteamDepotService? = null
}

interface SteamDepotService {
    suspend fun fetchPreflight(
        steamId: Long,
        appId: Int,
        branch: String,
        maxCount: Int,
    ): SteamDepotPreflight

    suspend fun fetchDepotKey(
        steamId: Long,
        appId: Int,
        depotId: Int,
        maxCount: Int,
    ): SteamDepotKey

    suspend fun fetchManifestRequestCode(
        steamId: Long,
        appId: Int,
        depotId: Int,
        manifestGid: Long,
        branch: String,
        branchPasswordHash: String,
        maxCount: Int,
    ): SteamManifestRequestCode

    suspend fun downloadManifest(
        depotId: Int,
        manifestGid: Long,
        requestCode: Long,
        outputPath: String,
        maxCount: Int,
    )

    suspend fun listManifestFiles(
        inputPath: String,
        depotKeyHex: String,
        filterText: String,
        limit: Int,
    ): SteamManifestFileList

    suspend fun startVerifyLocalFiles(
        inputPath: String,
        localRoot: String,
        depotKeyHex: String,
        filterText: String? = null,
    ): Long

    suspend fun startFileDownload(
        inputPath: String,
        outputPath: String,
        depotKeyHex: String,
        filePath: String,
        fileIndex: Long? = null,
        maxCount: Int,
    ): Long

    suspend fun pollTask(handle: Long): SteamDepotTaskSnapshot

    suspend fun pauseTask(handle: Long)

    suspend fun cancelTask(handle: Long)

    suspend fun disposeTask(handle: Long)
}

data class SteamDepotPreflight(
    val present: Boolean,
    val appId: Int = 0,
    val branch: String = "",
    val buildId: String = "",
    val depots: List<SteamDepotPreflightEntry> = emptyList(),
)

data class SteamDepotPreflightEntry(
    val depotId: Int,
    val manifestGid: Long,
    val size: Long = 0L,
    val downloadSize: Long = 0L,
    val encrypted: Boolean = false,
    val platformLabel: String? = null,
    val osList: String = "",
    val osArch: String = "",
    val depotFromApp: String = "",
    val sharedInstall: Boolean = false,
    val accessStatus: String = "",
    val keyEresult: Int = 0,
    val keyAvailable: Boolean = false,
)

data class SteamDepotKey(
    val present: Boolean,
    val depotId: Int = 0,
    val eresult: Int = 0,
    val keyHex: String = "",
)

data class SteamManifestRequestCode(
    val present: Boolean,
    val requestCode: Long = 0L,
)

data class SteamManifestFileList(
    val present: Boolean,
    val matchedCount: Long = 0L,
    val printedCount: Long = 0L,
    val totalCount: Long = 0L,
    val files: List<SteamManifestFileEntry> = emptyList(),
)

data class SteamManifestFileEntry(
    val filename: String,
    val flags: Int = 0,
    val size: Long = 0L,
    val chunkCount: Long = 0L,
)

data class SteamDepotTaskSnapshot(
    val handle: Long = 0L,
    val kindCode: Int = 0,
    val active: Boolean = false,
    val finished: Boolean = false,
    val canceled: Boolean = false,
    val paused: Boolean = false,
    val succeeded: Boolean = false,
    val moduleStatus: String = "",
    val phase: String = "",
    val completedSteps: Long = 0L,
    val totalSteps: Long = 0L,
    val completedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val target: String = "",
    val message: String = "",
    val verifyResult: SteamDepotLocalVerifyResult? = null,
    val kindLabel: String = "",
    val progressFraction: Float? = null,
    val progressSummary: String = "",
)

data class SteamDepotLocalVerifyResult(
    val present: Boolean = false,
    val clean: Boolean = false,
    val moduleStatus: String = "",
    val checkedCount: Long = 0L,
    val okCount: Long = 0L,
    val missingCount: Long = 0L,
    val mismatchedCount: Long = 0L,
    val sizeOnlyCount: Long = 0L,
    val filteredOutCount: Long = 0L,
    val totalCount: Long = 0L,
    val entries: List<SteamDepotLocalVerifyEntry> = emptyList(),
)

data class SteamDepotLocalVerifyEntry(
    val manifestFilename: String = "",
    val localPath: String = "",
    val statusCode: Int = 0,
    val expectedSize: Long = 0L,
    val actualSize: Long = 0L,
    val expectedShaHex: String = "",
    val actualShaHex: String = "",
    val reason: String = "",
    val statusLabel: String = "",
)
