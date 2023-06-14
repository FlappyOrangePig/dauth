package com.cyberflow.dauthsdk.wallet.impl

import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.DAuthResult.Companion.SDK_ERROR_CANNOT_GET_NONCE
import com.cyberflow.dauthsdk.api.entity.EstimateGasData
import com.cyberflow.dauthsdk.api.entity.SendTransactionData
import com.cyberflow.dauthsdk.api.entity.WalletBalanceData
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.wallet.sol.DAuthAccount
import com.cyberflow.dauthsdk.wallet.util.CredentialsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Function
import org.web3j.contracts.eip20.generated.ERC20
import org.web3j.contracts.eip721.generated.ERC721
import org.web3j.contracts.eip721.generated.ERC721Enumerable
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.RemoteCall
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.Response
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import org.web3j.tx.ClientTransactionManager
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.utils.Numeric
import java.math.BigInteger

private const val TAG = "Web3Manager"

/**
 * 不是DAuthAccount的合约地址，是它的中转合约的地址，据说为了打包gas费
 * 转发者和DAuthAccount接口相同
 */
private const val DAUTH_FORWARDER_ADDRESS = ""

object Web3Manager {

    private val web3j: Web3j
        get() {
            return Web3j.build(
                HttpService(
                    DAuthSDK.impl.config.chain?.rpcUrl.orEmpty(),
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
                DAuthLogger.e(android.util.Log.getStackTraceString(t), TAG)
                DAuthResult.NetworkError(t)
            }
        }
    }

    private suspend fun <T> RemoteCall<T>.await(): DAuthResult<T> = withContext(Dispatchers.IO) {
        try {
            val r = send()
            DAuthResult.Success(r)
        } catch (t: Throwable) {
            DAuthLogger.e(android.util.Log.getStackTraceString(t))
            DAuthResult.NetworkError(t)
        }
    }


    suspend fun getBalance(address: String): DAuthResult<WalletBalanceData> {
        return web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).await {
            WalletBalanceData.Eth(it.balance)
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
        DAuthLogger.i("nonce=${transactionCountResult.data}", TAG)

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

    suspend fun getERC20Balance(
        contractAddress: String,
        accountAddress: String
    ): DAuthResult<WalletBalanceData> {
        val contract = withContext(Dispatchers.IO) {
            val transactionManager = ClientTransactionManager(web3j, accountAddress)
            ERC20.load(contractAddress, web3j, transactionManager, DefaultGasProvider())
        }
        val callResult = contract.balanceOf(accountAddress).await()
        DAuthLogger.d("getERC20Balance $callResult", TAG)
        if (callResult is DAuthResult.Success) {
            return DAuthResult.Success(WalletBalanceData.ERC20(callResult.data))
        }
        return DAuthResult.NetworkError()
    }

    suspend fun getERC721NftTokenIds(
        contractAddress: String,
        walletAddress: String
    ): DAuthResult<WalletBalanceData> {
        val (erC721, erC721Enumerable) = withContext(Dispatchers.IO) {
            val transactionManager = ClientTransactionManager(web3j, walletAddress)
            val erC721 =
                ERC721.load(contractAddress, web3j, transactionManager, DefaultGasProvider())
            val erC721Enumerable =
                ERC721Enumerable.load(
                    contractAddress,
                    web3j,
                    transactionManager,
                    DefaultGasProvider()
                )
            erC721 to erC721Enumerable
        }
        val callResult = erC721.balanceOf(walletAddress).await()
        DAuthLogger.d("getERC721Balance $callResult", TAG)
        val arrayList = arrayListOf<BigInteger>()
        if (callResult !is DAuthResult.Success) {
            DAuthLogger.d("cannot get balance", TAG)
            return DAuthResult.NetworkError()
        }
        for (i in 0 until callResult.data.toLong()) {
            // get the token ID at a specific index owned by a specific address
            val tokenIdResult =
                erC721Enumerable.tokenOfOwnerByIndex(walletAddress, BigInteger(i.toString()))
            val tokenId = tokenIdResult.await()
            DAuthLogger.d("$i)tokenId=$tokenId", TAG)
            if (tokenId !is DAuthResult.Success) {
                DAuthLogger.d("cannot get tokenId $i", TAG)
                return DAuthResult.NetworkError()
            }
            arrayList.add(tokenId.data)
        }
        DAuthLogger.d("success with $arrayList", TAG)
        return DAuthResult.Success(WalletBalanceData.ERC721(arrayList))
    }

    suspend fun execute(
        dest: String,
        value: BigInteger,
        func: Function
    ): DAuthResult<TransactionReceipt> {
        val encoded = FunctionEncoder.encode(func)
        val dAuthAccount = withContext(Dispatchers.IO) {
            val dauthAccountAddress = DAUTH_FORWARDER_ADDRESS
            val transactionManager = ClientTransactionManager(web3j, dauthAccountAddress)
            DAuthAccount.load(
                dest,
                web3j, transactionManager, DefaultGasProvider()
            )
        }
        val r = dAuthAccount.execute(dest, value, encoded.toByteArray()).await()
        DAuthLogger.d("execute dest=$dest value=$value func=${FunctionWrapper(func)} result=$r", TAG)
        return r
    }
}