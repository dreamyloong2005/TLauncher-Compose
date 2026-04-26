package com.dreamyloong.tlauncher

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.dreamyloong.tlauncher.account.rememberAndroidCAuthAccountService
import com.dreamyloong.tlauncher.account.rememberAndroidCAuthHostServices
import com.dreamyloong.tlauncher.app.TLauncherApp
import com.dreamyloong.tlauncher.core.model.PlatformTarget
import com.dreamyloong.tlauncher.core.platform.clearAndroidLauncherActivity
import com.dreamyloong.tlauncher.core.platform.initializeAndroidLauncherActivity
import com.dreamyloong.tlauncher.core.platform.initializeAndroidLauncherStorage
import com.dreamyloong.tlauncher.launch.rememberAndroidGameLaunchState
import com.dreamyloong.tlauncher.ui.SystemBarMode
import com.dreamyloong.tlauncher.ui.applyTLauncherSystemBars

class MainActivity : ComponentActivity() {
    private var externalRefreshTick by mutableIntStateOf(0)
    private var hasHandledInitialResume = false
    private var lastExternalRefreshAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTLauncherSystemBars(SystemBarMode.Launcher)
        initializeAndroidLauncherStorage(applicationContext)
        initializeAndroidLauncherActivity(this)
        setContent {
            TLauncherApp(
                target = PlatformTarget.ANDROID,
                gameLaunchState = rememberAndroidGameLaunchState(),
                accountService = rememberAndroidCAuthAccountService(),
                hostServices = rememberAndroidCAuthHostServices(),
                externalRefreshTick = externalRefreshTick,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        requestExternalRefresh()
    }

    override fun onResume() {
        super.onResume()
        if (hasHandledInitialResume) {
            requestExternalRefresh()
        } else {
            hasHandledInitialResume = true
        }
    }

    override fun onDestroy() {
        clearAndroidLauncherActivity(this)
        super.onDestroy()
    }

    private fun requestExternalRefresh() {
        val now = SystemClock.elapsedRealtime()
        if (now - lastExternalRefreshAt < EXTERNAL_REFRESH_DEBOUNCE_MS) {
            return
        }
        lastExternalRefreshAt = now
        externalRefreshTick += 1
    }

    private companion object {
        private const val EXTERNAL_REFRESH_DEBOUNCE_MS = 300L
    }
}
