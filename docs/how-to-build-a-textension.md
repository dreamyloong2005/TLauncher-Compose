# How to Build a `TExtension`

This document is the end-to-end developer workflow for creating a third-party extension package for TLauncher Compose.

Canonical terminology:

- [SDK Glossary](sdk-glossary.md)

## Goal

By the end of this flow, you should have:

1. an extension source project
2. one `ExtensionEntrypoint`
3. one `LauncherExtension`
4. a generated `manifest.json`
5. a packaged `TExtension` file

## 1. Choose A Kind

Every extension declares exactly one primary `kind`.

Current kinds:

- `TEMPLATE`
- `THEME`
- `PLUGIN`

Choose the kind based on the extension's main purpose:

- use `TEMPLATE` for game integration
- use `THEME` for visual styling
- use `PLUGIN` for programmable host behavior

For a first package, `PLUGIN` is the easiest place to start.

## 2. Start With One Source Project

Use one extension project and split code by source set:

```text
src/
|- commonMain/
|  `- kotlin/
|- androidMain/
|  |- kotlin/
|  `- assets/
`- windowsMain/
   |- kotlin/
   `- assets/
```

Put shared contracts, IDs, metadata, and reusable helpers in `commonMain`. Put platform entrypoints, host adapters, and platform-only assets in `androidMain` or `windowsMain`.

The build packages each platform target into its own `TExtension` file. For a first package, `WINDOWS` remains the easiest target because it only needs a JVM runtime JAR.

## 3. Add The SDK Dependency

Your extension project should compile against the SDK API, not against the installable `TExtension` package.

Current SDK API artifact: `com.dreamyloong.tlauncher:tlauncher-extension-api:1.0.0`.

Target shape:

```kotlin
dependencies {
    compileOnly("com.dreamyloong.tlauncher:tlauncher-extension-api:1.0.0")
}
```

During local workspace development, the dependency should come from a Maven artifact. New extension projects should use the same coordinates from a shared Maven repository once one is available.

For public SDK releases, download and extract the SDK Maven repository zip:

```text
tlauncher-extension-sdk-1.0.0-maven.zip
```

Then point Gradle at the extracted directory:

```kotlin
repositories {
    maven {
        url = uri("path/to/tlauncher-extension-sdk-1.0.0-maven")
    }
    mavenCentral()
}
```

Launcher maintainers create that zip with:

```powershell
.\gradlew.bat packageExtensionSdkMaven --no-daemon --no-configuration-cache
```

When importing SDK types, prefer:

- `com.dreamyloong.tlauncher.sdk.*`

Avoid treating `com.dreamyloong.tlauncher.core.*` as the public extension API surface.

If your extension needs a host-owned operation, use the SDK facade exposed through `ExtensionContext.hostServices`. Do not copy launcher-owned native libraries, account/session storage code, or other host implementation details into the extension project. For Steam depot access, use `context.hostServices.steamDepot` and handle it being unavailable on hosts that do not provide that service.

## 4. Implement An Entrypoint

Declare one runtime entrypoint class that implements:

```kotlin
com.dreamyloong.tlauncher.sdk.extension.ExtensionEntrypoint
```

That entrypoint returns one `LauncherExtension`.

Minimal shape:

```kotlin
import com.dreamyloong.tlauncher.sdk.extension.ExtensionEntrypoint
import com.dreamyloong.tlauncher.sdk.extension.LauncherExtension

object MyExtensionEntrypoint : ExtensionEntrypoint {
    override fun createExtension(): LauncherExtension {
        return MyExtension()
    }
}
```

## 5. Implement A LauncherExtension

Your `LauncherExtension` provides:

- manifest-backed extension identity
- display metadata
- runtime feature creation

Minimal shape:

```kotlin
import com.dreamyloong.tlauncher.sdk.extension.ExtensionContext
import com.dreamyloong.tlauncher.sdk.extension.ExtensionFeature
import com.dreamyloong.tlauncher.sdk.extension.LauncherExtension
import com.dreamyloong.tlauncher.sdk.model.ExtensionManifest

private class MyExtension : LauncherExtension {
    override val extension: ExtensionManifest = TODO()
    override val displayName: String = "My Extension"
    override val version: String = "1.0.0"
    override val apiVersion: String = "1.0.0"
    override val entrypoint: String = "com.example.MyExtensionEntrypoint"

    override fun createFeatures(context: ExtensionContext): List<ExtensionFeature> {
        val hostServices = context.hostServices
        return emptyList()
    }
}
```

## 6. Choose Capabilities Carefully

Your extension manifest and runtime features must agree with each other.

Examples:

- a page contribution plugin needs `PROVIDE_PLUGIN_PAGE_CONTRIBUTIONS`
- a page mutation plugin needs `MUTATE_PAGE_TREE`
- a plugin that reads saved game instance state needs both:
  - capability: `READ_GAME_LIBRARY`
  - manifest `permissionKey`: `READ_GAME_LIBRARY`

Sensitive resource access requires both:

1. the right capability
2. the matching `permissionKey`
3. a host `HostGrant` decision at runtime

## 7. Generate `manifest.json`

Your package manifest should include at least:

- `id`
- `kind`
- `displayName`
- `version`
- `apiVersion`
- `supportedTargets`
- `capabilities`
- `compatibility`
- `entrypoints`
- `runtimeArtifacts`
- `permissionKeys` when needed

Example shape:

```json
{
  "id": "example.hello",
  "kind": "PLUGIN",
  "displayName": "Hello Extension",
  "version": "1.0.0",
  "apiVersion": "1.0.0",
  "supportedTargets": ["WINDOWS"],
  "capabilities": ["PROVIDE_PLUGIN_PAGE_CONTRIBUTIONS"],
  "compatibility": {
    "packageFormatVersion": 1,
    "minSdkApiVersion": 1,
    "targetSdkApiVersion": 1
  },
  "entrypoints": {
    "WINDOWS": "com.example.hello.HelloExtensionEntrypoint"
  },
  "runtimeArtifacts": {
    "WINDOWS": "runtime/windows/hello-extension.jar"
  }
}
```

## 8. Package The Runtime Artifact

The final `TExtension` package is a zip-format archive with a different suffix.

Minimal layout:

```text
my-extensionTExtension
├─ manifest.json
└─ runtime/
   └─ windows/
      └─ my-extension.jar
```

Optional package resources should live under `resources/`:

```text
my-extensionTExtension
├─ manifest.json
├─ runtime/
│  └─ windows/
│     └─ my-extension.jar
└─ resources/
   └─ icons/
      └─ night.png
```

Runtime code reads those files through `ExtensionContext.packageResources`.

Resources should be files owned by the extension package. Host-owned runtime services should stay in the launcher and be accessed through `ExtensionContext.hostServices`.

## 9. Build The Package

The in-repository Gradle sample modules were removed on purpose once the SDK contract started moving faster than the sample projects. Keeping executable sample projects in the main tree made them drift out of date too easily.

Use the documentation-first references in this order instead:

1. [Minimal Plugin Entrypoint Example](examples/minimal-plugin-entrypoint.kt)
2. [Example Build Script](examples/textension-plugin-build.gradle.kts)
3. [Extension SDK Packaging](extension-sdk-packaging.md)

That keeps the public guidance stable without pretending the launcher repository itself is the recommended place to author third-party extensions.

## 10. Load And Test It

Once packaged, the host should be able to:

1. parse the `TExtension`
2. validate `kind`, capabilities, and `permissionKeys`
3. load the target runtime artifact
4. instantiate the declared `ExtensionEntrypoint`
5. create the `LauncherExtension`
6. validate feature registration
7. expose permission review when required

Installed packages are copied into the host extension package directory and scanned again at startup. If you install a fixed build over a package that previously failed to load, the host clears stale load-failure state after the compatible replacement succeeds, so the Extension Manager should reflect the new load result immediately.

Package outputs should be written under the extension project's build output:

```text
build/dist/
```

## 11. Common Failure Cases

Typical errors during early development:

- wrong `kind` for the declared capabilities
- missing required `permissionKeys`
- unsupported target declared in `entrypoints` or `runtimeArtifacts`
- blank `apiVersion`
- missing or invalid SDK compatibility range
- runtime artifact path escaping the package root
- entrypoint class missing from the runtime JAR
- feature registration that does not match declared capabilities
- assuming a host-owned service is available without checking `context.hostServices`
- bundling launcher-owned implementation libraries instead of using the public `sdk.host` facade

## 12. Recommended Next Reads

- [Extension SDK Architecture](extension-sdk-architecture.md)
- [Extension SDK Packaging](extension-sdk-packaging.md)
- [Capability Matrix and Host Permissions](capability-matrix-and-host-permissions.md)
- [`TExtension` Package Format](textension-package-format.md)
