package com.cyberflow.dauthsdk

import android.app.Application
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.SdkConfig

private const val CONSUMER_KEY = "2tUyK3TbbjxHPUHOP25OnSL0r"
private const val CONSUMER_SECRET = "p9bAQDBtlNPdNiTQuMM8yLJuwwDsVCf8QZl2rRRa4eqHVIBFHs"
private const val CLIENT_ID = "e2fc714c4727ee9395f324cd2e7f331f"
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val config = SdkConfig().apply {
            twitterConsumerKey = CONSUMER_KEY
            twitterConsumerSecret = CONSUMER_SECRET
            clientId = CLIENT_ID
            isLogOpen = true
            localSign = false
            useLocalRelayer = false
            useDevWebSocketServer = false
            useDevRelayerServer = false
        }
        DAuthSDK.instance.initSDK(this, config = config)
    }
}