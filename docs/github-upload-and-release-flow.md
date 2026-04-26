# GitHub Upload and Release Flow

This repository has already diverged heavily from its earliest state. Treat the launcher repository root as the only Git repository for launcher code. Normal launcher releases should be uploaded as new commits on top of the current history. If you deliberately want GitHub `main` to become an exact mirror of the local launcher repository, use the explicit force-push flow below.

Do not create a second nested repository inside `TLauncher-Compose/`.

## Repository Boundary

The only Git repository should be:

- `D:\Dev\TLauncher-Compose`

The launcher Gradle project now lives directly at that repository root.

## Safe Upload Rule

If you want the new upload to stay clearly separated from the first commit, do this:

1. keep the existing `.git` directory at the repository root
2. make sure there are no nested `.git` directories inside subfolders
3. commit the current repository state as a new commit
4. push that commit to the remote repository

That preserves history correctly. GitHub will show the old commit and the new commit as two different snapshots in the same timeline, and files that were removed, renamed, or replaced will remain traceable instead of becoming mixed with an unrelated fresh repository.

## Recommended Command Flow

Run from:

```powershell
cd D:\Dev\TLauncher-Compose
```

Check the current remote:

```powershell
git remote -v
```

If the GitHub remote has not been added yet:

```powershell
git remote add origin git@github.com:dreamyloong2005/TLauncher-Compose.git
```

Fetch the remote state:

```powershell
git fetch origin
```

Check what will be committed:

```powershell
git status
git diff --stat
```

Create a new commit:

```powershell
git add .
git commit -m "Release 1.0.0"
```

Push to the main branch:

```powershell
git push -u origin main
```

If the remote `main` history must be replaced by the local `main` snapshot, use
the safer force form after fetching the remote:

```powershell
git fetch origin main
git push --force-with-lease origin main:main
```

## Full Build Before Publishing

Run the release-style launcher build from `D:\Dev\TLauncher-Compose` before pushing a release snapshot.

```powershell
.\gradlew.bat build :composeApp:assembleRelease :composeApp:bundleRelease :composeApp:createReleaseDistributable :composeApp:packageReleaseDistributionForCurrentOS packageExtensionSdkMaven --no-daemon --no-configuration-cache
```

For a faster preflight during active development:

```powershell
.\gradlew.bat :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinWindows --no-daemon --no-configuration-cache
```

## If You Want a Clean Separation From Earlier Experimental History

If your local repository history is messy and you want GitHub to clearly reflect the current project as a fresh authoritative line, use one of these two strategies.

### Strategy A: keep history

Use the normal flow above.

This is the best choice when:

- the earlier commits still matter
- you want GitHub blame and rename tracking to remain useful
- you want to show the real evolution from the first commit to now

### Strategy B: replace remote history intentionally

Only do this if you truly want the old remote history to stop being the canonical line.

Steps:

1. back up the repository first
2. confirm the current local tree is exactly what you want
3. force-push the current local state to `main`

```powershell
git push -f origin HEAD:main
```

This does not mix the two uploads. It replaces the remote `main` branch tip entirely. The old commits stop being the branch history unless separately preserved by tags or other refs.

If the goal is to clear GitHub `main` and replace it with the local `main`, the practical command is:

```powershell
git push --force origin main:main
```

Use this only after confirming `git status`, the latest commit, and the full build result.

## Recommended Choice For This Repository

For this project, the safer default is:

- keep the existing repository history
- make a new commit for the current architecture
- tag the new state as a release point

Example:

```powershell
git tag v1.0.0
git push origin v1.0.0
```

That gives you a clean milestone without losing the earlier history.

## Release Artifacts

Typical outputs for this repository are:

- Android debug APK from `composeApp/build/outputs/apk/debug/`
- Android release APK from `composeApp/build/outputs/apk/release/`
- Android release bundle from `composeApp/build/outputs/bundle/release/`
- Windows portable directory from `composeApp/build/compose/binaries/main-release/app/`
- Windows release installers from `composeApp/build/compose/binaries/main-release/exe/` and `composeApp/build/compose/binaries/main-release/msi/`
- Extension SDK Maven repository zip from `build/dist/tlauncher-extension-sdk-1.0.0-maven.zip`

For the current release line, the launcher version is `1.0.0`, and the SDK API artifact version is `1.0.0`.

Developers who want to build extensions should download the SDK Maven repository zip. Users who want to install an extension should download that extension's `TExtension` package.

This release line includes Windows-native Common Item Dialog file, extension
package, and directory pickers.

This release line also exposes CAuth-backed `SteamDepotService` through `ExtensionContext.hostServices` on Android and Windows. Extension packages should not ship launcher-owned CAuth binaries.

Upload release artifacts to GitHub Releases if you want users to download binaries without cloning the repository.

Recommended release build command from the repository root:

```powershell
.\gradlew.bat build :composeApp:assembleRelease :composeApp:bundleRelease :composeApp:createReleaseDistributable :composeApp:packageReleaseDistributionForCurrentOS packageExtensionSdkMaven --no-daemon --no-configuration-cache
```
