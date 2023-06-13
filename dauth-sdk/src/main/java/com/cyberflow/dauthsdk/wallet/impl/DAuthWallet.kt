package com.cyberflow.dauthsdk.wallet.impl

import android.content.Context
import com.cyberflow.dauthsdk.api.IWalletApi
import com.cyberflow.dauthsdk.api.entity.CreateWalletData
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.EstimateGasData
import com.cyberflow.dauthsdk.api.entity.SendTransactionData
import com.cyberflow.dauthsdk.api.entity.WalletAddressData
import com.cyberflow.dauthsdk.api.entity.WalletBalanceData
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import kotlinx.coroutines.runBlocking
import java.math.BigInteger

/**
 * 苟建的账号: 0xbDA5747bFD65F08deb54cb465eB87D40e51B197E (10000 ETH)
 * Private Key: 0x689af8efa8c651a91ad287602527f3af2fe9f6501a7ac4b061667b5a93e037fd
 * 有钱的账号：0x23618e81E3f5cdF7f54C3d65f7FBc0aBf5B21E8f
 */
class DAuthWallet internal constructor() : IWalletApi {

    override fun initWallet(context: Context) {
    }

    override suspend fun createWallet(passcode: String?): DAuthResult<CreateWalletData> {
        return DAuthResult.SdkError()
    }

    override suspend fun queryWalletAddress(): DAuthResult<WalletAddressData> {
        val address = Web3Manager.invokeGetAcc().orEmpty()
        return if (address.isEmpty()) {
            DAuthResult.SdkError()
        } else {
            DAuthResult.Success(WalletAddressData(address))
        }
    }

    override suspend fun queryWalletBalance(): DAuthResult<WalletBalanceData> {
        val addressResult = queryWalletAddress()
        if (addressResult !is DAuthResult.Success){
            return DAuthResult.SdkError()
        }
        val address = addressResult.data.address
        return Web3Manager.getBalance(address)
    }

    override suspend fun estimateGas(toUserId: String, amount: BigInteger): DAuthResult<EstimateGasData> {
        return DAuthResult.SdkError()
    }

    override suspend fun sendTransaction(toAddress: String, amount: BigInteger): DAuthResult<SendTransactionData> {
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
        return DAuthResult.NetworkError(NullPointerException())
    }

}
