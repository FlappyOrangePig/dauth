package com.cyberflow.dauthsdk

import android.app.Application
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.SdkConfig

private const val CONSUMER_KEY = "lolei76IdILu5LDW0OQsM3iGZ"
private const val CONSUMER_SECRET = "wMKmwqPoYzDqy8mUumm9RnnOIAHWNe4q7XayYmi7QS32wDOcuo"
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val config = SdkConfig().apply {
            twitterConsumerKey = CONSUMER_KEY
            twitterConsumerSecret = CONSUMER_SECRET
            chains = arrayListOf(
                SdkConfig.ChainInfo(
                    info = "sepolia test network",
                    rpcUrl = "https://rpc.sepolia.org/",
                    usdtAddress = "0x6175a8471C2122f778445e7E07A164250a19E661",
                    nftAddress = ""
                )
            )
        }
        DAuthSDK.instance.initSDK(this, config = config)
    }
}