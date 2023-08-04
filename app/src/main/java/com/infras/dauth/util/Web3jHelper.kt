package com.infras.dauth.util

import com.infras.dauth.manager.AccountManager.sdk
import com.infras.dauthsdk.api.entity.DAuthResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.Response
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigInteger

private const val TAG = "TransferMoneyUtil"

private suspend inline fun <S, T : Response<*>, D> Request<S, T>.await(crossinline block: (T) -> D): DAuthResult<D> {
    return withContext(Dispatchers.IO) {
        try {
            val r = send()
            if (r.hasError()) {
                DAuthResult.ServerError(r.error.code, r.error.message)
            } else {
                val d = block.invoke(r)
                DAuthResult.Success(d)
            }
        } catch (t: Throwable) {
            LogUtil.e(t.stackTraceToString(), TAG)
            DAuthResult.NetworkError(t)
        }
    }
}

private suspend inline fun <S, T : Response<*>, D> Request<S, T>.awaitLite(crossinline block: (T) -> D): D? {
    return (await(block) as? DAuthResult.Success)?.data
}

object Web3jHelper {

    private val web3j get() = sdk().getWeb3j()

    private const val RICH_EOA_ACCOUNT = "0xdD2FD4581271e230360230F9337D5c0430Bf44C0"
    private const val RICH_EOA_PRIVATE_KEY = "0xde9be858da4a475276426320d5e9262ecfc3ba460bfac56360bfa6c4c28b4ee0"

    suspend fun transferMoneyToAa(
        aaAccount: String,
        eoaAccount: String = RICH_EOA_ACCOUNT,
        eoaPrivateKey: String = RICH_EOA_PRIVATE_KEY
    ): String? {
        val nonce =
            web3j.ethGetTransactionCount(eoaAccount, DefaultBlockParameterName.LATEST).awaitLite {
                it.transactionCount
            }
        LogUtil.d(TAG, "nonce=$nonce")

        val value = Convert.toWei("1", Convert.Unit.ETHER).toBigInteger()
        val rawTransaction = RawTransaction.createEtherTransaction(
            nonce,
            BigInteger.valueOf(20000000000L),
            BigInteger.valueOf(21000000L),
            aaAccount,
            value
        )

        val signedMessage =
            TransactionEncoder.signMessage(rawTransaction, Credentials.create(eoaPrivateKey))
        val hexValue = Numeric.toHexString(signedMessage)
        LogUtil.d(TAG, "hexValue=$hexValue")
        val transactionHash = web3j.ethSendRawTransaction(hexValue).awaitLite {
            it.transactionHash
        }
        LogUtil.d(TAG, "transactionHash=$transactionHash")
        return transactionHash
    }

    @Deprecated("使用entryPoint.getNonce")
    suspend fun isCodeExists(aaAddress: String): Boolean? {
        val code = web3j.ethGetCode(aaAddress, DefaultBlockParameterName.LATEST).awaitLite {
            it.code
        } ?: return null
        val result = code != "0x"
        LogUtil.d("getCodeByAddress $aaAddress -> $code result=$result", TAG)
        return result
    }
}