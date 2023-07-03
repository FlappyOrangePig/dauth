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
            chain = SdkConfig.ChainInfo(
                // seplia-test
                //rpcUrl = "https://rpc.sepolia.org/",

                // 本地服务器
                rpcUrl = Web3Const.RPC_URL

                // ETMP-live
                //rpcUrl = "https://rpc.etm.network",
            )
        }
        DAuthSDK.instance.initSDK(this, config = config)
    }
}