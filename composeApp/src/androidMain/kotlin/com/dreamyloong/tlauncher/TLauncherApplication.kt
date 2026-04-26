package com.dreamyloong.tlauncher

import android.app.Application
import com.cauth.android.CAuthAndroidRuntime
import com.dreamyloong.tlauncher.core.platform.initializeAndroidLauncherStorage

class TLauncherApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeAndroidLauncherStorage(applicationContext)
        CAuthAndroidRuntime.attach(applicationContext)
    }
}
