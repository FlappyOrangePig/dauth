package com.cyberflow.dauthsdk

import android.app.Application
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.SdkConfig

private const val CONSUMER_KEY = "2tUyK3TbbjxHPUHOP25OnSL0r"
private const val CONSUMER_SECRET = "p9bAQDBtlNPdNiTQuMM8yLJuwwDsVCf8QZl2rRRa4eqHVIBFHs"
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