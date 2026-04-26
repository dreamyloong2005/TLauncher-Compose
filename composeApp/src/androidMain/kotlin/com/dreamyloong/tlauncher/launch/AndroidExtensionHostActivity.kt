package com.dreamyloong.tlauncher.launch

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commitNow
import com.dreamyloong.tlauncher.core.platform.AndroidExtensionHostController
import com.dreamyloong.tlauncher.core.platform.GameLaunchRequest
import com.dreamyloong.tlauncher.ui.SystemBarMode
import com.dreamyloong.tlauncher.ui.applyTLauncherSystemBars
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AndroidExtensionHostActivity : FragmentActivity(), AndroidExtensionHostController {
    private val payload: AndroidGameLaunchPayload by lazy {
        AndroidGameLaunchPayload.fromIntent(intent)
    }

    private var exitDialog: AlertDialog? = null
    private var preparedRuntime: PreparedAndroidTExtensionRuntime? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        instance = this
        syncAndroidLaunchContextEnvironment(this, payload)
        initializeHostRuntime()
        installBackHandler()
        super.onCreate(savedInstanceState)
        applyTLauncherSystemBars(SystemBarMode.GameHost)
        setContentView(createFragmentContainer())
        if (savedInstanceState == null) {
            val runtime = initializeHostRuntime()
            supportFragmentManager.commitNow {
                replace(
                    com.dreamyloong.tlauncher.R.id.game_host_container,
                    AndroidTExtensionRuntime.createHostFragment(runtime, payload),
                )
            }
        }
        Log.i(TAG, "Starting Android extension host for ${payload.gameDisplayName}")
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    @SuppressLint("GestureBackNavigation", "MissingSuperCall")
    override fun onBackPressed() {
        requestCloseHostedRuntime()
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            requestCloseHostedRuntime()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onDestroy() {
        exitDialog?.dismiss()
        exitDialog = null
        closeHostRuntimeIfNeeded()
        if (instance === this) {
            instance = null
        }
        super.onDestroy()
    }

    override fun requestCloseHostedRuntime() {
        if (exitDialog?.isShowing == true || isFinishing) {
            return
        }
        val useChinese = currentLocaleLanguage() == "zh"
        exitDialog = MaterialAlertDialogBuilder(this)
            .setTitle(if (useChinese) "关闭游戏？" else "Close game?")
            .setMessage(
                if (useChinese) {
                    "当前游戏将会退出并返回启动器。"
                } else {
                    "The current game will close and return to the launcher."
                },
            )
            .setNegativeButton(if (useChinese) "继续游戏" else "Keep playing") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(if (useChinese) "关闭" else "Close") { _, _ ->
                closeHostedRuntimeAndReturnToLauncher()
            }
            .setOnDismissListener {
                exitDialog = null
            }
            .show()
    }

    override fun closeHostedRuntimeAndReturnToLauncher() {
        Log.i(TAG, "Closing Android extension host and returning to launcher")
        exitDialog?.dismiss()
        exitDialog = null
        closeHostRuntimeIfNeeded()
        packageManager.getLaunchIntentForPackage(packageName)?.let { launcherIntent ->
            launcherIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP,
            )
            startActivity(launcherIntent)
        }
        finishAndRemoveTask()
        terminateGameProcess()
    }

    override fun restartHostedRuntime() {
        Log.i(TAG, "Restarting Android extension host")
        exitDialog?.dismiss()
        exitDialog = null
        val relaunchIntent = intent?.let(::Intent)
            ?: intentFor(this, payload)
        finish()
        startActivity(relaunchIntent)
    }

    override fun restartLauncherFromHostedRuntime() {
        runOnUiThread {
            closeHostedRuntimeAndReturnToLauncher()
        }
    }

    private fun initializeHostRuntime(): PreparedAndroidTExtensionRuntime {
        return preparedRuntime ?: AndroidTExtensionRuntime.prepare(this, payload).also { runtime ->
            AndroidTExtensionRuntime.onHostActivityCreated(this, runtime)
            preparedRuntime = runtime
        }.also {
            Log.i(TAG, "Android extension runtime initialized")
        }
    }

    private fun closeHostRuntimeIfNeeded() {
        preparedRuntime?.let { runtime ->
            AndroidTExtensionRuntime.onHostActivityDestroyed(this, runtime)
            preparedRuntime = null
            Log.i(TAG, "Android extension runtime closed")
        }
    }

    private fun terminateGameProcess() {
        val currentPid = android.os.Process.myPid()
        Handler(Looper.getMainLooper()).postDelayed(
            {
                Log.i(TAG, "Terminating Android game process pid=$currentPid")
                android.os.Process.killProcess(currentPid)
                kotlin.system.exitProcess(0)
            },
            PROCESS_TERMINATION_DELAY_MS,
        )
    }

    private fun installBackHandler() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requestCloseHostedRuntime()
                }
            },
        )
    }

    private fun createFragmentContainer(): androidx.fragment.app.FragmentContainerView {
        return androidx.fragment.app.FragmentContainerView(this).apply {
            id = com.dreamyloong.tlauncher.R.id.game_host_container
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
    }

    private fun currentLocaleLanguage(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales[0]?.language.orEmpty()
        } else {
            @Suppress("DEPRECATION")
            resources.configuration.locale?.language.orEmpty()
        }
    }

    companion object {
        private const val TAG = "AndroidExtensionHost"
        private const val PROCESS_TERMINATION_DELAY_MS = 200L
        private var instance: AndroidExtensionHostActivity? = null

        @JvmStatic
        fun getInstance(): AndroidExtensionHostActivity? = instance

        internal fun intentFor(
            context: Context,
            request: GameLaunchRequest.AndroidRuntime,
        ): Intent {
            return intentFor(
                context = context,
                payload = AndroidGameLaunchPayload.fromRequest(request),
            )
        }

        internal fun intentFor(
            context: Context,
            payload: AndroidGameLaunchPayload,
        ): Intent {
            return payload.toIntent(Intent(context, AndroidExtensionHostActivity::class.java)).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        }
    }
}
