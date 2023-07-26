package com.cyberflow.dauthsdk.wallet.impl

import com.cyberflow.dauthsdk.api.IWalletApi
import com.cyberflow.dauthsdk.api.entity.CommitTransactionData
import com.cyberflow.dauthsdk.api.entity.CreateWalletData
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.EstimateGasData
import com.cyberflow.dauthsdk.api.entity.CreateUserOpAndEstimateGasData
import com.cyberflow.dauthsdk.api.entity.DAuthResult.Companion.SDK_ERROR_CANNOT_GET_ADDRESS
import com.cyberflow.dauthsdk.api.entity.SendTransactionData
import com.cyberflow.dauthsdk.api.entity.TokenType
import com.cyberflow.dauthsdk.api.entity.WalletAddressData
import com.cyberflow.dauthsdk.api.entity.WalletBalanceData
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.mpc.websocket.WebsocketManager
import com.cyberflow.dauthsdk.wallet.impl.manager.Managers
import com.cyberflow.dauthsdk.wallet.impl.manager.WalletManager
import com.cyberflow.dauthsdk.wallet.sol.DAuthAccount
import com.cyberflow.dauthsdk.wallet.sol.EntryPoint.UserOperation
import com.cyberflow.dauthsdk.wallet.util.prependHexPrefix
import kotlinx.coroutines.Dispatchers
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

    private val walletPrefsV2 get() = Managers.walletPrefsV2
    private val web3m get() = Managers.web3m

    override suspend fun createWallet(forceCreate: Boolean): DAuthResult<CreateWalletData> {
        return withContext(Dispatchers.Default) {
            Managers.walletManager.initWallet(forceCreate)
        }
    }

    override suspend fun queryWalletAddress(): DAuthResult<WalletAddressData> {
        val aaAddress = walletPrefsV2.getAaAddress()
        val signerAddress = walletPrefsV2.getEoaAddress()
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

    override suspend fun queryWalletBalance(
        walletAddress: String,
        tokenType: TokenType
    ): DAuthResult<WalletBalanceData> {
        val r = when (tokenType) {
            is TokenType.Eth -> {
                web3m.getBalance(walletAddress)
            }

            is TokenType.ERC20 -> {
                web3m.getERC20Balance(
                    contractAddress = tokenType.contractAddress,
                    accountAddress = walletAddress
                )
            }

            is TokenType.ERC721 -> {
                web3m.getERC721NftTokenIds(
                    contractAddress = tokenType.contractAddress,
                    walletAddress = walletAddress
                )
            }
        }
        DAuthLogger.i("queryWalletBalance $r", TAG)
        return r
    }

    override suspend fun estimateGas(
        toUserId: String,
        amount: BigInteger
    ): DAuthResult<EstimateGasData> {
        val addressResult = queryWalletAddress()
        if (addressResult !is DAuthResult.Success) {
            return DAuthResult.SdkError(DAuthResult.SDK_ERROR_CANNOT_GET_ADDRESS)
        }
        val address = addressResult.data.aaAddress
        return web3m.estimateGas(address, toUserId, amount).also {
            DAuthLogger.d("estimateGas from=$address to=$toUserId amount=$amount result=$it", TAG)
        }
    }

    override suspend fun sendTransaction(
        toAddress: String,
        amount: BigInteger
    ): DAuthResult<SendTransactionData> {
        DAuthLogger.d("sendTransaction $toAddress $amount", TAG)
        return web3m.sendTransaction(toAddress, amount).also {
            DAuthLogger.i("sendTransaction to=$toAddress amount=$amount result=$it", TAG)
        }
    }

    private suspend fun <T> dealWithUserOperationCall(
        contractAddress: String,
        balance: BigInteger,
        func: ByteArray,
        block: suspend (String, ByteArray) -> DAuthResult<T>
    ): DAuthResult<T> {
        val addressResult = queryWalletAddress()
        if (addressResult !is DAuthResult.Success) {
            return DAuthResult.SdkError(SDK_ERROR_CANNOT_GET_ADDRESS)
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
        return block.invoke(signerAddress, callData)
    }

    override suspend fun execute(
        userOperation: UserOperation
    ): DAuthResult<CommitTransactionData> {
        val addressResult = queryWalletAddress()
        if (addressResult !is DAuthResult.Success) {
            return DAuthResult.SdkError(SDK_ERROR_CANNOT_GET_ADDRESS)
        }
        val aaAddress = addressResult.data.aaAddress
        return web3m.executeUserOperation(userOperation, aaAddress)
    }

    override suspend fun createUserOpAndEstimateGas(
        contractAddress: String,
        balance: BigInteger,
        func: ByteArray
    ): DAuthResult<CreateUserOpAndEstimateGasData> {
        val addressResult = queryWalletAddress()
        if (addressResult !is DAuthResult.Success) {
            return DAuthResult.SdkError(SDK_ERROR_CANNOT_GET_ADDRESS)
        }
        val signerAddress = addressResult.data.signerAddress
        val aaAddress = addressResult.data.aaAddress
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

        val execResult = web3m.createUserOpAndEstimateGas(signerAddress, aaAddress, callData)
        DAuthLogger.d(
            "createUserOpAndEstimateGas $contractAddress $balance ${func.size} result=$execResult",
            TAG
        )
        return execResult
    }

    override fun getWeb3j(): Web3j {
        return web3m.web3j
    }

    override suspend fun mpcSign(msgHash: String): Sign.SignatureData? {
        return WebsocketManager.instance.mpcSign(msgHash)
    }

    override fun deleteWallet() {
        Managers.walletManager.clearData()
    }

    override suspend fun restoreKeys(keys: List<String>): DAuthResult<CreateWalletData> {
        Managers.mpcKeyStore.setAllKeys(keys = keys.toList())
        Managers.walletPrefsV2.setWalletState(WalletManager.STATE_KEY_GENERATED)
        return createWallet(false)
    }
}