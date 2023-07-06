package com.cyberflow.dauthsdk.wallet.impl

import com.cyberflow.dauthsdk.api.IWalletApi
import com.cyberflow.dauthsdk.api.entity.CreateWalletData
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.EstimateGasData
import com.cyberflow.dauthsdk.api.entity.SendTransactionData
import com.cyberflow.dauthsdk.api.entity.TokenType
import com.cyberflow.dauthsdk.api.entity.WalletAddressData
import com.cyberflow.dauthsdk.api.entity.WalletBalanceData
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.login.utils.LoginPrefs
import com.cyberflow.dauthsdk.mpc.MpcKeyStore
import com.cyberflow.dauthsdk.mpc.websocket.WebsocketManager
import com.cyberflow.dauthsdk.wallet.impl.manager.WalletManager
import com.cyberflow.dauthsdk.wallet.sol.DAuthAccount
import com.cyberflow.dauthsdk.wallet.util.WalletPrefsV2
import com.cyberflow.dauthsdk.wallet.util.prependHexPrefix
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Sign
import org.web3j.protocol.Web3j
import org.web3j.utils.Numeric
import java.math.BigInteger

private const val TAG = "EoaWallet"

/**
 * 模拟钱包实现类。
 * 在AA钱包开发完成前先创建假数据。
 */
class EoaWallet internal constructor(): IWalletApi {

    private val loginPrefs by lazy { LoginPrefs() }
    private val walletManager by lazy { WalletManager.instance }
    @Volatile
    private var lastDeferred : Deferred<*>? = null

    override suspend fun createWallet(passcode: String?): DAuthResult<CreateWalletData> {
        return coroutineScope {
            lastDeferred?.cancel()
            async {
                walletManager.initWallet(loginPrefs)
            }.let {
                lastDeferred = it
                it.await()
            }
        }
    }

    override suspend fun queryWalletAddress(): DAuthResult<WalletAddressData> {
        val aaAddress = WalletPrefsV2.getAaAddress()
        val signerAddress = WalletPrefsV2.getEoaAddress()
        DAuthLogger.i("queryWalletAddress $aaAddress/$signerAddress", TAG)
        return if (aaAddress.isNotEmpty() && signerAddress.isNotEmpty()) {
            DAuthResult.Success(
                WalletAddressData(
                    aaAddress = aaAddress,
                    signerAddress = signerAddress
                )
            )
        } else {
            DAuthResult.SdkError(DAuthResult.SDK_ERROR_CANNOT_GET_ADDRESS)
        }
    }

    override suspend fun queryWalletBalance(walletAddress: String, tokenType: TokenType): DAuthResult<WalletBalanceData> {
        val r = when (tokenType) {
            is TokenType.Eth -> {
                Web3Manager.getBalance(walletAddress)
            }
            is TokenType.ERC20 -> {
                Web3Manager.getERC20Balance(
                    contractAddress = tokenType.contractAddress,
                    accountAddress = walletAddress
                )
            }
            is TokenType.ERC721 -> {
                Web3Manager.getERC721NftTokenIds(
                    contractAddress = tokenType.contractAddress,
                    walletAddress = walletAddress
                )
            }
        }
        DAuthLogger.i("queryWalletBalance $r", TAG)
        return r
    }

    override suspend fun estimateGas(toUserId: String, amount: BigInteger): DAuthResult<EstimateGasData> {
        val addressResult = queryWalletAddress()
        if (addressResult !is DAuthResult.Success) {
            return DAuthResult.SdkError(DAuthResult.SDK_ERROR_CANNOT_GET_ADDRESS)
        }
        val address = addressResult.data.aaAddress
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

    override suspend fun execute(
        contractAddress: String,
        balance: BigInteger,
        func: ByteArray
    ): DAuthResult<String> {
        val addressResult = queryWalletAddress()
        if (addressResult !is DAuthResult.Success) {
            return DAuthResult.SdkError()
        }
        val signerAddress = addressResult.data.signerAddress
        val function = Function(
            DAuthAccount.FUNC_EXECUTE,
            listOf<Type<*>>(
                Address(contractAddress.prependHexPrefix()),
                Uint256(balance),
                DynamicBytes(func)
            ), emptyList<TypeReference<*>>()
        )
        val encodedFunction = FunctionEncoder.encode(function)
        val callData = Numeric.hexStringToByteArray(encodedFunction)
        val execResult = Web3Manager.executeUserOperation(signerAddress, callData)
        DAuthLogger.d("execute $contractAddress $balance ${func.size} result=$execResult", TAG)
        return execResult
    }

    override fun getWeb3j(): Web3j {
        return Web3Manager.web3j
    }

    override suspend fun mpcSign(msgHash: String): Sign.SignatureData? {
        return WebsocketManager.instance.mpcSign(msgHash)
    }
}