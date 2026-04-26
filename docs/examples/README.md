# SDK Examples

This directory contains copyable build examples for third-party extension projects.

Canonical terminology:

- [SDK Glossary](../sdk-glossary.md)

## Files

- [textension-plugin-build.gradle.kts](textension-plugin-build.gradle.kts)
- [minimal-plugin-entrypoint.kt](minimal-plugin-entrypoint.kt)

## What The Build Example Shows

The current example demonstrates a minimal JVM-first `PLUGIN` packaging flow for the `WINDOWS` target:

1. depend on the SDK API at compile time
2. build a runtime JAR
3. generate `manifest.json`
4. package the runtime artifact and manifest into `TExtension`

The examples target SDK API artifact `1.0.0`, host SDK compatibility version `1`, and package format version `1`. Their manifests declare both `minSdkApiVersion` and `targetSdkApiVersion` so the host can evaluate compatibility the same way Android evaluates SDK ranges.

It is intentionally small and should be read together with:

- [minimal-plugin-entrypoint.kt](minimal-plugin-entrypoint.kt)
- [How to Build a `TExtension`](../how-to-build-a-textension.md)
- [Extension SDK Packaging](../extension-sdk-packaging.md)

## Why There Are No Live Sample Modules

The repository no longer ships compileable sample extension subprojects under `samples/`.

That directory was retired because executable sample builds kept lagging behind the real SDK and host contracts. The supported path now is:

1. copy the build template from this folder
2. copy the minimal entrypoint shape from this folder
3. create your extension in its own repository or workspace
