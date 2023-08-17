package com.infras.dauth

import android.app.Application
import com.infras.dauth.manager.AccountManager
import com.infras.dauth.util.DAuthEnv
import com.infras.dauth.util.LogUtil
import com.infras.dauthsdk.api.DAuthSDK
import com.infras.dauthsdk.api.SdkConfig
import com.infras.dauthsdk.api.annotation.DAuthLogLevel

private const val CONSUMER_KEY = "2tUyK3TbbjxHPUHOP25OnSL0r"
private const val CONSUMER_SECRET = "p9bAQDBtlNPdNiTQuMM8yLJuwwDsVCf8QZl2rRRa4eqHVIBFHs"
private const val GOOGLE_CLIENT_ID =
    "209392989758-svboqpe2r94q1p3vg9qrkgcgfuemnakk.apps.googleusercontent.com"

class MyApplication : Application() {

    companion object {
        private const val TAG = "MyApplication"
        lateinit var app: MyApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()

        val env = when (BuildConfig.IS_LIVE) {
            true -> DAuthEnv.EnvProd
            false -> DAuthEnv.EnvDev
        }

        runSpending(TAG, "启动耗时") {
            app = this
            val config = SdkConfig().apply {
                stage = env.stage
                chain = env.chain
                clientId = env.clientId
                twitterConsumerKey = CONSUMER_KEY
                twitterConsumerSecret = CONSUMER_SECRET
                consoleLogLevel = DAuthLogLevel.LEVEL_DEBUG
                isLogOpen = true
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