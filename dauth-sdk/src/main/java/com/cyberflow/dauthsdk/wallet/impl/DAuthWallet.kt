package com.cyberflow.dauthsdk.wallet.impl

import android.content.Context
import android.util.Log
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.wallet.api.IWalletApi
import com.cyberflow.dauthsdk.wallet.util.KeystoreUtil
import kotlinx.coroutines.*
import java.math.BigInteger

/**
 * 苟建的账号: 0xbDA5747bFD65F08deb54cb465eB87D40e51B197E (10000 ETH)
 * Private Key: 0x689af8efa8c651a91ad287602527f3af2fe9f6501a7ac4b061667b5a93e037fd
 * 有钱的账号：0x23618e81E3f5cdF7f54C3d65f7FBc0aBf5B21E8f
 */
class DAuthWallet internal constructor() : IWalletApi {

    override fun initWallet(context: Context) {
    }

    override fun createWallet(passcode: String?): Int {
        TODO("Not yet implemented")
    }

    override fun queryWalletAddress(): String {
        return runBlocking {
            Web3Manager.invokeGetAcc().orEmpty()
        }
    }

    override fun queryWalletBalance(): BigInteger? {
        return runBlocking {
            //val address = WalletPrefs(context).getWalletAddress()
            val address = queryWalletAddress()
            if (address.isNotEmpty()) {
                val big = Web3Manager.getBalance(address)
                big
            } else {
                null
            }
        }
    }

    override fun estimateGas(toUserId: String, amount: BigInteger): BigInteger? {
        runBlocking{

        }
        return null
    }

    override fun sendTransaction(toAddress: String, amount: BigInteger) {
        runBlocking {
            val address = queryWalletAddress()
            val queryAddress = toAddress
            DAuthLogger.d("sendTransaction: $queryAddress")
            if (queryAddress.isNotEmpty()) {
                val tx = Web3Manager.invokeTestTemp(
                    queryAddress,
                    amount
                )
                DAuthLogger.d("tx: $tx")
            }
        }
    }

}