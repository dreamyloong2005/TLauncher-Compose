# CAuth Vendor Artifacts

This directory stores the current prebuilt CAuth artifacts consumed by TLauncher.

Build CAuth in its own workspace, then copy only the runtime artifacts here:

```text
tlauncher-compose/vendor/cauth/
  android/
    libs/
      cauth-android-core.jar
      cauth-android-steam-auth.jar
      cauth-android-steam-cloud.jar
      cauth-android-steam-depot.jar
    jniLibs/
      arm64-v8a/
        libc++_shared.so
        libcauth_android_core_jni.so
        libcauth_android_steam_auth_jni.so
        libcauth_android_steam_cloud_jni.so
        libcauth_android_steam_depot_jni.so
        libcauth_core_ffi.so
        libcauth_steam_auth_ffi.so
        libcauth_steam_cloud_ffi.so
        libcauth_steam_depot_ffi.so
  windows/
    x64/
      cauth_core_ffi.dll
      cauth_steam_auth_ffi.dll
      cauth_steam_cloud_ffi.dll
      cauth_steam_depot_ffi.dll
```

The launcher Gradle build must not include or build the CAuth source tree.

The current launcher integration uses `cauth_core`, `cauth_steam_auth`,
`cauth_steam_cloud`, and `cauth_steam_depot`.

The vendored CAuth artifacts include native resumable transfer support and
native login cancellation support. The launcher account service, Steam auth
flow, and Steam depot and cloud operations can benefit from checkpointed
transfer state and cancellation without vendoring or compiling the full
upstream CAuth source tree.

Windows bindings use newer core symbols such as
`cauth_client_create_with_options`, `cauth_session_list_saved`, and
`cauth_session_clear_account`, plus `cauth_auth_request_login_cancel` from
Steam auth. If Windows reports an `UnsatisfiedLinkError` for one of those
symbols, refresh the four DLLs under `windows/x64/` from the latest successful
standalone CAuth build, then run `:composeApp:windowsProcessResources` or a
Windows package task so the updated DLLs are embedded as resources.

Because these are checked in as jar files instead of Gradle project
dependencies, the launcher build declares the Android runtime dependencies
from CAuth manually.
