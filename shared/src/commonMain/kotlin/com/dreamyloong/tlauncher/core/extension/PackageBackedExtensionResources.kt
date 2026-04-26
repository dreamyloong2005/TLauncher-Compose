package com.dreamyloong.tlauncher.core.extension

class PackageBackedExtensionResources(
    private val source: ExtensionPackageSource,
) : ExtensionPackageResources {
    override fun exists(path: String): Boolean {
        return source.exists(resolvePath(path))
    }

    override fun list(path: String): List<String> {
        return source.list(resolvePath(path))
    }

    override fun readUtf8(path: String): String? {
        return source.readUtf8(resolvePath(path))
    }

    override fun readBytes(path: String): ByteArray? {
        return source.readBytes(resolvePath(path))
    }

    private fun resolvePath(path: String): String {
        val trimmedPath = path.trim()
        require(trimmedPath.isNotEmpty()) { "Extension resource path must not be blank." }
        require(!trimmedPath.startsWith("/") && !trimmedPath.startsWith("\\") && ':' !in trimmedPath) {
            "Extension resource path must be relative to the package resources root."
        }
        require(trimmedPath.split('/', '\\').none { segment -> segment == ".." }) {
            "Extension resource path must not escape the package resources root."
        }
        return "$RESOURCE_ROOT/$trimmedPath"
    }

    companion object {
        const val RESOURCE_ROOT: String = "resources"
    }
}
