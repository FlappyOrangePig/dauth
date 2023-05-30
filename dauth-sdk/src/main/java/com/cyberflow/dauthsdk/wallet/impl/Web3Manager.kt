package com.cyberflow.dauthsdk.wallet.impl

import com.cyberflow.dauthsdk.wallet.sol.TestTemp
import com.cyberflow.dauthsdk.wallet.util.ConvertUtil
import com.cyberflow.dauthsdk.wallet.const.WalletConst
import com.cyberflow.dauthsdk.wallet.const.WalletConst.LOG_TAG
import com.cyberflow.dauthsdk.wallet.util.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.*
import org.web3j.protocol.core.methods.response.EthGasPrice
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import org.web3j.tx.ClientTransactionManager
import org.web3j.tx.gas.DefaultGasProvider
import java.math.BigInteger

private const val TAG = LOG_TAG

object Web3Manager {

    // 苟建本地节点
    private const val GOU_JIAN_URL = "http://172.16.13.155:8545/"

    // sepolia测试节点，从https://sepolia.dev/#抄的
    private const val SEPOLIA_RPC_URL = "https://rpc.sepolia.org/"
    private val web3j by lazy { Web3j.build(HttpService(GOU_JIAN_URL)) }

    private suspend fun <S, T : Response<*>> Request<S, T>.await(): T? {
        val result = withContext(Dispatchers.IO) {
            try {
                send()
            } catch (e: Exception) {
                LogUtil.e(TAG, android.util.Log.getStackTraceString(e))
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
                LogUtil.e(TAG, android.util.Log.getStackTraceString(e))
                null
            }
        }
        return result
    }

    suspend fun getBalance(address: String): BigInteger? {
        return web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).await()?.let {
            kotlin.runCatching { it.balance }.getOrNull()
        }
    }

    suspend fun getGasPrice(): EthGasPrice? {
        return web3j.ethGasPrice().await()
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