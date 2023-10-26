package com.infras.dauthsdk.wallet.impl.manager

import android.content.Context
import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.login.utils.LoginPrefs
import com.infras.dauthsdk.wallet.ext.getMetaData
import com.infras.dauthsdk.wallet.ext.runCatchingWithLog
import java.io.File


class SdkVersionBumpManager(
    private val context: Context,
    private val globalPrefsManager: GlobalPrefsManager,
) {

    private companion object {
        private const val TAG = "SdkVersionBumpManager"
    }

    private val initCaller by lazy {
        InitCaller()
    }

    fun initialize() {
        initCaller
    }

    private fun initInner() {
        val oldVersion = globalPrefsManager.getVersion()
        val newVersion = context.getMetaData("DAUTH_VERSION")
        DAuthLogger.i("init $oldVersion -> $newVersion", TAG)
        if (oldVersion != newVersion) {
            globalPrefsManager.setSdkVersion(newVersion)
            onUpgrade()
        }
    }

    private inner class InitCaller {
        init {
            initInner()
        }
    }

    private fun onUpgrade() {
        DAuthLogger.i("on upgrade", TAG)
        removeWalletsAndLoginStateInfoPrefs()
    }

    private fun removeWalletsAndLoginStateInfoPrefs() {
        DAuthLogger.i("removeWalletsAndLoginStateInfoPrefs", TAG)
        val packageName = context.packageName
        val prefsPath = "/data/data/$packageName/shared_prefs/"
        val prefsDir = File(prefsPath)
        if (!prefsDir.isDirectory) {
            DAuthLogger.e("$prefsDir is not dir", TAG)
            return
        }
        prefsDir.listFiles().orEmpty().forEach { file ->
            if (file.isFile) {
                val name = file.name
                val delete = when {
                    name == "${LoginPrefs.LOGIN_STATE_INFO}.xml" -> true
                    name.startsWith("walletV2-") && name.endsWith(".xml") -> true
                    else -> false
                }
                if (delete) {
                    val deleteResult = runCatchingWithLog {
                        file.delete()
                    }
                    DAuthLogger.i("delete ${file.absolutePath} $deleteResult", TAG)
                }
            }
        }
    }
}