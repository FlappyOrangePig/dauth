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
import com.cyberflow.dauthsdk.wallet.util.CredentialsUtil
import java.math.BigInteger

private const val TAG = "EoaWallet"

/**
 * 模拟钱包实现类。
 * 在AA钱包开发完成前先创建假数据。
 */
class EoaWallet internal constructor(): IWalletApi {

    override fun initWallet(context: Context) {

    }

    override suspend fun createWallet(passcode: String?): DAuthResult<CreateWalletData> {
        val credential = CredentialsUtil.loadCredentials(true)
        val address = credential.address
        DAuthLogger.d("createWallet $address", TAG)
        return DAuthResult.Success(CreateWalletData(address))
    }

    override suspend fun queryWalletAddress(): DAuthResult<WalletAddressData> {
        val address = CredentialsUtil.loadCredentials(false).address
        DAuthLogger.i("queryWalletAddress $address", TAG)
        return address?.let { DAuthResult.Success(WalletAddressData(address)) }
            ?: DAuthResult.SdkError(DAuthResult.SDK_ERROR_CANNOT_GET_ADDRESS)
    }

    override suspend fun queryWalletBalance(): DAuthResult<WalletBalanceData> {
        val addressResult = queryWalletAddress()
        if (addressResult !is DAuthResult.Success){
            return DAuthResult.SdkError(DAuthResult.SDK_ERROR_CANNOT_GET_ADDRESS)
        }
        val address = addressResult.data.address
        val balance = Web3Manager.getBalance(address)
        DAuthLogger.i("queryWalletBalance $balance", TAG)
        return balance
    }

    override suspend fun estimateGas(toUserId: String, amount: BigInteger): DAuthResult<EstimateGasData> {
        val addressResult = queryWalletAddress()
        if (addressResult !is DAuthResult.Success){
            return DAuthResult.SdkError(DAuthResult.SDK_ERROR_CANNOT_GET_ADDRESS)
        }
        val address = addressResult.data.address
        return Web3Manager.estimateGas(address, toUserId, amount).also {
            DAuthLogger.d("estimateGas from=$address to=$toUserId amount=$amount result=$it", TAG)
        }
    }

    override suspend fun sendTransaction(toAddress: String, amount: BigInteger): DAuthResult<SendTransactionData> {
        DAuthLogger.d("sendTransaction $toAddress $amount", TAG)
        return Web3Manager.sendTransaction(toAddress, amount).also {
            DAuthLogger.i("sendTransaction to=$toAddress amount=$amount result=$it", TAG)
        }
    }
}