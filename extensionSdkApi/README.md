# TLauncher Extension API

This module is the stable developer-facing ABI for third-party extension projects.

Current SDK API artifact version: `1.0.0`

## Purpose

`extensionSdkApi` defines the supported import and binary surface for extension authors.

Third-party extensions should prefer:

- `com.dreamyloong.tlauncher.sdk.*`

They should avoid depending directly on:

- `com.dreamyloong.tlauncher.core.*`

The `core.*` packages remain the host implementation space. The `sdk.*` packages are the public ABI.

## Current Shape

The module is self-contained. Its public types are real `sdk.*` declarations, not aliases to launcher-core implementation types:

- extension contracts from `ExtensionSdk.kt`
- host-owned service facades from `host/HostServicesSdk.kt`
- model types
- page contribution DSL
- plugin feature interfaces
- template extension types
- settings contribution types
- theme types
- selected action and platform types, including file pickers, launch requests, and launcher icon control
- SDK compatibility metadata with Android-style minimum and target SDK API fields

The launcher host adapts loaded `sdk.*` runtime objects back into its internal `core.*` model at the runtime boundary.

Extensions should treat host services as typed facades. If a platform operation depends on launcher-owned native code, account state, secure storage, or other host resources, the extension should call the relevant service from `ExtensionContext.hostServices` instead of bundling or importing the host implementation library. For SDK 1.0.0, Android and Windows hosts expose CAuth-backed Steam depot operations through `SteamDepotService`.

## Packaging

Artifact coordinates:

```text
com.dreamyloong.tlauncher:tlauncher-extension-api
```

Current version:

```text
1.0.0
```

Current build command:

```powershell
.\gradlew.bat :extensionSdkApi:build
```

Package the release-ready Maven repository zip:

```powershell
.\gradlew.bat packageExtensionSdkMaven --no-daemon --no-configuration-cache
```

Output:

```text
build/dist/tlauncher-extension-sdk-1.0.0-maven.zip
```

Extension developers can download and extract this zip, then use the extracted directory as a Maven repository for:

```text
com.dreamyloong.tlauncher:tlauncher-extension-api:1.0.0
```

For local workspace testing, publish to Maven local:

```powershell
.\gradlew.bat :extensionSdkApi:publishToMavenLocal --no-daemon --no-configuration-cache
```

## Related Docs

- [SDK Glossary](../docs/sdk-glossary.md)
- [How to Build a `TExtension`](../docs/how-to-build-a-textension.md)
