# Extension SDK Packaging

This document defines the first concrete answer to two different packaging questions:

1. how the SDK itself should be delivered to extension developers
2. how an extension should be built and distributed as a `TExtension` package

These two things should not be conflated.

## Shared Terminology

Canonical reference:

- [SDK Glossary](sdk-glossary.md)

- `Extension`: the installable unit recognized by the launcher
- `kind`: the primary category of an extension, currently `TEMPLATE`, `THEME`, or `PLUGIN`
- `capability`: an architectural authority declared by an extension manifest
- `permissionKey`: a manifest-time request for sensitive host access
- `HostGrant`: the host's persisted decision for a requested `permissionKey`
- `TExtension`: the installable distribution package used by the launcher

## 1. SDK Packaging Form

The SDK should be delivered as normal developer-facing build artifacts, not as `TExtension`.

Recommended split:

1. `tlauncher-extension-api`
   - compile-time API only
   - backed in-repo by `extensionSdkApi/`
   - published as Maven artifacts and plain JAR metadata
   - versioned independently with SDK compatibility rules

2. `tlauncher-textension-packager`
   - Gradle plugin or Gradle build convention
   - generates `manifest.json`
   - assembles the final `TExtension` archive

3. `TExtension`
   - install-time delivery package
   - contains the extension manifest and runtime artifacts
   - consumed by the launcher, not by the extension source project

That means:

- developers compile against SDK JARs
- users install `TExtension` files

## 2. Current State

The repository already has a stable package parser, install flow, and package suffix:

- suffix: `TExtension`
- manifest path: `manifest.json`
- current SDK API artifact version: `1.0.0`
- current host SDK compatibility version: `1`

The dedicated SDK ABI now lives in `extensionSdkApi/`. The current implementation direction is:

1. stabilize public entrypoint and manifest contracts first
2. stabilize packaging layout and build workflow second
3. publish the SDK API artifacts through Maven so extension projects can build outside the launcher source checkout

The stable runtime contract introduced for packaging is:

- public entrypoint interface: `com.dreamyloong.tlauncher.sdk.extension.ExtensionEntrypoint`
- public host-service access point: `ExtensionContext.hostServices`

Each target-specific runtime artifact referenced in `manifest.json` should expose a class that implements this interface.

The launcher can install `TExtension` files through the Extension Manager, scan installed packages at startup, load runtime artifacts for the active platform, persist enable or disable state, and delete installed packages. When replacing an installed package, the host compares package identity and version, supports overwrite confirmation, and clears stale load-failure records after a successful compatible replacement.

## 3. Version Terms

The manifest carries three version concepts that should be kept separate:

1. `version`
   - the package release version shown to users
2. `apiVersion`
   - the extension runtime API version exposed by the extension itself
3. `compatibility.*`
   - the host SDK compatibility range declared by the package

The compatibility range currently uses:

- `compatibility.minSdkApiVersion`: the minimum host SDK contract version required by the package
- `compatibility.targetSdkApiVersion`: the host SDK behavior level targeted by the package

In short:

- `version` is about the package release
- `apiVersion` is about the extension runtime contract surface
- `compatibility.*` is about host and SDK compatibility

The compatibility model follows the Android SDK pattern: the minimum SDK API version decides whether the host is new enough to load the package, while the target SDK API version declares the behavior level the extension expects. The host can continue running older target SDK packages as long as that target remains inside the host's supported SDK window.

## 4. Recommended Developer Workflow

For SDK 1.0.0, the workflow should look like this.

### Step 1: Create one extension source project

The extension author creates one Kotlin project with shared and platform source sets.

Recommended source layout:

```text
my-extension/
|- build.gradle.kts
`- src/
   |- commonMain/
   |  `- kotlin/
   |- androidMain/
   |  |- kotlin/
   |  `- assets/
   `- windowsMain/
      |- kotlin/
      `- assets/
```

Use `commonMain` for shared extension code. Use `androidMain`, `windowsMain`, and future target source sets for platform-specific entrypoints, host bridges, and assets.

Do not copy launcher-owned vendor libraries into an extension project just to reach host features. If the launcher owns the native implementation or account/session state, depend on the SDK facade and access it through `ExtensionContext.hostServices`.

For SDK 1.0.0, Steam depot access is exposed as `SteamDepotService`. Android and Windows hosts provide it through launcher-owned CAuth runtime integrations, while extensions keep only the SDK dependency.

### Step 2: Depend on the SDK API

The extension project should add a compile-time dependency on the SDK API artifact.

Target shape:

```kotlin
dependencies {
    compileOnly("com.dreamyloong.tlauncher:tlauncher-extension-api:1.0.0")
}
```

Extension projects should resolve SDK `1.0.0` from a Maven repository instead of cloning the launcher source checkout.

For public releases, maintainers should publish a self-contained Maven repository zip:

```powershell
.\gradlew.bat packageExtensionSdkMaven --no-daemon --no-configuration-cache
```

The release artifact is:

```text
build/dist/tlauncher-extension-sdk-1.0.0-maven.zip
```

Extension developers download that zip, extract it, and add the extracted directory as a Maven repository:

```kotlin
repositories {
    maven {
        url = uri("path/to/tlauncher-extension-sdk-1.0.0-maven")
    }
    mavenCentral()
}

dependencies {
    compileOnly("com.dreamyloong.tlauncher:tlauncher-extension-api:1.0.0")
}
```

For launcher workspace testing only, maintainers can still refresh local Maven artifacts with:

```powershell
.\gradlew.bat :extensionSdkApi:publishToMavenLocal --no-daemon --no-configuration-cache
```

### Step 3: Implement one entrypoint class

The extension runtime JAR should expose one class named in `manifest.json`.

That class should implement:

```kotlin
com.dreamyloong.tlauncher.sdk.extension.ExtensionEntrypoint
```

It should return exactly one `LauncherExtension` root object.

### Step 4: Build the runtime artifacts

The extension build produces one runtime artifact per platform package.

Current package conventions:

- `WINDOWS` target runtime artifact: JVM `.jar`
- `ANDROID` target runtime artifact: dexed runtime `.jar`

Recommended path inside the package:

```text
runtime/windows/<extension-artifact>.jar
```

### Step 5: Generate `manifest.json`

The build should generate a manifest containing:

- extension identity
- `kind`
- `version`
- `apiVersion`
- supported targets
- capabilities
- `permissionKeys` when sensitive plugin access is requested
- SDK compatibility metadata
- target-specific `entrypoints`
- target-specific `runtimeArtifacts`

### Step 6: Zip each target into `TExtension`

Each final package should target one host platform. A single source project can emit multiple `TExtension` files.

Minimal Windows layout:

```text
my-extensionTExtension
├─ manifest.json
└─ runtime/
   └─ windows/
      └─ my-extension.jar
```

Packages can also include resources:

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

At runtime, extension code can read those files through `ExtensionContext.packageResources`.

Package resources should contain extension-owned assets and runtime files. Host-owned services, native binaries, and account/session stores remain in the launcher and are reached through SDK host service facades.

## 5. Why `TExtension` Should Stay a Distribution Package

The `TExtension` file should remain a launcher-facing installation artifact instead of becoming the SDK itself.

That separation gives cleaner versioning:

- SDK version controls what developers compile against
- `TExtension` package version controls what users install

It also improves forward compatibility:

- the launcher can evolve package loading separately from source-level SDK APIs
- the SDK can add helpers without forcing a package-format bump

## 6. First Public Packaging Contract

The first public contract should be intentionally small:

1. one manifest file
2. one entrypoint class per target
3. one runtime artifact path per target
4. one archive suffix: `TExtension`

Do not make the first public SDK depend on custom binary metadata or a bespoke compiler toolchain.

Plain Kotlin/Gradle plus a packaging task is the right first step.

## 7. Build Template

A copyable sample build script is provided here:

- [textension-plugin-build.gradle.kts](examples/textension-plugin-build.gradle.kts)
- [minimal-plugin-entrypoint.kt](examples/minimal-plugin-entrypoint.kt)

That template shows:

- a JVM extension build for the Windows target
- manifest generation
- JAR packaging
- `TExtension` archive creation

Multi-platform extension projects should expose package tasks that produce one `TExtension` file per target.

## 8. Host-Side Runtime Flow

The launcher runtime flow is:

1. parse and validate the package manifest
2. load the runtime artifact for the current `PlatformTarget`
3. instantiate the declared `ExtensionEntrypoint`
4. create the `LauncherExtension`
5. validate `kind`, `capabilities`, `permissionKeys`, and registered feature types
6. provide `ExtensionContext` with host grants, package resources, package-local state, host paths, and host services
7. apply host grants before enabling sensitive runtime behavior
8. surface parse or runtime load failures in Extension Manager until the package is fixed, replaced, disabled, or deleted

That is the point where the SDK pipeline becomes fully executable end to end.
