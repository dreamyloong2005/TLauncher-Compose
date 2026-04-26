package com.dreamyloong.tlauncher.core.extension

import java.io.File
import java.util.zip.ZipFile

class ZipExtensionPackageSource(
    val file: File,
) : ExtensionPackageSource {
    override val sourceName: String
        get() = file.name

    override fun exists(path: String): Boolean {
        ZipFile(file).use { zip ->
            return zip.getEntry(path) != null
        }
    }

    override fun list(path: String): List<String> {
        val prefix = normalizedZipDirectoryPrefix(path)
        ZipFile(file).use { zip ->
            return zip.entries().asSequence()
                .map { entry -> entry.name }
                .filter { entryName -> entryName.startsWith(prefix) && entryName != prefix }
                .map { entryName -> entryName.removePrefix(prefix).substringBefore('/') }
                .filter(String::isNotBlank)
                .distinct()
                .sorted()
                .toList()
        }
    }

    override fun readUtf8(path: String): String? {
        ZipFile(file).use { zip ->
            val entry = zip.getEntry(path) ?: return null
            zip.getInputStream(entry).use { input ->
                return input.readBytes().toString(Charsets.UTF_8)
            }
        }
    }

    override fun readBytes(path: String): ByteArray? {
        ZipFile(file).use { zip ->
            val entry = zip.getEntry(path) ?: return null
            zip.getInputStream(entry).use { input ->
                return input.readBytes()
            }
        }
    }
}

private fun normalizedZipDirectoryPrefix(path: String): String {
    val trimmed = path.trim().trim('/')
    return if (trimmed.isBlank()) "" else "$trimmed/"
}
