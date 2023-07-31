package com.cyberflow.dauthsdk

import android.app.Application
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.SdkConfig
import com.cyberflow.dauthsdk.manager.AccountManager

private const val CONSUMER_KEY = "2tUyK3TbbjxHPUHOP25OnSL0r"
private const val CONSUMER_SECRET = "p9bAQDBtlNPdNiTQuMM8yLJuwwDsVCf8QZl2rRRa4eqHVIBFHs"
private const val CLIENT_ID = "b86df4abc13f3bba4d4b2057bc6df910"
private const val CLIENT_SECRET= "abc123&*@cde"
private const val GOOGLE_CLIENT_ID = "209392989758-svboqpe2r94q1p3vg9qrkgcgfuemnakk.apps.googleusercontent.com"
class MyApplication : Application() {

    companion object {
        lateinit var app: MyApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        val config = SdkConfig().apply {
            twitterConsumerKey = CONSUMER_KEY
            twitterConsumerSecret = CONSUMER_SECRET
            clientId = CLIENT_ID
            clientSecret = CLIENT_SECRET
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