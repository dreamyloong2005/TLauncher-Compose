package com.dreamyloong.tlauncher.core.extension

import com.dreamyloong.tlauncher.core.model.ExtensionManifest

data class ExtensionCompatibility(
    val packageFormatVersion: Int = ExtensionSdkContract.PACKAGE_FORMAT_VERSION,
    val sdkApiVersion: Int = ExtensionSdkContract.SDK_API_VERSION,
    val minSdkApiVersion: Int = sdkApiVersion,
    val targetSdkApiVersion: Int = sdkApiVersion,
)

data class HostSdkDescriptor(
    val packageFormatVersion: Int,
    val sdkApiVersion: Int,
    val minSupportedSdkApiVersion: Int = ExtensionSdkContract.MIN_SUPPORTED_SDK_API_VERSION,
)

object ExtensionSdkContract {
    const val PACKAGE_FORMAT_VERSION: Int = 1
    const val MIN_SUPPORTED_SDK_API_VERSION: Int = 1
    const val SDK_API_VERSION: Int = 1
    const val SDK_VERSION: String = "1.0.0"

    fun hostDescriptor(): HostSdkDescriptor {
        return HostSdkDescriptor(
            packageFormatVersion = PACKAGE_FORMAT_VERSION,
            sdkApiVersion = SDK_API_VERSION,
            minSupportedSdkApiVersion = MIN_SUPPORTED_SDK_API_VERSION,
        )
    }
}

enum class ExtensionCompatibilityIssue {
    PACKAGE_FORMAT_VERSION_MISMATCH,
    SDK_API_VERSION_MISMATCH,
    SDK_API_VERSION_RANGE_INVALID,
    MIN_SDK_API_VERSION_TOO_NEW,
    TARGET_SDK_API_VERSION_TOO_OLD,
}

sealed interface ExtensionCompatibilityResult {
    val issues: List<ExtensionCompatibilityIssue>
    val isCompatible: Boolean

    data class Compatible(
        override val issues: List<ExtensionCompatibilityIssue> = emptyList(),
    ) : ExtensionCompatibilityResult {
        override val isCompatible: Boolean = true
    }

    data class Incompatible(
        override val issues: List<ExtensionCompatibilityIssue>,
    ) : ExtensionCompatibilityResult {
        override val isCompatible: Boolean = false
    }
}

interface ExtensionCompatibilityChecker {
    fun check(
        manifest: ExtensionManifest,
        host: HostSdkDescriptor,
    ): ExtensionCompatibilityResult
}

object DefaultExtensionCompatibilityChecker : ExtensionCompatibilityChecker {
    override fun check(
        manifest: ExtensionManifest,
        host: HostSdkDescriptor,
    ): ExtensionCompatibilityResult {
        val compatibility = manifest.compatibility
        val issues = buildList {
            if (compatibility.packageFormatVersion != host.packageFormatVersion) {
                add(ExtensionCompatibilityIssue.PACKAGE_FORMAT_VERSION_MISMATCH)
            }
            if (
                compatibility.sdkApiVersion < 1 ||
                compatibility.minSdkApiVersion < 1 ||
                compatibility.targetSdkApiVersion < 1 ||
                compatibility.minSdkApiVersion > compatibility.targetSdkApiVersion
            ) {
                add(ExtensionCompatibilityIssue.SDK_API_VERSION_RANGE_INVALID)
            }
            if (compatibility.minSdkApiVersion > host.sdkApiVersion) {
                add(ExtensionCompatibilityIssue.MIN_SDK_API_VERSION_TOO_NEW)
            }
            if (compatibility.targetSdkApiVersion < host.minSupportedSdkApiVersion) {
                add(ExtensionCompatibilityIssue.TARGET_SDK_API_VERSION_TOO_OLD)
            }
        }
        if (issues.isNotEmpty()) {
            return ExtensionCompatibilityResult.Incompatible(issues)
        }
        return ExtensionCompatibilityResult.Compatible()
    }
}
