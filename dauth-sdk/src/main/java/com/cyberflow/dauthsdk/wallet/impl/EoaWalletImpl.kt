package com.cyberflow.dauthsdk.wallet.impl

import com.cyberflow.dauthsdk.api.IEoaWalletApi
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.EstimateGasData
import com.cyberflow.dauthsdk.api.entity.SendTransactionData
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.wallet.ext.runCatchingWithLogSuspend
import com.cyberflow.dauthsdk.wallet.impl.manager.Managers
import com.cyberflow.dauthsdk.wallet.util.CredentialsUtil
import java.math.BigInteger

private const val TAG = "EoaWalletImpl"

internal class EoaWalletImpl internal constructor() : IEoaWalletApi {

    private val web3m get() = Managers.web3m
    private val connectManager get() = Managers.connectManager

    override suspend fun connectWallet(): Boolean {
        return runCatchingWithLogSuspend { connectManager.connect() } ?: false
    }

    override suspend fun getEoaWalletAddress(): DAuthResult<String> {
        val credential = CredentialsUtil.loadCredentials(false)
        return DAuthResult.Success(credential.address)
    }

    override suspend fun estimateGas(
        toUserId: String,
        amount: BigInteger
    ): DAuthResult<EstimateGasData> {
        val addressResult = getEoaWalletAddress()
        if (addressResult !is DAuthResult.Success) {
            return DAuthResult.SdkError(DAuthResult.SDK_ERROR_CANNOT_GET_ADDRESS)
        }
        val address = addressResult.data
        return web3m.estimateGas(address, toUserId, amount).also {
            DAuthLogger.d("estimateGas from=$address to=$toUserId amount=$amount result=$it", TAG)
        }
    }

    override suspend fun sendTransaction(
        toAddress: String,
        amount: BigInteger
    ): DAuthResult<SendTransactionData> {
        DAuthLogger.d("sendTransaction $toAddress $amount", TAG)
        return web3m.sendTransaction(toAddress, amount).also {
            DAuthLogger.i("sendTransaction to=$toAddress amount=$amount result=$it", TAG)
        }
    }
}