package com.infras.dauthsdk.api

import android.app.Application
import android.content.Context
import androidx.annotation.VisibleForTesting
import com.infras.dauthsdk.login.impl.DAuthLogin
import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.wallet.ext.getMetaData
import com.infras.dauthsdk.wallet.ext.getPackageInfo
import com.infras.dauthsdk.wallet.ext.getVersionCode
import com.infras.dauthsdk.wallet.ext.runCatchingWithLog
import com.infras.dauthsdk.wallet.impl.AAWalletImpl
import com.infras.dauthsdk.wallet.impl.EoaWalletImpl
import com.infras.dauthsdk.wallet.impl.manager.Managers
import com.infras.dauthsdk.wallet.util.DebugUtil
import com.infras.dauthsdk.wallet.util.KeystoreUtil

class DAuthSDK private constructor(
    internal val loginApi: DAuthLogin,
    private val aaWalletApi: AAWalletImpl,
    internal val eoaWalletApi: EoaWalletImpl,
) : com.infras.dauthsdk.api.IDAuthApi, com.infras.dauthsdk.api.ILoginApi by loginApi, com.infras.dauthsdk.api.IAAWalletApi by aaWalletApi {

    companion object {
        val instance: com.infras.dauthsdk.api.IDAuthApi get() = com.infras.dauthsdk.api.DAuthSDK.Companion.impl
        internal val impl: com.infras.dauthsdk.api.DAuthSDK by lazy {
            com.infras.dauthsdk.api.DAuthSDK(DAuthLogin(), AAWalletImpl(), EoaWalletImpl())
        }
    }

    private var _context: Context? = null
    internal val context get() = _context ?: throw RuntimeException("please call initSDK() first")
    private var _config: com.infras.dauthsdk.api.SdkConfig? = null
    internal val config get() = _config ?: throw RuntimeException("please call initSDK() first")

    override fun initSDK(context: Context, config: com.infras.dauthsdk.api.SdkConfig) {
        val appContext = context.applicationContext as Application
        this._context = appContext
        this._config = config
        initializeCheck()
        Managers.inject(appContext)
        loginApi.initSDK(context, config)
        KeystoreUtil.setupBouncyCastle()
        DAuthLogger.i("init sdk ok")
        printSdkVersion()
        printDebugInfo()
    }

    private fun initializeCheck() {
        context
    }

    @VisibleForTesting
    fun initSDKForTest(context: Context, config: com.infras.dauthsdk.api.SdkConfig) {
        this._context = context
        this._config = config
    }

    private fun printSdkVersion() {
        runCatchingWithLog {
            val ai = context.getPackageInfo()
            DAuthLogger.i(
                "**** dauth ${context.getMetaData("DAUTH_VERSION")} ****\n" +
                        "androidVersion=${android.os.Build.VERSION.SDK_INT}\n" +
                        "versionName=${ai?.versionName}\n" +
                        "getVersionCode=${ai?.getVersionCode()}\n"
            )
        }
    }

    private fun printDebugInfo() {
        if (DebugUtil.isAppDebuggable(context)) {
            val loginPrefs = Managers.loginPrefs
            val trace = StringBuilder()
                .appendLine("***** debug info *****")
                .appendLine("authId=${loginPrefs.getAuthId()} ")
                .appendLine("accessToken=${loginPrefs.getAccessToken()}")
            DAuthLogger.d(trace.toString())
        }
    }
}