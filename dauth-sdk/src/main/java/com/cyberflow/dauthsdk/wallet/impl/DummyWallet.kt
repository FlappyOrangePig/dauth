package com.cyberflow.dauthsdk.wallet.impl

import android.content.Context
import com.cyberflow.dauthsdk.wallet.api.IWalletApi
import com.cyberflow.dauthsdk.wallet.util.CredentialsUtil
import kotlinx.coroutines.runBlocking
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.core.methods.response.EthEstimateGas
import org.web3j.utils.Numeric
import java.math.BigInteger

class DummyWallet internal constructor(): IWalletApi {
    override fun initWallet(context: Context) {

    }

    override fun createWallet(passcode: String?): Int {
        return 0
    }

    override fun queryWalletAddress(): String {
        return CredentialsUtil.loadCredentials().address
    }

    override fun queryWalletBalance(): BigInteger? {
        return runBlocking {
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
        val address = queryWalletAddress()
        return runBlocking {
            Web3Manager.estimateGas(address, toUserId, amount)
        }
    }

    override fun sendTransaction(toAddress: String, amount: BigInteger) {
        Web3Manager.sendTransaction(toAddress, amount)
    }
}