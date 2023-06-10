package com.cyberflow.dauthsdk.wallet.impl

import android.content.Context
import com.cyberflow.dauthsdk.api.entity.CreateWalletResult
import com.cyberflow.dauthsdk.api.entity.EstimateGasResult
import com.cyberflow.dauthsdk.api.entity.GetAddressResult
import com.cyberflow.dauthsdk.api.entity.GetBalanceResult
import com.cyberflow.dauthsdk.api.entity.SendTransactionResult
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.wallet.api.IWalletApi
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

    override suspend fun createWallet(passcode: String?): CreateWalletResult {
        val credential = CredentialsUtil.loadCredentials(true)
        DAuthLogger.d("createWallet ${credential.address}", TAG)
        return CreateWalletResult.Success(credential.address)
    }

    override suspend fun queryWalletAddress(): GetAddressResult {
        val address = CredentialsUtil.loadCredentials(false).address
        DAuthLogger.i("queryWalletAddress $address", TAG)
        return address?.let { GetAddressResult.Success(address) } ?: GetAddressResult.Failure
    }

    override suspend fun queryWalletBalance(): GetBalanceResult {
        val addressResult = queryWalletAddress()
        if (addressResult !is GetAddressResult.Success){
            return GetBalanceResult.CannotGetAddress
        }
        val address = addressResult.address
        val balance = Web3Manager.getBalance(address).takeIf { address.isNotEmpty() }
        DAuthLogger.i("queryWalletBalance $balance", TAG)
        return balance?.let { GetBalanceResult.Success(it) } ?: GetBalanceResult.Failure
    }

    override suspend fun estimateGas(toUserId: String, amount: BigInteger): EstimateGasResult {
        val addressResult = queryWalletAddress()
        if (addressResult !is GetAddressResult.Success){
            return EstimateGasResult.CannotGetAddress
        }
        val address = addressResult.address
        return Web3Manager.estimateGas(address, toUserId, amount).also {
            DAuthLogger.d("estimateGas from=$address to=$toUserId amount=$amount result=$it", TAG)
        }
    }

    override suspend fun sendTransaction(toAddress: String, amount: BigInteger):SendTransactionResult {
        DAuthLogger.d("sendTransaction $toAddress $amount", TAG)
        return Web3Manager.sendTransaction(toAddress, amount).also {
            DAuthLogger.i("sendTransaction to=$toAddress amount=$amount result=$it", TAG)
        }
    }
}