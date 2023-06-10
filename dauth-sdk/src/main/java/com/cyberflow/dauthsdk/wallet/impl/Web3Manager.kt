package com.cyberflow.dauthsdk.wallet.impl

import android.os.Build
import com.cyberflow.dauthsdk.api.entity.EstimateGasResult
import com.cyberflow.dauthsdk.api.entity.SendTransactionResult
import com.cyberflow.dauthsdk.login.api.DAuthSDK
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.wallet.const.WalletConst
import com.cyberflow.dauthsdk.wallet.sol.TestTemp
import com.cyberflow.dauthsdk.wallet.util.CredentialsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.RemoteFunctionCall
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.Response
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import org.web3j.tx.ClientTransactionManager
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.utils.Numeric
import java.math.BigInteger
import java.util.concurrent.TimeUnit

/**
 * 获取应答中的字段，拋异常时返回空
 */
private inline fun <T, R> T?.field(crossinline block: (T) -> R): R? = this?.let {
    kotlin.runCatching { block.invoke(it) }.getOrNull()
}

object Web3Manager {

    private val web3j by lazy {
        Web3j.build(
            HttpService(
                DAuthSDK.instance.config.web3RpcUrl,
                HttpClient.client
            )
        )
    }

    private suspend fun <S, T : Response<*>> Request<S, T>.await(): T? {
        val result = withContext(Dispatchers.IO) {
            try {
                send()
            } catch (e: Exception) {
                DAuthLogger.e(android.util.Log.getStackTraceString(e))
                null
            }
        }
        return result
    }

    private suspend fun <T> RemoteFunctionCall<T>.await(): T? {
        val result = withContext(Dispatchers.IO) {
            try {
                send()
            } catch (e: Exception) {
                DAuthLogger.e(android.util.Log.getStackTraceString(e))
                null
            }
        }
        return result
    }

    private fun getOkhttpClient() = OkHttpClient().newBuilder().apply {
        connectTimeout(10, TimeUnit.SECONDS)
        readTimeout(10, TimeUnit.SECONDS)
        addInterceptor(HttpLoggingInterceptor {
            DAuthLogger.i(it)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
    }.build()

    suspend fun getBalance(address: String): BigInteger? {
        return web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).await().field { it.balance }
    }

    suspend fun getGasPrice(): BigInteger? {
        return web3j.ethGasPrice().await().field { it.gasPrice }
    }

    suspend fun estimateGas(from: String, to: String, value: BigInteger): EstimateGasResult {
        return web3j.ethEstimateGas(
            Transaction.createEtherTransaction(
                from,
                null,
                null,
                null,
                to,
                value
            )
        ).await().field { it.amountUsed }?.let { EstimateGasResult.Success(it) }
            ?: EstimateGasResult.Failure
    }

    private suspend fun getTransactionReceipt(txHash: String): TransactionReceipt? {
        val transactionReceipt = web3j.ethGetTransactionReceipt(txHash).await().field { it.transactionReceipt } ?: return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (transactionReceipt.isPresent) {
                transactionReceipt.get()
            } else {
                null
            }
        } else {
            null
        }
    }

    suspend fun sendTransaction(to: String, amount: BigInteger): SendTransactionResult {
        val credentials = CredentialsUtil.loadCredentials(false)
        val address = credentials.address
        val transactionCount =
            web3j.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST)
                .await()?.let {
                    kotlin.runCatching { it.transactionCount }.getOrNull()
                }
        DAuthLogger.i("transactionCount=$transactionCount")
        if (transactionCount == null) {
            return SendTransactionResult.CannotFetchTransactionCount
        }

        val gasProvider = DefaultGasProvider()
        val gasPrice = gasProvider.gasPrice
        val gasLimit = gasProvider.gasLimit
        val nonce = transactionCount
        val chainId = 11155111L
        val transaction =
            RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, to, amount)
        val signedTransaction = TransactionEncoder.signMessage(transaction, chainId, credentials)
        val hexValue = Numeric.toHexString(signedTransaction)
        val hash = web3j.ethSendRawTransaction(hexValue).await()?.field { it.transactionHash }
        DAuthLogger.i("hash=$hash")
        if (hash.isNullOrEmpty()) {
            DAuthLogger.i("hash is null")
            return SendTransactionResult.CannotFetchHash
        }

        val receipt = getTransactionReceipt(hash)
        DAuthLogger.i("receipt=$receipt")
        if (receipt == null) {
            return SendTransactionResult.CannotFetchReceipt(hash)
        }

        return SendTransactionResult.Success(
            receipt.transactionHash.orEmpty(),
            receipt.gasUsedRaw.orEmpty()
        )
    }

    private fun getTestTempContract(): TestTemp {
        val contractAddress = WalletConst.TEST_TEMP_CONTRACT_ADDRESS
        //val credentials = CredentialsUtil.loadCredentials()
        val transactionManager = ClientTransactionManager(web3j, WalletConst.ACCOUNT_ADDRESS_GOUJIAN2)
        return TestTemp.load(contractAddress, web3j, transactionManager, DefaultGasProvider())
        //return TestTemp.load(contractAddress, web3j, credentials, DefaultGasProvider())
    }

    suspend fun invokeTestTemp(
        toAddress: String,
        amount: BigInteger
    ): TransactionReceipt? {
        return getTestTempContract()
            .TestCall(toAddress, amount).await()
    }

    suspend fun invokeGetAcc(): String? {
        return getTestTempContract().acc.await()
    }

    suspend fun invokeGetOwner(): String? {
        return getTestTempContract()
            .owner()
            .await()
    }
}