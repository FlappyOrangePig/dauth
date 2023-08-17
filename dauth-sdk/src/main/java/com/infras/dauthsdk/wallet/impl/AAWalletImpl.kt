package com.infras.dauthsdk.wallet.impl

import com.infras.dauthsdk.api.IAAWalletApi
import com.infras.dauthsdk.api.entity.CommitTransactionData
import com.infras.dauthsdk.api.entity.CreateWalletData
import com.infras.dauthsdk.api.entity.DAuthResult
import com.infras.dauthsdk.api.entity.CreateUserOpAndEstimateGasData
import com.infras.dauthsdk.api.entity.DAuthResult.Companion.SDK_ERROR_CANNOT_GET_ADDRESS
import com.infras.dauthsdk.api.entity.TokenType
import com.infras.dauthsdk.api.entity.WalletAddressData
import com.infras.dauthsdk.api.entity.WalletBalanceData
import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.mpc.ext.ElapsedContext
import com.infras.dauthsdk.mpc.websocket.WebsocketManager
import com.infras.dauthsdk.wallet.impl.manager.Managers
import com.infras.dauthsdk.wallet.impl.manager.WalletManager
import com.infras.dauthsdk.wallet.sol.EntryPoint.UserOperation
import com.infras.dauthsdk.wallet.util.prependHexPrefix
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

private const val TAG = "AAWalletImpl"

/**
 * 模拟钱包实现类。
 * 在AA钱包开发完成前先创建假数据。
 */
class AAWalletImpl internal constructor(): IAAWalletApi {

    private val walletPrefsV2 get() = Managers.walletPrefsV2
    private val web3m get() = Managers.web3m

    override suspend fun createWallet(forceCreate: Boolean): DAuthResult<CreateWalletData> {
        return withContext(Dispatchers.Default) {
            val context = ElapsedContext(TAG, "createWallet")
            Managers.walletManager.initWallet(context, forceCreate).also {
                context.finish()
            }
        }
    }

    override suspend fun queryWalletAddress(): DAuthResult<WalletAddressData> {
        val aaAddress = walletPrefsV2.getAaAddress()
        val signerAddress = walletPrefsV2.getEoaAddress()
        DAuthLogger.d("queryWalletAddress $aaAddress/$signerAddress", TAG)
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

    override suspend fun execute(
        userOperation: UserOperation
    ): DAuthResult<CommitTransactionData> {
        val addressResult = queryWalletAddress()
        if (addressResult !is DAuthResult.Success) {
            return DAuthResult.SdkError(SDK_ERROR_CANNOT_GET_ADDRESS)
        }
        val aaAddress = addressResult.data.aaAddress
        val context = ElapsedContext(TAG, "executeUserOperation")
        val result = web3m.executeUserOperation(context, userOperation, aaAddress)
        context.finish()
        return result
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
            com.infras.dauthsdk.wallet.sol.DAuthAccount.FUNC_EXECUTE,
            listOf<Type<*>>(
                Address(contractAddress.prependHexPrefix()),
                Uint256(balance),
                DynamicBytes(func)
            ), emptyList<TypeReference<*>>()
        )
        val encodedFunction = FunctionEncoder.encode(function)
        val callData = Numeric.hexStringToByteArray(encodedFunction)

        val context = ElapsedContext(TAG, "createUserOpAndEstimateGas")
        val execResult = web3m.createUserOpAndEstimateGas(context, signerAddress, aaAddress, callData)
        context.finish()
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