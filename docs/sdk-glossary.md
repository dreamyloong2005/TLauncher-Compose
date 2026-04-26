# SDK Glossary

This document is the canonical terminology reference for the TLauncher Compose extension SDK.

Use these definitions consistently across the README, SDK docs, package docs, samples, and future public SDK material.

## Core Terms

### Extension

An `Extension` is the installable unit recognized by the launcher.

Every installable third-party addition enters the host as an extension, even if its primary purpose is game integration, theming, or programmable host behavior.

### Kind

`kind` is the primary category of an extension.

Current kinds:

- `TEMPLATE`
- `THEME`
- `PLUGIN`

`kind` is the outer architectural boundary. It determines which capabilities, permissions, and feature types are allowed.

### Capability

A `capability` is an architectural authority declared by an extension manifest.

Examples:

- contribute page content
- mutate a page tree
- intercept launcher actions
- read saved game instance state

A capability says what an extension is allowed to participate in. It does not by itself grant sensitive host access.

### Permission Key

A `permissionKey` is a manifest-time request for sensitive host access.

Examples:

- `NETWORK_ACCESS`
- `FILESYSTEM_READ`
- `FILESYSTEM_WRITE`
- `READ_GAME_LIBRARY`
- `WRITE_GAME_LIBRARY`

In `TExtension` manifests, these appear under `permissionKeys`.

### Host Grant

A `HostGrant` is the host's persisted decision for a requested `permissionKey`.

Examples:

- `NETWORK_ACCESS` requested and granted
- `FILESYSTEM_WRITE` requested and denied

The manifest declares requested permissions. The host records the actual decision as grants.

### Host Grant State

`HostGrantState` is the current decision state of a `HostGrant`.

Current states:

- `GRANTED`
- `DENIED`

### `TExtension`

`TExtension` is the installable distribution package format for third-party extensions.

It is a launcher-facing package, not the SDK itself.

At minimum, a `TExtension` package carries:

- `manifest.json`
- target-specific runtime artifacts

### Template Package

A template package is a `TEMPLATE` extension that describes and implements support for one game family.

It can provide metadata, runtime requirements, template-scoped pages, and platform-specific launch preparation.

### Installed Extension Package

An installed extension package is a `TExtension` file that the launcher has copied into its platform extension package directory.

Installed packages are scanned at startup and can be enabled, disabled, reviewed for permissions, replaced, or deleted from the Extension Manager.

### Game Instance

A game instance is a saved launcher record for one user-created installation or configuration of a game family.

Instances persist the extension package identity that provided their template support. If that package is removed or disabled, the launcher can tell the user which template package must be installed or enabled again.

## Version Terms

### Version

`version` is the package release version shown to users.

This is about the installable package release, not host compatibility.

### API Version

`apiVersion` is the extension runtime API version exposed by the package itself.

This is separate from the package release version and separate from SDK compatibility metadata.

### SDK Compatibility

The compatibility block in `manifest.json` describes the SDK and host compatibility contract:

- `packageFormatVersion`
- `minSdkApiVersion`
- `targetSdkApiVersion`

These fields answer whether the package is compatible with the host SDK contract. They do not replace `version` or `apiVersion`.

Current values:

- SDK API artifact version: `1.0.0`
- SDK compatibility integer: `1`
- package format version: `1`

The launcher treats SDK compatibility like Android SDK compatibility: `minSdkApiVersion` is the lowest host SDK the package needs, and `targetSdkApiVersion` is the behavior level the package targets. `sdkApiVersion` remains accepted as a shorthand when both numbers are the same, but new manifests should use the explicit minimum and target fields.

## Runtime Terms

### Platform Target

`PlatformTarget` is the host platform key used in manifests and package tasks.

Current target values include:

- `WINDOWS`
- `ANDROID`
- `IOS`

### Entrypoint

An `entrypoint` is the runtime class declared in `manifest.json` that the host instantiates after loading a target runtime artifact.

The entrypoint class implements:

- `com.dreamyloong.tlauncher.sdk.extension.ExtensionEntrypoint`

### LauncherExtension

`LauncherExtension` is the runtime root object returned by an entrypoint.

It exposes:

- extension manifest metadata
- display metadata such as package version and api version
- feature creation through `createFeatures(...)`

### Extension Feature

An `ExtensionFeature` is a runtime participation unit created by a `LauncherExtension`.

Examples include:

- page contribution features
- page mutation features
- launcher action interception features
- template diagnostics interception features

Feature registration must still pass host validation against `kind`, `capabilities`, and requested permissions.

### Package Resources

Package resources are non-code files packaged under a `TExtension` package's `resources/` directory.

Runtime code accesses them through `ExtensionContext.packageResources`. This is how template-owned assets, packaged icons, runtime DLLs, and platform native files should be exposed to extension code without reaching into launcher project directories.

### Host Services

Host services are typed launcher-owned services exposed through `ExtensionContext.hostServices`.

They are used when an extension needs host-owned behavior without importing launcher internals or bundling launcher implementation libraries. A host service may depend on platform native code, secure storage, account/session state, or other resources that should remain in the launcher.

Current host service examples:

- `SteamDepotService`

Android and Windows hosts currently provide `SteamDepotService` through launcher-owned CAuth integrations. Extension packages should depend on the SDK interface and treat service availability as runtime host capability.

Host services are optional at runtime. Extension code should handle a service being `null` when the current platform or launcher version does not provide it.

### Launcher Icon Source

`LauncherIconSource` describes a launcher icon request.

Current sources:

- `Default`
- `Night`
- `Asset(path, bytes)`

Windows can update the running window icon from these sources. Android launcher-icon changes are applied through component aliases and require the app to close after saving the icon setting.

## Design Rules

Use the following wording consistently:

- say `kind`, not `role`
- say `permissionKey` for manifest-time permission requests
- say `HostGrant` for the host's persisted runtime decision
- say `host service` for a typed SDK facade backed by launcher-owned implementation code
- say `TExtension` for the installable package
- keep `version`, `apiVersion`, and SDK compatibility terms separate

## Recommended References

Related documents:

- [Extension SDK Architecture](extension-sdk-architecture.md)
- [Extension SDK Packaging](extension-sdk-packaging.md)
- [Capability Matrix and Host Permissions](capability-matrix-and-host-permissions.md)
- [`TExtension` Package Format](textension-package-format.md)
