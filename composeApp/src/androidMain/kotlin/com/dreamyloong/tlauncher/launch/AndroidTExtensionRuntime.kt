package com.dreamyloong.tlauncher.launch

import android.content.Context
import com.dreamyloong.tlauncher.core.extension.DefaultExtensionPackageParser
import com.dreamyloong.tlauncher.core.extension.PackageBackedExtensionResources
import com.dreamyloong.tlauncher.core.model.PlatformTarget
import com.dreamyloong.tlauncher.core.platform.findInstalledTExtensionPackage
import com.dreamyloong.tlauncher.core.platform.prepareReadOnlyDynamicCodeFile
import com.dreamyloong.tlauncher.core.platform.writeManagedResourceFile
import androidx.fragment.app.Fragment
import dalvik.system.DexClassLoader
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

internal object AndroidTExtensionRuntime {
    private val cachedRuntimes = mutableMapOf<String, PreparedAndroidTExtensionRuntime>()
    private val loadedNativeLibraries = mutableSetOf<String>()

    fun prepare(
        context: Context,
        payload: AndroidGameLaunchPayload,
    ): PreparedAndroidTExtensionRuntime {
        val cacheKey = payload.cacheKey()
        cachedRuntimes[cacheKey]?.let { cached -> return cached }
        val packageLookup = findInstalledTExtensionPackage(
            identityId = payload.templatePackageId.value,
            target = PlatformTarget.ANDROID,
        ) ?: error("Installed Android .textension package was not found: ${payload.templatePackageId.value}")
        val parsedPackage = DefaultExtensionPackageParser.parse(packageLookup.source)
        val resources = PackageBackedExtensionResources(packageLookup.source)
        val rootDirectory = context.codeCacheDir
            .resolve("android-extension-runtime")
            .resolve(payload.templatePackageId.value.toSafeFileName())
            .apply { mkdirs() }
        val nativeLibraryDirectory = rootDirectory.resolve("arm64-v8a").apply { mkdirs() }
        resources.list(payload.nativeLibraryResourceDirectory)
            .sorted()
            .forEach { fileName ->
                val bytes = resources.readBytes("${payload.nativeLibraryResourceDirectory}/$fileName")
                    ?: error("Missing Android native library resource: $fileName")
                writeManagedResourceFile(
                    directory = nativeLibraryDirectory,
                    fileName = fileName,
                    bytes = bytes,
                )
            }
        val dynamicJarFiles = payload.dynamicJarResourcePaths.mapIndexed { index, resourcePath ->
            val bytes = resources.readBytes(resourcePath)
                ?: error("Missing Android runtime jar resource: $resourcePath")
            prepareReadOnlyDynamicCodeFile(
                directory = rootDirectory,
                fileName = "$index-${resourcePath.substringAfterLast('/').ifBlank { "runtime.jar" }}",
                bytes = bytes,
            )
        }
        val runtimeArtifactPath = parsedPackage.manifest.runtimeArtifacts[PlatformTarget.ANDROID]
            ?: error("Missing Android runtime artifact declaration.")
        val runtimeArtifactBytes = packageLookup.source.readBytes(runtimeArtifactPath)
            ?: error("Missing Android runtime artifact: $runtimeArtifactPath")
        val runtimeArtifactFile = prepareReadOnlyDynamicCodeFile(
            directory = rootDirectory,
            fileName = runtimeArtifactPath.substringAfterLast('/').ifBlank { "android-runtime.jar" },
            bytes = runtimeArtifactBytes,
        )
        val optimizedDirectory = context.codeCacheDir
            .resolve("android-extension-odex")
            .resolve(payload.templatePackageId.value.toSafeFileName())
            .apply { mkdirs() }
        val nativeLibrarySearchPath = listOf(
            nativeLibraryDirectory.absolutePath,
            context.applicationInfo.nativeLibraryDir,
        )
            .filter { path -> path.isNotBlank() }
            .joinToString(File.pathSeparator)
        val classLoader = DexClassLoader(
            (listOf(runtimeArtifactFile) + dynamicJarFiles)
                .joinToString(File.pathSeparator) { file -> file.absolutePath },
            optimizedDirectory.absolutePath,
            nativeLibrarySearchPath,
            context.classLoader,
        )
        val bridgeClass = payload.runtimeBridgeClassName?.let { className ->
            classLoader.loadClass(className)
        }
        loadNativeLibraries(
            payload = payload,
            bridgeClass = bridgeClass,
            nativeLibraryDirectory = nativeLibraryDirectory,
        )
        return PreparedAndroidTExtensionRuntime(
            nativeLibraryDirectory = nativeLibraryDirectory,
            bridgeClass = bridgeClass,
        ).also { prepared ->
            cachedRuntimes[cacheKey] = prepared
        }
    }

    fun onHostActivityCreated(
        context: Context,
        preparedRuntime: PreparedAndroidTExtensionRuntime,
    ) {
        preparedRuntime.bridgeClass?.invokeOptional("onHostActivityCreated", context)
    }

    fun onHostActivityDestroyed(
        context: Context,
        preparedRuntime: PreparedAndroidTExtensionRuntime,
    ) {
        preparedRuntime.bridgeClass?.invokeOptional("onHostActivityDestroyed", context)
    }

    fun prepareProjectDirectory(
        preparedRuntime: PreparedAndroidTExtensionRuntime,
        projectDirectory: File,
        launchContextJson: String,
        hostProjectKey: String? = null,
    ): File {
        val runtimeRootDirectory = preparedRuntime.nativeLibraryDirectory.parentFile
            ?: error("Android extension runtime root directory is unavailable.")
        val safeHostProjectKey = hostProjectKey
            ?.trim()
            ?.takeIf { key -> key.isNotBlank() }
            ?.toSafeFileName()
            ?: projectDirectory.hostProjectKey()
        val hostProjectDirectory = runtimeRootDirectory
            .resolve("project-host")
            .resolve(safeHostProjectKey)
            .apply { mkdirs() }
        val bridgedDirectory = preparedRuntime.bridgeClass
            ?.findStaticMethod("prepareProjectDirectory", parameterCount = 4)
            ?.invoke(
                null,
                preparedRuntime.nativeLibraryDirectory.absolutePath,
                projectDirectory.absolutePath,
                hostProjectDirectory.absolutePath,
                launchContextJson,
            )
            ?: preparedRuntime.bridgeClass
            ?.findStaticMethod("prepareProjectDirectory", parameterCount = 3)
            ?.invoke(
                null,
                preparedRuntime.nativeLibraryDirectory.absolutePath,
                projectDirectory.absolutePath,
                hostProjectDirectory.absolutePath,
            )
            ?: preparedRuntime.bridgeClass?.invokeOptional(
                name = "prepareProjectDirectory",
                preparedRuntime.nativeLibraryDirectory.absolutePath,
                projectDirectory.absolutePath,
            )
        return (bridgedDirectory as? String)
            ?.takeIf { path -> path.isNotBlank() }
            ?.let(::File)
            ?: hostProjectDirectory.takeIf(File::isDirectory)
            ?: projectDirectory
    }

    fun createHostFragment(
        preparedRuntime: PreparedAndroidTExtensionRuntime,
        payload: AndroidGameLaunchPayload,
    ): Fragment {
        val sourceProjectDirectory = payload.projectDirectory
            .trim()
            .takeIf { it.isNotBlank() }
            ?.let(::File)
            ?: error("Android launch project directory must not be blank.")
        val hostProjectDirectory = prepareProjectDirectory(
            preparedRuntime = preparedRuntime,
            projectDirectory = sourceProjectDirectory,
            launchContextJson = payload.launchContextJson,
            hostProjectKey = payload.hostProjectKey,
        )
        val packFile = sourceProjectDirectory.resolve(payload.packFileName)
        val bridgeClass = preparedRuntime.bridgeClass
            ?: error("Android runtime bridge class is required to create the host fragment.")
        val fragment = bridgeClass.findStaticMethod("createHostFragment", parameterCount = 3)
            ?.invoke(
                null,
                hostProjectDirectory.absolutePath,
                packFile.absolutePath,
                payload.launchContextJson,
            )
            ?: bridgeClass.invokeRequired(
                name = "createHostFragment",
                hostProjectDirectory.absolutePath,
                packFile.absolutePath,
            )
        return fragment as? Fragment
            ?: error("Android runtime bridge createHostFragment must return a Fragment.")
    }

    private fun loadNativeLibraries(
        payload: AndroidGameLaunchPayload,
        bridgeClass: Class<*>?,
        nativeLibraryDirectory: File,
    ) {
        val bridgeMethod = bridgeClass?.findStaticMethod("loadLibraries", parameterCount = 1)
        val allLibraries = nativeLibraryDirectory.listFiles()
            .orEmpty()
            .filter { file -> file.isFile && file.extension == "so" }
            .filterNot { file -> file.name in payload.hostNativeLibraryExcludes }
            .sortedWith(
                compareBy<File>(
                    {
                        payload.nativeLibraryLoadOrder.indexOf(it.name)
                            .let { index -> if (index == -1) Int.MAX_VALUE else index }
                    },
                    { it.name.lowercase() },
                ),
            )
            .toMutableList()

        var pendingLibraries = allLibraries
        while (pendingLibraries.isNotEmpty()) {
            val failedLibraries = mutableListOf<File>()
            var loadedInPass = 0
            pendingLibraries.forEach { libraryFile ->
                val libraryKey = "${payload.templatePackageId.value}:${libraryFile.name}"
                if (libraryKey in loadedNativeLibraries) {
                    return@forEach
                }
                val result = runCatching {
                    loadNativeLibrary(
                        libraryFile = libraryFile,
                        bridgeMethod = bridgeMethod,
                    )
                }
                if (result.isSuccess) {
                    loadedNativeLibraries += libraryKey
                    loadedInPass += 1
                } else {
                    failedLibraries += libraryFile
                }
            }
            if (failedLibraries.isEmpty()) {
                return
            }
            if (loadedInPass == 0) {
                val message = failedLibraries.joinToString(separator = ", ") { file ->
                    val failure = runCatching {
                        loadNativeLibrary(
                            libraryFile = file,
                            bridgeMethod = bridgeMethod,
                        )
                    }.exceptionOrNull()
                    val detail = failure?.rootMessage()?.takeIf { it.isNotBlank() }
                    if (detail == null) file.name else "${file.name} ($detail)"
                }
                error("Unable to load Android native libraries from .textension: $message")
            }
            pendingLibraries = failedLibraries
        }
    }

    private fun loadNativeLibrary(
        libraryFile: File,
        bridgeMethod: Method?,
    ) {
        if (bridgeMethod != null) {
            val result = runCatching {
                bridgeMethod.invoke(null, arrayOf(libraryFile.absolutePath))
            }
            val failure = result.exceptionOrNull() ?: return
            if (failure.isAlreadyOpenedByAnotherClassLoader()) {
                return
            }
            throw failure
        } else {
            System.load(libraryFile.absolutePath)
        }
    }

    private fun Throwable.isAlreadyOpenedByAnotherClassLoader(): Boolean {
        val message = rootMessage()
        return message.contains("already opened by ClassLoader") &&
            message.contains("can't open in ClassLoader")
    }

    private fun Throwable.rootMessage(): String {
        return rootCauses()
            .mapNotNull { failure -> failure.message?.takeIf { it.isNotBlank() } }
            .lastOrNull()
            .orEmpty()
    }

    private fun Throwable.rootCauses(): Sequence<Throwable> {
        return sequence {
            val seen = mutableSetOf<Throwable>()
            var current: Throwable? = this@rootCauses
            while (current != null && seen.add(current)) {
                yield(current)
                current = when (current) {
                    is InvocationTargetException -> current.targetException ?: current.cause
                    else -> current.cause
                }
            }
        }
    }

    private fun AndroidGameLaunchPayload.cacheKey(): String {
        return listOf(
            templatePackageId.value,
            nativeLibraryResourceDirectory,
            dynamicJarResourcePaths.joinToString("|"),
            runtimeBridgeClassName.orEmpty(),
        ).joinToString("#")
    }

    private fun String.toSafeFileName(): String {
        return map { character ->
            if (character.isLetterOrDigit() || character == '.' || character == '-' || character == '_') {
                character
            } else {
                '_'
            }
        }.joinToString("").ifBlank { "extension" }
    }

    private fun File.hostProjectKey(): String {
        return Integer.toHexString(absolutePath.hashCode())
    }

    private fun Class<*>.invokeOptional(
        name: String,
        vararg args: Any,
    ): Any? {
        return findStaticMethod(name, args.size)?.invoke(null, *args)
    }

    private fun Class<*>.invokeRequired(
        name: String,
        vararg args: Any,
    ): Any? {
        return findStaticMethod(name, args.size)
            ?.invoke(null, *args)
            ?: error("Android runtime bridge method was not found: $name/${args.size}")
    }

    private fun Class<*>.findStaticMethod(
        name: String,
        parameterCount: Int,
    ): Method? {
        return methods.firstOrNull { method ->
            method.name == name && method.parameterTypes.size == parameterCount
        }
    }
}

internal data class PreparedAndroidTExtensionRuntime(
    val nativeLibraryDirectory: File,
    val bridgeClass: Class<*>?,
)
