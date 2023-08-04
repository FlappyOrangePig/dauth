package com.infras.dauthsdk.wallet.impl

import com.infras.dauthsdk.api.IEoaWalletApi
import com.infras.dauthsdk.api.entity.DAuthResult
import com.infras.dauthsdk.api.entity.EstimateGasData
import com.infras.dauthsdk.api.entity.SendTransactionData
import com.infras.dauthsdk.api.entity.Transaction1559
import com.infras.dauthsdk.api.entity.transformError
import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.mpc.util.MoshiUtil
import com.infras.dauthsdk.wallet.connect.metamask.MetaMaskActivity
import com.infras.dauthsdk.wallet.connect.metamask.MetaMaskCallback
import com.infras.dauthsdk.wallet.connect.metamask.MetaMaskInput
import com.infras.dauthsdk.wallet.ext.app
import com.infras.dauthsdk.wallet.impl.manager.Managers
import kotlinx.coroutines.suspendCancellableCoroutine
import java.math.BigInteger
import kotlin.coroutines.resume

private const val TAG = "EoaWalletImpl"

internal class EoaWalletImpl internal constructor() : IEoaWalletApi {

    private val web3m get() = Managers.web3m
    private val connectManager get() = Managers.connectManager

    internal var metamaskCallback: MetaMaskCallback? = null

    /**
     * Current eoa address 保存[getEoaWalletAddress]所返回的地址
     */
    @Volatile
    internal var currentEoaAddress: String? = null

    override suspend fun connectWallet(): Boolean {
        return /*runCatchingWithLogSuspend { connectManager.connect() } ?:*/ false
    }

    override suspend fun connectMetaMask(): DAuthResult<String> {
        val r: DAuthResult<String> = suspendCancellableCoroutine { continuation ->
            this.metamaskCallback = {
                continuation.resume(it)
            }
            MetaMaskActivity.launch(app(), MetaMaskInput.Connect)
        }
        return r
    }

    override suspend fun getEoaWalletAddress(): DAuthResult<String> {
        /*val credential = CredentialsUtil.loadCredentials(false)
        return DAuthResult.Success(credential.address)*/
        val address = MetaMaskActivity.getEoaAddress()
        return if (!address.isNullOrEmpty()) {
            DAuthResult.Success(address)
        } else {
            DAuthResult.SdkError(DAuthResult.SDK_ERROR_UNKNOWN)
        }
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

    override suspend fun sendTransaction(transaction1559: Transaction1559): DAuthResult<SendTransactionData> {
        DAuthLogger.d("sendTransaction $transaction1559", TAG)
        val r:DAuthResult<String> = suspendCancellableCoroutine { continuation ->
            this.metamaskCallback = {
                continuation.resume(it)
            }
            val transactionJson = MoshiUtil.toJson(transaction1559)
            MetaMaskActivity.launch(app(), MetaMaskInput.SendTransaction(transactionJson))
        }
        return if (r is DAuthResult.Success){
            DAuthResult.Success(SendTransactionData(r.data))
        }else{
            r.transformError()
        }
    }

    override suspend fun personalSign(message: String): DAuthResult<String> {
        val r: DAuthResult<String> = suspendCancellableCoroutine { continuation ->
            this.metamaskCallback = {
                continuation.resume(it)
            }
            MetaMaskActivity.launch(app(), MetaMaskInput.PersonalSign(message))
        }
        return r
    }
}