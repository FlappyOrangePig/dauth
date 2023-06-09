package com.cyberflow.dauthsdk


import android.app.Application
import android.content.Context
import com.cyberflow.dauthsdk.login.api.ILoginApi
import com.cyberflow.dauthsdk.login.api.bean.SdkConfig
import com.cyberflow.dauthsdk.login.callback.ThirdPartyCallback
import com.cyberflow.dauthsdk.login.impl.DAuthLifeCycle
import com.cyberflow.dauthsdk.login.impl.LoginHolder
import com.cyberflow.dauthsdk.login.twitter.TwitterLoginManager
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.wallet.api.IWalletApi
import com.cyberflow.dauthsdk.wallet.impl.WalletHolder


class DAuthSDK private constructor() : ILoginApi by LoginHolder.loginApi,
    IWalletApi by WalletHolder.walletApi {

    companion object {
        val instance by lazy {
            DAuthSDK()
        }
        var callback: ThirdPartyCallback? = null
    }

    private var _context: Context? = null
    internal val context get() = _context ?: throw RuntimeException("please call initSDK() first")
    private var _config: SdkConfig? = null
    private val config get() = _config ?: throw RuntimeException("please call initSDK() first")

    override fun initSDK(context: Context, config: SdkConfig) {
        val appContext = context.applicationContext as Application
        this._context = appContext
        this._config = config
        //Twitter初始化
        TwitterLoginManager.instance.initTwitterSDK(context, config)
        initWallet(appContext)
        appContext.registerActivityLifecycleCallbacks(DAuthLifeCycle)
        DAuthLogger.i("init sdk")
    }
}