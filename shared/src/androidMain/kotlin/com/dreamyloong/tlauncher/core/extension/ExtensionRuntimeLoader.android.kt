package com.dreamyloong.tlauncher.core.extension

import com.dreamyloong.tlauncher.core.model.PlatformTarget
import com.dreamyloong.tlauncher.core.platform.prepareReadOnlyDynamicCodeFile
import com.dreamyloong.tlauncher.core.platform.requireAndroidLauncherContext
import com.dreamyloong.tlauncher.sdk.extension.ExtensionEntrypoint as SdkExtensionEntrypoint
import com.dreamyloong.tlauncher.sdk.extension.LauncherExtension as SdkLauncherExtension
import dalvik.system.DexClassLoader
import java.io.File

actual object ExtensionRuntimeLoader {
    actual fun load(
        parsedPackage: ParsedExtensionPackage,
        source: ExtensionPackageSource,
        target: PlatformTarget,
    ): LoadedExtensionRuntime {
        val packageManifest = parsedPackage.manifest
        val artifactPath = packageManifest.runtimeArtifacts[target]
            ?: throw ExtensionRuntimeLoadException(
                "No runtime artifact declared for $target in ${parsedPackage.sourceName}.",
            )
        val entrypointClassName = packageManifest.entrypoints[target]
            ?: throw ExtensionRuntimeLoadException(
                "No entrypoint declared for $target in ${parsedPackage.sourceName}.",
            )
        val artifactBytes = source.readBytes(artifactPath)
            ?: throw ExtensionRuntimeLoadException(
                "Unable to read runtime artifact from ${parsedPackage.sourceName}: $artifactPath",
            )
        val extractedRuntime = extractRuntimeArtifact(
            sourceName = parsedPackage.sourceName,
            artifactPath = artifactPath,
            artifactBytes = artifactBytes,
        )
        val context = requireAndroidLauncherContext()
        val optimizedDirectory = context.codeCacheDir.resolve("textension-odex").apply { mkdirs() }
        val nativeLibraryDirectory = context.codeCacheDir.resolve("textension-native").apply { mkdirs() }
        val classLoader = DexClassLoader(
            extractedRuntime.absolutePath,
            optimizedDirectory.absolutePath,
            nativeLibraryDirectory.absolutePath,
            SdkExtensionEntrypoint::class.java.classLoader,
        )
        val entrypoint = instantiateEntrypoint(classLoader, entrypointClassName)
        val runtimeExtension = try {
            entrypoint.createExtension()
        } catch (error: Throwable) {
            throw ExtensionRuntimeLoadException(
                "Entrypoint $entrypointClassName failed to create extension for ${parsedPackage.sourceName}: ${error.message}",
                error,
            )
        }
        validateRuntimeExtension(packageManifest, runtimeExtension)
        return LoadedExtensionRuntime(
            extension = runtimeExtension.toCoreLauncherExtension(),
            hostGrants = emptySet(),
            packageResources = PackageBackedExtensionResources(source),
        )
    }

    private fun extractRuntimeArtifact(
        sourceName: String,
        artifactPath: String,
        artifactBytes: ByteArray,
    ): File {
        val context = requireAndroidLauncherContext()
        val extractionDirectory = context.codeCacheDir.resolve("textension-runtime").apply { mkdirs() }
        val sanitizedSourceName = sourceName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val fileName = artifactPath.substringAfterLast('/').ifBlank { "$sanitizedSourceName-runtime.jar" }
        return prepareReadOnlyDynamicCodeFile(
            directory = extractionDirectory,
            fileName = "$sanitizedSourceName-$fileName",
            bytes = artifactBytes,
        )
    }

    private fun instantiateEntrypoint(
        classLoader: ClassLoader,
        entrypointClassName: String,
    ): SdkExtensionEntrypoint {
        val entrypointClass = try {
            classLoader.loadClass(entrypointClassName)
        } catch (error: Throwable) {
            throw ExtensionRuntimeLoadException(
                "Unable to load entrypoint class $entrypointClassName: ${error.message}",
                error,
            )
        }
        if (!SdkExtensionEntrypoint::class.java.isAssignableFrom(entrypointClass)) {
            throw ExtensionRuntimeLoadException(
                "Entrypoint class $entrypointClassName does not implement ${SdkExtensionEntrypoint::class.java.name}.",
            )
        }

        @Suppress("UNCHECKED_CAST")
        val typedClass = entrypointClass as Class<out SdkExtensionEntrypoint>
        val singletonInstance = runCatching {
            typedClass.getDeclaredField("INSTANCE").apply {
                isAccessible = true
            }.get(null)
        }.getOrNull()
        if (singletonInstance is SdkExtensionEntrypoint) {
            return singletonInstance
        }

        return try {
            typedClass.getDeclaredConstructor().apply {
                isAccessible = true
            }.newInstance()
        } catch (error: Throwable) {
            throw ExtensionRuntimeLoadException(
                "Entrypoint class $entrypointClassName must expose a no-arg constructor or Kotlin object INSTANCE.",
                error,
            )
        }
    }

    private fun validateRuntimeExtension(
        packageManifest: ExtensionPackageManifest,
        runtimeExtension: SdkLauncherExtension,
    ) {
        val expected = packageManifest.extension
        val actual = runtimeExtension.extension
        if (actual.id != expected.id) {
            throw ExtensionRuntimeLoadException(
                "Runtime extension id ${actual.id} does not match packaged manifest id ${expected.id}.",
            )
        }
        if (actual.kind.name != expected.kind.name) {
            throw ExtensionRuntimeLoadException(
                "Runtime extension kind ${actual.kind} does not match packaged manifest kind ${expected.kind}.",
            )
        }
    }
}
