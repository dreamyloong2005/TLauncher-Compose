package com.dreamyloong.tlauncher.core.platform

import okio.FileSystem

expect fun launcherStorageDirectoryPath(): String

expect fun launcherAppFilesDirectoryPath(): String

expect fun launcherPackageName(): String?

expect fun launcherFileSystem(): FileSystem
