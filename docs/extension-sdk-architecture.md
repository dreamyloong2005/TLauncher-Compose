# Extension SDK Architecture

This document defines the SDK 1.0.0 boundary for third-party extensions in TLauncher Compose.

The goal is not to make every extension kind equally powerful. The goal is to keep a single extension pipeline while giving each kind a clear and enforceable capability boundary.

## Shared Terminology

Canonical reference:

- [SDK Glossary](sdk-glossary.md)

- `Extension`: the installable unit recognized by the launcher
- `kind`: the primary category of an extension, currently `TEMPLATE`, `THEME`, or `PLUGIN`
- `capability`: an architectural authority declared by an extension manifest
- `permissionKey`: a manifest-time request for sensitive host access
- `HostGrant`: the host's persisted decision for a requested `permissionKey`
- `HostServices`: typed host-owned services exposed through `ExtensionContext.hostServices`
- `TExtension`: the installable distribution package that carries a manifest and runtime artifacts

## Core Rule

All third-party additions enter the launcher as an `Extension`, but each extension must declare a single primary kind:

- `TEMPLATE`
- `THEME`
- `PLUGIN`

The launcher may load all three through the same extension pipeline, but it must not treat them as interchangeable.

## Why One SDK With Kind Boundaries

The project already uses `Extension` as the umbrella abstraction for:

- game templates
- themes
- plugins

That should stay true in the SDK. What changes is that the SDK must make the kind-specific contract explicit instead of relying on convention.

The practical shape is:

1. shared manifest rules
2. shared extension identity and compatibility rules
3. shared localization and page registration primitives
4. kind-specific capability limits

In code, this means the host should prefer one shared `ExtensionManifest` with an `ExtensionKind` enum instead of three separate manifest base classes that all inherit from a common `Extension` type.

## Kind Definitions

### Template

A template is a game-integration extension.

Templates are allowed to:

- define a game template identity
- declare supported platforms and launch support levels
- declare runtime requirements
- provide template-scoped page contributions
- provide template-scoped settings contributions
- call host-owned SDK services exposed by the current host when the service is available

Templates are not allowed to:

- mutate arbitrary page trees after resolution
- act like general-purpose automation plugins
- request broad host permissions by default
- define launcher-wide visual behavior
- import launcher internals or bundle launcher-owned native/vendor libraries as part of normal SDK use

In other words, a template may extend the launcher as the implementation of one game family, but it should not become a general host plugin.

### Theme

A theme is a visual-style extension.

Themes are allowed to:

- define theme metadata
- provide theme tokens
- provide scene and preview descriptors

Themes are not allowed to:

- mutate launcher state
- register arbitrary runtime behavior
- access saved game instance state
- request network or filesystem access
- inject general host logic

Themes must stay declarative from the launcher's point of view, even if their implementation code internally computes tokens or scene data.

### Plugin

A plugin is a programmable host extension.

Plugins are allowed to:

- contribute widgets and sections to launcher pages
- contribute settings content
- mutate resolved page trees when explicitly allowed
- intercept launcher actions when explicitly allowed
- intercept game launch requests when explicitly allowed
- intercept template diagnostics, file checks, and runtime reports when explicitly allowed
- intercept prepared template launch requests before execution when explicitly allowed
- read or write game-library state when explicitly granted
- request host-level permissions such as network or filesystem access

Plugins are not the default way to add a new game template or a new theme. If the primary purpose is game integration, use a `TEMPLATE`. If the primary purpose is visual style, use a `THEME`.

## Capability Matrix

The SDK should use capability declarations, but the host must validate them against the extension kind.

### Template capabilities

- `DEFINE_TEMPLATE_METADATA`
- `DEFINE_TEMPLATE_RUNTIME_REQUIREMENTS`
- `PROVIDE_TEMPLATE_PAGE_CONTRIBUTIONS`
- `PROVIDE_TEMPLATE_SETTINGS_CONTRIBUTIONS`

### Theme capabilities

- `DEFINE_THEME_METADATA`
- `DEFINE_THEME_TOKENS`
- `DEFINE_THEME_SCENE`

### Plugin capabilities

- `PROVIDE_PLUGIN_PAGE_CONTRIBUTIONS`
- `PROVIDE_PLUGIN_SETTINGS_CONTRIBUTIONS`
- `MUTATE_PAGE_TREE`
- `INTERCEPT_ACTIONS`
- `INTERCEPT_GAME_LAUNCH`
- `INTERCEPT_TEMPLATE_LAUNCH_PREPARATION`
- `READ_GAME_LIBRARY`
- `WRITE_GAME_LIBRARY`
- `OPEN_EXTERNAL_URLS`
- `NETWORK_ACCESS`
- `FILESYSTEM_READ`
- `FILESYSTEM_WRITE`

## Host Policy

Capability declarations do not automatically grant access. They express what an extension wants to do within the limits of its kind.

The host remains responsible for:

- validating that capabilities are allowed for the extension kind
- validating that requested `permissionKeys` are allowed for the extension kind
- rejecting or downgrading invalid declarations
- gating sensitive operations behind explicit host permissions
- keeping launcher-wide behavior stable even when extensions are installed

For sensitive operations, the contract is:

1. the manifest declares capabilities
2. the manifest requests `permissionKeys`
3. the host records `HostGrant` decisions
4. runtime access checks must respect the current grants

For host-owned operations that are intentionally exposed as platform services, the contract is different:

1. the SDK declares a typed service interface under `com.dreamyloong.tlauncher.sdk.host`
2. the host provides an implementation through `ExtensionContext.hostServices`
3. the extension handles the service being unavailable on platforms or launcher versions that do not provide it
4. the extension does not import `core.*` or package the host implementation library

## Current Implementation Direction

The SDK 1.0.0 implementation defines these rules in the public `sdk.*` ABI, while the launcher host enforces them inside shared core:

1. add a shared `ExtensionCapability` enum
2. add a capability policy by `ExtensionKind`
3. require `Template`, `Theme`, and `Plugin` package manifests to compose a shared `ExtensionManifest`
4. expose validation helpers on `ExtensionManifest`

That gives the project a concrete SDK contract and keeps extension binaries independent from launcher internals.

The runtime registration model uses feature composition:

1. one shared extension context contract
2. one shared feature marker interface
3. kind-specific feature interfaces layered on top of that shared feature model
4. typed host services available through that shared context
5. host validation that checks `kind`, `capabilities`, and registered feature types together

The manifest contract carries explicit SDK compatibility metadata:

1. `packageFormatVersion`
2. `minSdkApiVersion`
3. `targetSdkApiVersion`

For the current SDK release, the public SDK artifact is `1.0.0`, the host SDK compatibility integer is `1`, and the package format version is `1`. The in-repository ABI module is `extensionSdkApi/`, and its primary extension contract source entry is `ExtensionSdk.kt`.

SDK compatibility follows the Android SDK pattern. An extension can load when its `minSdkApiVersion` is not newer than the host SDK API version, and older `targetSdkApiVersion` values remain valid while the host still supports that behavior level.

The installed package lifecycle is now executable in the launcher:

1. users pick or install a `TExtension` package through the Extension Manager
2. the host copies installed packages into the platform package directory
3. startup scans parse installed packages, apply compatibility and permission checks, and load runtime entrypoints for the current platform
4. enable or disable state and host grant decisions are persisted by extension identity
5. compatible overwrite installs clear stale runtime failure records so a fixed package can load without requiring a page round trip or app restart

The host should evaluate these fields through a shared compatibility checker instead of scattering version checks across the UI or loader code.

High-value behavior should be routed through explicit interception points instead of direct cross-extension mutation. The current recommended interception points are:

1. launcher actions such as navigation, refresh, external URL opens, and launch requests
2. game launch execution after a template prepares a request
3. template diagnostics such as file checks and runtime readiness reports
4. template launch preparation after a template builds a request but before the host executes it
5. resolved page trees after base contributions, conflict resolution, and plugin mutation ordering

## Public SDK ABI Modules

Extension developers should compile against the SDK ABI instead of importing launcher internals directly.

Current SDK files:

- `ExtensionSdk.kt`: entrypoint, extension runtime, compatibility, host grants, package resources, package-local state, host paths, and host service access
- `HostServicesSdk.kt`: typed host-owned services such as Steam depot access
- `ModelSdk.kt`: extension kind, platform target, manifest, and package metadata models
- `PageSdk.kt`: page, section, widget, and action registration models
- `TemplateSdk.kt`, `ThemeSdk.kt`, and `PluginSdk.kt`: kind-specific feature contracts
- `PlatformSdk.kt`: file pickers, launch requests, and launcher icon control

These files declare the public `com.dreamyloong.tlauncher.sdk.*` ABI directly. Runtime loading accepts SDK entrypoints and the host adapts SDK objects to `core.*` internally.

## Host Services

`ExtensionContext.hostServices` is the SDK boundary for operations that must stay owned by the launcher host.

Use host services when an extension needs a capability that depends on host-owned account state, secure storage, native binaries, or platform-specific implementation code. The extension compiles against the `sdk.host` interface and receives the host implementation at runtime.

Current host service surface:

- `SteamDepotService`

The launcher currently provides `SteamDepotService` through CAuth-backed host implementations on Android and Windows. Extensions should call this SDK facade instead of bundling CAuth artifacts, loading launcher runtime classes, or reaching into `core.*`.

Host services are nullable by design. An extension should fail gracefully or hide the relevant action when a required service is not available on the current host.

## Platform Resources And Icons

Installed packages can expose packaged files through `ExtensionContext.packageResources`. Template and plugin packages should place non-code files under the package `resources/` directory.

`PlatformSdk.kt` also exposes `LauncherIconController` and `LauncherIconSource`. Windows can update the running window icon, including bytes loaded from package resources through `LauncherIconSource.Asset`. Android launcher-icon changes are component-alias based and require the launcher to close, so Android exposes the same API shape while applying changes through the settings-driven save-and-close flow.

## Future Work

This first pass intentionally does not solve everything. The next stages should likely include:

1. a unified runtime extension manifest format
2. a loader that resolves installed extensions from disk
3. a Gradle packager or convention plugin for extension projects
4. host permission prompts and review surfaces for plugins
5. template-specific and plugin-specific context objects with narrower APIs than the current app internals
