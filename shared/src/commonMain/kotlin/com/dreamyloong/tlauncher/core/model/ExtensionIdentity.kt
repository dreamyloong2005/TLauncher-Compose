package com.dreamyloong.tlauncher.core.model

private val extensionIdSegmentPattern = Regex("^[a-z0-9][a-z0-9_-]*$")

data class ResolvedExtensionIdentity(
    val registrationId: String,
    val identityId: String,
    val kind: ExtensionKind,
    val targetQualifier: PlatformTarget? = null,
)

object ExtensionIdentity {
    fun resolve(
        registrationId: String,
        kind: ExtensionKind,
    ): ResolvedExtensionIdentity {
        val normalizedId = registrationId.trim()
        require(normalizedId.isNotEmpty()) { "Extension registration id must not be blank." }

        val segments = normalizedId.split('.')
        require(segments.size >= 3) {
            "Extension registration id must follow kind.author.name[.target]: $normalizedId"
        }
        require(segments.none(String::isBlank)) {
            "Extension registration id contains blank segments: $normalizedId"
        }
        require(segments.all { segment -> extensionIdSegmentPattern.matches(segment) }) {
            "Extension registration id contains invalid segments: $normalizedId"
        }

        val expectedKindSegment = kind.name.lowercase()
        require(segments.first() == expectedKindSegment) {
            "Extension registration id $normalizedId must start with $expectedKindSegment."
        }

        val targetQualifier = PlatformTarget.entries.firstOrNull { target ->
            target.name.lowercase() == segments.last()
        }
        val identitySegments = if (targetQualifier != null) segments.dropLast(1) else segments
        require(identitySegments.size >= 3) {
            "Extension identity id must contain kind.author.name: $normalizedId"
        }

        return ResolvedExtensionIdentity(
            registrationId = normalizedId,
            identityId = identitySegments.joinToString("."),
            kind = kind,
            targetQualifier = targetQualifier,
        )
    }
}
