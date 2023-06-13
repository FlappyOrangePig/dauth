package com.cyberflow.dauthsdk.wallet.impl

import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.DAuthResult.Companion.SDK_ERROR_CANNOT_GET_NONCE
import com.cyberflow.dauthsdk.api.entity.EstimateGasData
import com.cyberflow.dauthsdk.api.entity.SendTransactionData
import com.cyberflow.dauthsdk.api.entity.WalletBalanceData
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

object Web3Manager {

    private val web3j by lazy {
        Web3j.build(
            HttpService(
                DAuthSDK.impl.config.chains.first().rpcUrl,
                HttpClient.client
            )
        )
    }

    /**
     * S=请求 T=应答 D=data
     */
    private suspend inline fun <S, T : Response<*>, D> Request<S, T>.await(crossinline block: (T) -> D): DAuthResult<D> {
        return withContext(Dispatchers.IO) {
            try {
                val r = send()
                if (r.hasError()) {
                    DAuthResult.Web3Error(r.error.code, r.error.message)
                } else {
                    val d = block.invoke(r)
                    DAuthResult.Success(d)
                }
            } catch (t: Throwable) {
                DAuthLogger.e(android.util.Log.getStackTraceString(t))
                DAuthResult.NetworkError(t)
            }
        }
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

    suspend fun getBalance(address: String): DAuthResult<WalletBalanceData> {
        return web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).await {
            WalletBalanceData(it.balance)
        }
    }

    /*suspend fun getGasPrice(): BigInteger? {
        return web3j.ethGasPrice().await() { it.gasPrice }
    }*/

    suspend fun estimateGas(from: String, to: String, value: BigInteger): DAuthResult<EstimateGasData> {
        return web3j.ethEstimateGas(
            Transaction.createEtherTransaction(
                from,
                null,
                null,
                null,
                to,
                value
            )
        ).await {
            EstimateGasData(it.amountUsed)
        }
    }

    /*private suspend fun getTransactionReceipt(txHash: String): TransactionReceipt? {
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
    }*/

    suspend fun sendTransaction(to: String, amount: BigInteger): DAuthResult<SendTransactionData> {
        val credentials = CredentialsUtil.loadCredentials(false)
        val address = credentials.address
        val transactionCountResult =
            web3j.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST)
                .await {
                    it.transactionCount
                }
        if (transactionCountResult !is DAuthResult.Success) {
            return DAuthResult.SdkError(SDK_ERROR_CANNOT_GET_NONCE)
        }
        DAuthLogger.i("nonce=${transactionCountResult.data}")

        val gasProvider = DefaultGasProvider()
        val gasPrice = gasProvider.gasPrice
        val gasLimit = gasProvider.gasLimit
        val nonce = transactionCountResult.data
        val chainId = 11155111L
        val transaction =
            RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, to, amount)
        val signedTransaction = TransactionEncoder.signMessage(transaction, chainId, credentials)
        val hexValue = Numeric.toHexString(signedTransaction)

        return web3j.ethSendRawTransaction(hexValue).await {
            SendTransactionData(it.transactionHash)
        }
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