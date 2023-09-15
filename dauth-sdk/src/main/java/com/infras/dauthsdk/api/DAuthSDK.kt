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
import com.infras.dauthsdk.wallet.impl.manager.Managers
import com.infras.dauthsdk.wallet.util.DebugUtil
import com.infras.dauthsdk.wallet.util.KeystoreUtil

class DAuthSDK private constructor(
    internal val loginApi: DAuthLogin,
    private val aaWalletApi: AAWalletImpl,
) : IDAuthApi, ILoginApi by loginApi, IAAWalletApi by aaWalletApi {

    companion object {
        val instance: IDAuthApi get() = impl
        internal val impl: DAuthSDK by lazy {
            DAuthSDK(DAuthLogin(), AAWalletImpl())
        }
    }

    private var _config: SdkConfig? = null
    internal val config get() = _config ?: throw RuntimeException("please call initSDK() first")

    override fun initSDK(context: Context, config: SdkConfig) {
        val appContext = context.applicationContext as Application
        this._config = config
        Managers.inject(appContext)
        loginApi.initSDK(context, config)
        KeystoreUtil.setupBouncyCastle()
        Managers.preGenerateKeyManager.initialize()
        Managers.statsManager.initialize()
        DAuthLogger.i("init sdk ok")
        printSdkVersion(appContext)
        printDebugInfo(appContext)
    }

    override fun getEoaApi(): IEoaWalletApi {
        return Managers.eoaWalletApi
    }

    override fun getFiatApi(): IFiatApi {
        return Managers.fiatApi
    }

    @VisibleForTesting
    fun initSDKForTest(context: Context, config: SdkConfig) {
        Managers.context = context
        this._config = config
    }

    private fun printSdkVersion(context: Context) {
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

    private fun printDebugInfo(context: Context) {
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