package com.cyberflow.dauthsdk.wallet.impl

import android.os.Build
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

    // 苟建本地节点
    private const val GOU_JIAN_URL = "http://172.16.13.155:8545/"

    // sepolia测试节点，从https://sepolia.dev/#抄的
    // chainId：11155111 (0xaa36a7)
    private const val SEPOLIA_RPC_URL = "https://rpc.sepolia.org/"
    val okHttpClient by lazy { getOkhttpClient() }
    private val web3j by lazy { Web3j.build(HttpService(SEPOLIA_RPC_URL, okHttpClient)) }

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
        return web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).await()?.let {
            kotlin.runCatching { it.balance }.getOrNull()
        }
    }

    suspend fun getGasPrice(): BigInteger? {
        return web3j.ethGasPrice().await()?.gasPrice
    }

    suspend fun estimateGas(from: String, to: String, value: BigInteger): BigInteger? {
        return web3j.ethEstimateGas(
            Transaction.createEtherTransaction(
                from,
                null,
                null,
                null,
                to,
                value
            )
        ).await()?.amountUsed
    }

    private suspend fun getTransactionReceipt(txHash: String): TransactionReceipt? {
        val r = web3j.ethGetTransactionReceipt(txHash).await()?.transactionReceipt ?: return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (r.isPresent) {
                r.get()
            } else {
                null
            }
        } else {
            null
        }
    }

    suspend fun sendTransaction(to: String, amount: BigInteger): String? {
        val credentials = CredentialsUtil.loadCredentials()
        val address = credentials.address
        val transactionCount =
            web3j.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST)
                .await()?.transactionCount
        DAuthLogger.i("transactionCount=$transactionCount")
        if (transactionCount == null) {
            return null
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
        val ethSendTransaction = web3j.ethSendRawTransaction(hexValue).await()
        val hash = ethSendTransaction?.transactionHash
        DAuthLogger.i("hash=$hash")
        if (hash.isNullOrEmpty()) {
            DAuthLogger.i("hash is null")
            return null
        }

        val receipt = getTransactionReceipt(hash)
        DAuthLogger.i("receipt=$receipt")
        if (receipt == null) {
            return null
        }

        return receipt.gasUsedRaw
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