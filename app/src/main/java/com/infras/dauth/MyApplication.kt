package com.infras.dauth

import android.app.Application
import com.infras.dauth.BuildConfig
import com.infras.dauthsdk.api.DAuthChainEnum
import com.infras.dauthsdk.api.DAuthSDK
import com.infras.dauthsdk.api.DAuthStageEnum
import com.infras.dauthsdk.api.SdkConfig
import com.infras.dauth.manager.AccountManager
import com.infras.dauth.util.LogUtil
import com.infras.dauthsdk.api.DAuthLogLevel

private const val CONSUMER_KEY = "2tUyK3TbbjxHPUHOP25OnSL0r"
private const val CONSUMER_SECRET = "p9bAQDBtlNPdNiTQuMM8yLJuwwDsVCf8QZl2rRRa4eqHVIBFHs"
private const val GOOGLE_CLIENT_ID = "209392989758-svboqpe2r94q1p3vg9qrkgcgfuemnakk.apps.googleusercontent.com"

private const val TAG = "MyApplication"

class MyApplication : Application() {

    companion object {
        lateinit var app: MyApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()

        val configStage =
            if (BuildConfig.IS_LIVE) DAuthStageEnum.STAGE_LIVE else DAuthStageEnum.STAGE_TEST
        val configChain =
            if (BuildConfig.IS_LIVE) DAuthChainEnum.CHAIN_ARBITRUM else DAuthChainEnum.CHAIN_ARBITRUM_GOERLI

        runSpending(TAG, "启动耗时") {
            app = this
            val config = SdkConfig().apply {
                stage = configStage
                chain = configChain
                clientId = BuildConfig.CLIENT_ID
                clientSecret = BuildConfig.CLIENT_SECRET
                twitterConsumerKey = CONSUMER_KEY
                twitterConsumerSecret = CONSUMER_SECRET
                consoleLogLevel = DAuthLogLevel.LEVEL_DEBUG
                isLogOpen = true
                localSign = false
                useLocalRelayer = false
                useDevWebSocketServer = false
                useDevRelayerServer = false
                googleClientId = GOOGLE_CLIENT_ID
            }
            val sdk = DAuthSDK.instance
            sdk.initSDK(this, config = config)
            AccountManager.attachSdk(sdk)
        }
    }
}

private fun <T> runSpending(
    tag: String,
    log: String,
    block: () -> T,
): T {
    val finalTag = "DAuth-$tag"
    LogUtil.d("$log >>>", finalTag)
    val start = System.currentTimeMillis()
    val r = block.invoke()
    val spent = System.currentTimeMillis() - start
    LogUtil.d("$log <<< spent $spent", finalTag)
    return r
}