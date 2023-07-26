package com.cyberflow.dauthsdk.api

import android.app.Application
import android.content.Context
import androidx.annotation.VisibleForTesting
import com.cyberflow.dauthsdk.login.impl.DAuthLogin
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.wallet.ext.getMetaData
import com.cyberflow.dauthsdk.wallet.ext.getPackageInfo
import com.cyberflow.dauthsdk.wallet.ext.getVersionCode
import com.cyberflow.dauthsdk.wallet.ext.runCatchingWithLog
import com.cyberflow.dauthsdk.wallet.impl.WalletHolder
import com.cyberflow.dauthsdk.wallet.impl.manager.Managers
import com.cyberflow.dauthsdk.wallet.util.DebugUtil

class DAuthSDK private constructor(
    internal val loginApi: ILoginApi,
    private val walletApi: IWalletApi
) : IDAuthApi, ILoginApi by loginApi,
    IWalletApi by walletApi {

    companion object {
        val instance: IDAuthApi get() = impl
        internal val impl: DAuthSDK by lazy {
            DAuthSDK(DAuthLogin.instance, WalletHolder.walletApi)
        }
    }

    private var _context: Context? = null
    internal val context get() = _context ?: throw RuntimeException("please call initSDK() first")
    private var _config: SdkConfig? = null
    internal val config get() = _config ?: throw RuntimeException("please call initSDK() first")

    override fun initSDK(context: Context, config: SdkConfig) {
        val appContext = context.applicationContext as Application
        this._context = appContext
        this._config = config
        initializeCheck()
        runCatchingWithLog {
            val ai = context.getPackageInfo()
            DAuthLogger.i(
                "**** dauth ${context.getMetaData("DAUTH_VERSION")} ****\n" +
                        "androidVersion=${android.os.Build.VERSION.SDK_INT}\n" +
                        "versionName=${ai?.versionName}\n" +
                        "getVersionCode=${ai?.getVersionCode()}\n"
            )
        }
        Managers.inject(appContext)
        loginApi.initSDK(context, config)
        DAuthLogger.i("init sdk ok")
        if (DebugUtil.isAppDebuggable(context)) {
            val loginPrefs = Managers.loginPrefs
            val trace = StringBuilder()
                .appendLine("***** debug info *****")
                .appendLine("authId=${loginPrefs.getAuthId()} ")
                .appendLine("accessToken=${loginPrefs.getAccessToken()}")
            DAuthLogger.d(trace.toString())
        }
    }

    private fun initializeCheck() {
        context
    }

    @VisibleForTesting
    fun initSDKForTest(context: Context, config: SdkConfig){
        this._context = context
        this._config = config
    }
}