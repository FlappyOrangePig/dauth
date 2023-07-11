package com.cyberflow.dauthsdk

import android.app.Application
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.SdkConfig

private const val CONSUMER_KEY = "2tUyK3TbbjxHPUHOP25OnSL0r"
private const val CONSUMER_SECRET = "p9bAQDBtlNPdNiTQuMM8yLJuwwDsVCf8QZl2rRRa4eqHVIBFHs"
private const val CLIENT_ID = "e2fc714c4727ee9395f324cd2e7f331f"
private const val CLIENT_SECRET = "4657*@cde"
private const val GOOGLE_CLIENT_ID = "209392989758-svboqpe2r94q1p3vg9qrkgcgfuemnakk.apps.googleusercontent.com"
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
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
            useBuiltInAppServerUrl = false
            alwaysCreateKey = false
            googleClientId = GOOGLE_CLIENT_ID
        }
        DAuthSDK.instance.initSDK(this, config = config)
    }
}