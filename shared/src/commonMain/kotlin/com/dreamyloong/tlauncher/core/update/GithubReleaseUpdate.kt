package com.dreamyloong.tlauncher.core.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

enum class AppUpdateCheckStatus {
    Idle,
    Checking,
    UpToDate,
    InternalBuild,
    UpdateAvailable,
    Failed,
}

data class GithubReleaseInfo(
    val tagName: String,
    val name: String,
    val htmlUrl: String,
)

data class AppUpdateCheckState(
    val status: AppUpdateCheckStatus = AppUpdateCheckStatus.Idle,
    val release: GithubReleaseInfo? = null,
)

class GithubReleaseUpdateChecker(
    private val owner: String,
    private val repository: String,
) {
    suspend fun check(currentVersion: String): AppUpdateCheckState {
        val payload = platformHttpGetText(
            url = "https://api.github.com/repos/$owner/$repository/releases/latest",
            headers = mapOf(
                "Accept" to "application/vnd.github+json",
                "X-GitHub-Api-Version" to "2022-11-28",
                "User-Agent" to "TLauncher/$currentVersion",
            ),
        ) ?: return AppUpdateCheckState(status = AppUpdateCheckStatus.Failed)

        val latestRelease = runCatching {
            json.decodeFromString<GithubLatestReleasePayload>(payload)
        }.getOrNull() ?: return AppUpdateCheckState(status = AppUpdateCheckStatus.Failed)

        val release = GithubReleaseInfo(
            tagName = latestRelease.tagName,
            name = latestRelease.name,
            htmlUrl = latestRelease.htmlUrl,
        )
        val versionComparison = compareVersions(currentVersion, release.tagName)
        return AppUpdateCheckState(
            status = when {
                versionComparison == 0 -> AppUpdateCheckStatus.UpToDate
                versionComparison > 0 -> AppUpdateCheckStatus.InternalBuild
                else -> AppUpdateCheckStatus.UpdateAvailable
            },
            release = release,
        )
    }

    fun releasesPageUrl(): String {
        return "https://github.com/$owner/$repository/releases"
    }
}

expect suspend fun platformHttpGetText(
    url: String,
    headers: Map<String, String> = emptyMap(),
): String?

@Serializable
private data class GithubLatestReleasePayload(
    @SerialName("tag_name")
    val tagName: String,
    val name: String = "",
    @SerialName("html_url")
    val htmlUrl: String,
)

private val json = Json {
    ignoreUnknownKeys = true
}

private fun normalizeVersion(raw: String): String {
    return raw.trim()
        .removePrefix("v")
        .removePrefix("V")
}

private fun compareVersions(currentVersion: String, latestVersion: String): Int {
    return ParsedVersion.parse(currentVersion).compareTo(ParsedVersion.parse(latestVersion))
}

private data class ParsedVersion(
    val releaseParts: List<VersionIdentifier>,
    val preReleaseParts: List<VersionIdentifier>,
) : Comparable<ParsedVersion> {
    override fun compareTo(other: ParsedVersion): Int {
        val releaseSize = maxOf(releaseParts.size, other.releaseParts.size)
        repeat(releaseSize) { index ->
            val left = releaseParts.getOrNull(index) ?: VersionIdentifier.Zero
            val right = other.releaseParts.getOrNull(index) ?: VersionIdentifier.Zero
            val comparison = left.compareTo(right)
            if (comparison != 0) {
                return comparison
            }
        }

        if (preReleaseParts.isEmpty() && other.preReleaseParts.isEmpty()) {
            return 0
        }
        if (preReleaseParts.isEmpty()) {
            return 1
        }
        if (other.preReleaseParts.isEmpty()) {
            return -1
        }

        val preReleaseSize = maxOf(preReleaseParts.size, other.preReleaseParts.size)
        repeat(preReleaseSize) { index ->
            val left = preReleaseParts.getOrNull(index)
            val right = other.preReleaseParts.getOrNull(index)
            if (left == null) {
                return -1
            }
            if (right == null) {
                return 1
            }
            val comparison = left.compareTo(right)
            if (comparison != 0) {
                return comparison
            }
        }
        return 0
    }

    companion object {
        fun parse(raw: String): ParsedVersion {
            val normalized = normalizeVersion(raw).substringBefore("+")
            val releaseText = normalized.substringBefore("-")
            val preReleaseText = normalized.substringAfter("-", missingDelimiterValue = "")
            return ParsedVersion(
                releaseParts = releaseText
                    .split('.', '_')
                    .filter { it.isNotBlank() }
                    .map(VersionIdentifier::parse),
                preReleaseParts = preReleaseText
                    .split('.', '-', '_')
                    .filter { it.isNotBlank() }
                    .map(VersionIdentifier::parse),
            )
        }
    }
}

private data class VersionIdentifier(
    val numericValue: Long?,
    val textValue: String,
) : Comparable<VersionIdentifier> {
    override fun compareTo(other: VersionIdentifier): Int {
        return when {
            numericValue != null && other.numericValue != null -> numericValue.compareTo(other.numericValue)
            numericValue != null -> 1
            other.numericValue != null -> -1
            else -> textValue.compareTo(other.textValue)
        }
    }

    companion object {
        val Zero = VersionIdentifier(0, "0")

        fun parse(raw: String): VersionIdentifier {
            return VersionIdentifier(
                numericValue = raw.toLongOrNull(),
                textValue = raw.lowercase(),
            )
        }
    }
}
