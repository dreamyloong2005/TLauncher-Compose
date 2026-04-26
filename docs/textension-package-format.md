# `TExtension` Package Format

This document defines the first package format for installable third-party extensions.

## Shared Terminology

Canonical reference:

- [SDK Glossary](sdk-glossary.md)

- `Extension`: the installable unit recognized by the launcher
- `kind`: the primary category of an extension, currently `TEMPLATE`, `THEME`, or `PLUGIN`
- `capability`: an architectural authority declared by the package manifest
- `permissionKey`: a manifest-time request for sensitive host access
- `HostGrant`: the host's persisted decision for a requested `permissionKey`

## File Extension

- Extension archive suffix: `TExtension`
- Required manifest path inside the package: `manifest.json`
- Recommended runtime directory: `runtime/<target>/`
- Recommended resource directory: `resources/`

## Current Parsing Scope

The current shared parser validates:

1. package suffix
2. manifest JSON structure
3. extension kind and capability declaration
4. SDK compatibility metadata
5. feature loading entrypoint declarations
6. single-platform package shape for each `TExtension`
7. runtime artifact path safety
8. runtime artifact presence when a package source can expose file entries

The parser is intentionally separated from archive transport. The shared parser works with a logical package source abstraction, while platform package stores provide file-backed sources for installed `TExtension` files.

## Required Manifest Fields

```json
{
  "id": "example.debug.tools",
  "kind": "PLUGIN",
  "displayName": "Debug Tools",
  "version": "1.0.0",
  "apiVersion": "1.0.0",
  "supportedTargets": ["WINDOWS"],
  "capabilities": [
    "PROVIDE_PLUGIN_PAGE_CONTRIBUTIONS",
    "INTERCEPT_ACTIONS",
    "NETWORK_ACCESS"
  ],
  "compatibility": {
    "packageFormatVersion": 1,
    "minSdkApiVersion": 1,
    "targetSdkApiVersion": 1
  },
  "entrypoints": {
    "WINDOWS": "com.example.debug.DebugToolsEntrypoint"
  },
  "runtimeArtifacts": {
    "WINDOWS": "runtime/windows/debug-tools.jar"
  },
  "permissionKeys": ["NETWORK_ACCESS"]
}
```

## Version Field Meanings

The manifest uses three different version concepts:

- `version`: the package release version
- `apiVersion`: the extension runtime API version exposed by the package
- `compatibility.minSdkApiVersion`: the minimum host SDK contract the package requires
- `compatibility.targetSdkApiVersion`: the host SDK behavior level the package targets

Current SDK `1.0.0` packages should use `apiVersion: "1.0.0"` with both `minSdkApiVersion` and `targetSdkApiVersion` set to `1`.

The parser also accepts `sdkApiVersion` as a shorthand when a package's minimum and target SDK API versions are the same. New package manifests should prefer the explicit minimum and target fields.

The compatibility block is about launcher and SDK compatibility. It is not a replacement for `version` or `apiVersion`.

## Validation Rules

1. The package name must end with `TExtension`.
2. `manifest.json` must exist.
3. `id`, `displayName`, `version`, and `apiVersion` must be non-blank.
4. `supportedTargets` must not be empty.
5. `compatibility` must declare `minSdkApiVersion` and `targetSdkApiVersion`, or the `sdkApiVersion` shorthand.
6. SDK API version numbers must be positive, and `minSdkApiVersion` must not be greater than `targetSdkApiVersion`.
7. Capabilities must be valid for the declared `kind`.
8. `entrypoints` must not be empty.
9. Each `TExtension` package must target exactly one platform. Split multi-platform source projects into one package per target.
10. `entrypoints` and `runtimeArtifacts` must match `supportedTargets`.
11. Runtime artifact paths must stay relative to the package root and must not escape it with `..`.
12. `permissionKeys` are manifest-time requested host permissions and are currently reserved for `PLUGIN` extensions only.
13. A target entrypoint should name a runtime class that implements `com.dreamyloong.tlauncher.sdk.extension.ExtensionEntrypoint`.

## V1 Layout Convention

Recommended single-platform layout:

```text
my-extensionTExtension
├─ manifest.json
├─ runtime/
│  └─ windows/
│     └─ my-extension.jar
└─ resources/
   └─ ...
```

The parser does not currently require a fixed `runtime/` folder name, but this layout should be treated as the default convention for SDK tooling.

For SDK 1.0.0, one `TExtension` file should target one host platform. A single source project can still build several files, such as:

- `template.example.windows-1.0.0TExtension`
- `template.example.android-1.0.0TExtension`

Current runtime artifact conventions:

- `WINDOWS` -> JVM `.jar`
- `ANDROID` -> dexed runtime `.jar`

## Resources

Files under `resources/` are package-owned assets. The host exposes them through `ExtensionContext.packageResources`, so extension code can read packaged files without depending on launcher-internal paths.

Examples:

- `resources/icons/night.png`
- `resources/runtime/dotnet_bcl/System.Private.CoreLib.dll`
- `resources/android/jniLibs/arm64-v8a/libnative_runtime.so`

Host-owned services and launcher vendor libraries are not package resources. Extension code should access those through typed SDK services such as `ExtensionContext.hostServices` when the host exposes them. Steam depot operations, for example, should use `SteamDepotService` instead of shipping CAuth binaries or reflecting into launcher runtime classes.
