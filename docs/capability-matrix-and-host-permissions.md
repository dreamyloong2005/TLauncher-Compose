# Capability Matrix and Host Permission Model

This document formalizes two separate but related concepts:

1. capability declarations
2. host permission grants

They are not the same thing.

## Shared Terminology

Canonical reference:

- [SDK Glossary](sdk-glossary.md)

- `kind`: the primary category of an extension, currently `TEMPLATE`, `THEME`, or `PLUGIN`
- `capability`: an architectural authority declared by an extension manifest
- `permissionKey`: a manifest-time request for sensitive host access
- `HostGrant`: the host's persisted decision for a requested `permissionKey`
- `HostGrantState`: the current decision state for a `HostGrant`

## 1. Conceptual Split

### Capability

A capability answers:

- what category of behavior an extension is allowed to participate in

Examples:

- contribute page content
- mutate a page tree
- intercept game launch
- read saved game instance state

Capabilities define architectural authority.

### Host Permission Request

A host permission request answers:

- which sensitive host resource access the launcher is willing to grant

Examples:

- network access
- filesystem read
- filesystem write
- read saved game instance state
- write saved game instance state

In the manifest, these appear as `permissionKeys`.

Requested host permissions define trust intent and resource access needs.

### Host Grant

A `HostGrant` answers:

- what the host actually decided for a requested `permissionKey`

Examples:

- `NETWORK_ACCESS` requested, `GRANTED`
- `FILESYSTEM_WRITE` requested, `DENIED`

`HostGrant` is the runtime and persisted decision model. It is separate from the manifest declaration.

An extension must not gain sensitive resource access only because it implements a feature interface.

### Host Service Facade

A host service facade answers:

- which launcher-owned operation the host intentionally exposes through the public SDK

Examples:

- platform or native operations owned by the launcher
- account/session-backed services owned by the launcher
- service wrappers around vendored host libraries
- CAuth-backed Steam depot operations exposed as `SteamDepotService` on Android and Windows

Host services are exposed through `ExtensionContext.hostServices`. They are not raw permission grants and they are not a reason for an extension to import `core.*` or bundle host-owned implementation libraries.

## 2. Kind Boundary

The primary extension kind still comes first:

- `TEMPLATE`
- `THEME`
- `PLUGIN`

Kind is the outer boundary.

Capabilities are the second boundary.

Requested host permissions are the third boundary.

Granted host permissions are the runtime enforcement result.

## 3. Capability Matrix

### Template

Allowed capabilities:

- `DEFINE_TEMPLATE_METADATA`
- `DEFINE_TEMPLATE_RUNTIME_REQUIREMENTS`
- `PROVIDE_TEMPLATE_PAGE_CONTRIBUTIONS`
- `PROVIDE_TEMPLATE_SETTINGS_CONTRIBUTIONS`

Forbidden categories:

- arbitrary page mutation
- launcher action interception
- game launch interception outside template preparation hooks
- host resource permissions

### Theme

Allowed capabilities:

- `DEFINE_THEME_METADATA`
- `DEFINE_THEME_TOKENS`
- `DEFINE_THEME_SCENE`

Forbidden categories:

- saved game instance access
- launcher behavior interception
- host filesystem or network permissions
- general-purpose logic injection

### Plugin

Allowed capabilities:

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

Plugins are the only extension kind allowed to request host permissions.

## 4. Host Permission Keys

Current host permission keys:

- `READ_GAME_LIBRARY`
- `WRITE_GAME_LIBRARY`
- `OPEN_EXTERNAL_URLS`
- `NETWORK_ACCESS`
- `FILESYSTEM_READ`
- `FILESYSTEM_WRITE`

These keys are represented in code by:

- [HostPermissions.kt](../shared/src/commonMain/kotlin/com/dreamyloong/tlauncher/core/extension/HostPermissions.kt)

## 5. Capability to Permission Mapping

Some plugin capabilities are sensitive enough that they must also declare a matching host permission key.

Current required mappings:

- `READ_GAME_LIBRARY` capability -> `READ_GAME_LIBRARY` permission
- `WRITE_GAME_LIBRARY` capability -> `WRITE_GAME_LIBRARY` permission
- `OPEN_EXTERNAL_URLS` capability -> `OPEN_EXTERNAL_URLS` permission
- `NETWORK_ACCESS` capability -> `NETWORK_ACCESS` permission
- `FILESYSTEM_READ` capability -> `FILESYSTEM_READ` permission
- `FILESYSTEM_WRITE` capability -> `FILESYSTEM_WRITE` permission

This means:

- capabilities define what an extension can attempt
- `permissionKeys` define which sensitive resources the host may grant

Both declarations are required for third-party plugins when sensitive resource access is involved.

That still does not grant the access. A matching `HostGrant` decision is also required at runtime.

## 6. Validation Order

The host should validate in this order:

1. extension `kind`
2. capability set allowed for that `kind`
3. `permissionKeys` allowed for that `kind`
4. required `permissionKeys` implied by capabilities
5. registered feature interfaces allowed for that `kind`
6. required capabilities implied by registered features

The current code already enforces this in two places:

- package parsing
- registry registration

Relevant files:

- [TExtensionPackageParser.kt](../shared/src/commonMain/kotlin/com/dreamyloong/tlauncher/core/extension/TExtensionPackageParser.kt)
- [ExtensionFeaturePolicy.kt](../shared/src/commonMain/kotlin/com/dreamyloong/tlauncher/core/extension/ExtensionFeaturePolicy.kt)

## 7. HostGrant Model

Runtime host grant state is represented by:

- `HostGrant`
- `HostGrantState`

Current states:

- `GRANTED`
- `DENIED`

This is intentionally separate from the manifest. A package may request a `permissionKey`, but the host still records the actual runtime result as a grant decision.

## 8. Current Runtime Policy

Current implementation status is already executable:

- `TExtension` packages can declare `permissionKeys`
- the host persists `HostGrant` state by extension source identity
- plugins do not receive implicit grants from host policy
- third-party packages can remain pending or denied until permission review is completed
- runtime loading is gated on the current review result for requested permissions
- host-owned services are exposed through typed SDK facades instead of raw implementation classes
- installed package failures are visible in Extension Manager, and successful compatible replacement clears stale failure state

## 9. Final Intended Policy

The intended launcher policy is:

1. show requested `permissionKeys` before install or enable
2. allow per-permission grant and deny decisions
3. persist grant state by extension package identity
4. expose granted permissions through extension context only
5. keep sensitive host operations behind host-owned facades rather than raw global APIs

In other words:

- extensions declare intent
- the host grants access
- features do not bypass host grant state

## 10. Design Rule

The launcher should remain default-deny for sensitive resources.

The practical rule is:

- no `permissionKey` declared -> no sensitive resource access
- no host grant recorded -> no sensitive resource access
- unsupported `kind` + capability combination -> registration failure
- unsupported `kind` + feature combination -> registration failure
