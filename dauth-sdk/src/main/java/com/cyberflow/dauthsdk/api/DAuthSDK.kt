package com.cyberflow.dauthsdk.api


import android.app.Application
import android.content.Context
import com.cyberflow.dauthsdk.login.impl.DAuthLogin
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.wallet.impl.WalletHolder
import com.example.hellojni.HelloJni


class DAuthSDK private constructor(
    private val loginApi: ILoginApi,
    private val walletApi: IWalletApi
) : IDAuthApi, ILoginApi by loginApi,
    IWalletApi by walletApi {

    companion object {
        val instance: IDAuthApi get() = impl
        val impl: DAuthSDK by lazy {
            DAuthSDK(DAuthLogin.instance, WalletHolder.walletApi)
        }
    }

    private var _context: Context? = null
    internal val context get() = _context ?: throw RuntimeException("please call initSDK() first")
    private var _config: SdkConfig? = null
    internal val config get() = _config ?: throw RuntimeException("please call initSDK() first")

    override fun initSDK(context: Context, config: SdkConfig) {
        DAuthLogger.i("init sdk")
        val appContext = context.applicationContext as Application
        this._context = appContext
        this._config = config
        initializeCheck()
        loginApi.initSDK(context, config)
        initWallet(appContext)
        DAuthLogger.i("init sdk ok")
    }

    private fun initializeCheck() {
        context
        if (config.chains.isEmpty()) {
            throw IllegalArgumentException("must add a chain at least")
        }
    }

    override fun initWallet(context: Context) {
        DAuthLogger.d(HelloJni().stringFromJNI().orEmpty(), "JNI")
    }
}