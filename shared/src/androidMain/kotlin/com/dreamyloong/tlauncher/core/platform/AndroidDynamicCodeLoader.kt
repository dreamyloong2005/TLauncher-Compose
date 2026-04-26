package com.dreamyloong.tlauncher.core.platform

import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun writeManagedResourceFile(
    directory: File,
    fileName: String,
    bytes: ByteArray,
): File {
    directory.mkdirs()
    val targetFile = directory.resolve(fileName)
    if (targetFile.exists() && targetFile.length() == bytes.size.toLong()) {
        val existingBytes = runCatching { targetFile.readBytes() }.getOrNull()
        if (existingBytes != null && existingBytes.contentEquals(bytes)) {
            return targetFile
        }
    }

    val tempPrefix = targetFile.nameWithoutExtension.ifBlank { "resource" }
        .takeIf { it.length >= 3 }
        ?: (targetFile.name.filter { it.isLetterOrDigit() }.ifBlank { "res" } + "___").take(3)
    val tempFile = File.createTempFile(tempPrefix, ".tmp", directory)
    try {
        FileOutputStream(tempFile).use { output ->
            output.write(bytes)
            output.fd.sync()
        }
        if (targetFile.exists()) {
            targetFile.setWritable(true, true)
            if (!targetFile.delete()) {
                throw IOException("Unable to replace existing file: ${targetFile.absolutePath}")
            }
        }
        if (!tempFile.renameTo(targetFile)) {
            tempFile.copyTo(targetFile, overwrite = true)
        }
        return targetFile
    } finally {
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }
}

fun prepareReadOnlyDynamicCodeFile(
    directory: File,
    fileName: String,
    bytes: ByteArray,
): File {
    val targetFile = writeManagedResourceFile(
        directory = directory,
        fileName = fileName,
        bytes = bytes,
    )
    targetFile.setReadable(true, false)
    targetFile.setExecutable(false, false)
    if (!targetFile.setWritable(false, false) && targetFile.canWrite()) {
        throw IOException("Unable to mark dynamic code file read-only: ${targetFile.absolutePath}")
    }
    return targetFile
}
