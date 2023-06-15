package com.cyberflow.dauthsdk.wallet.impl

import com.cyberflow.dauthsdk.api.IWalletApi
import com.cyberflow.dauthsdk.api.SdkConfig
import com.cyberflow.dauthsdk.api.entity.CreateWalletData
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.EstimateGasData
import com.cyberflow.dauthsdk.api.entity.SendTransactionData
import com.cyberflow.dauthsdk.api.entity.TokenType
import com.cyberflow.dauthsdk.api.entity.WalletAddressData
import com.cyberflow.dauthsdk.api.entity.WalletBalanceData
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.mpc.DAuthJniInvoker
import com.cyberflow.dauthsdk.wallet.sol.DAuthAccount
import com.cyberflow.dauthsdk.wallet.sol.TestDAuthAccount
import com.cyberflow.dauthsdk.wallet.util.CredentialsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Function
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.ClientTransactionManager
import org.web3j.tx.gas.DefaultGasProvider
import java.math.BigInteger

private const val TAG = "EoaWallet"

/**
 * 模拟钱包实现类。
 * 在AA钱包开发完成前先创建假数据。
 */
class EoaWallet internal constructor(): IWalletApi {

    override fun initWallet(chain: SdkConfig.ChainInfo) {

    }

    override suspend fun createWallet(passcode: String?): DAuthResult<CreateWalletData> {
        val credential = CredentialsUtil.loadCredentials(true)
        val address = credential.address
        DAuthLogger.d("createWallet $address", TAG)
        return DAuthResult.Success(CreateWalletData(address))
    }

    override suspend fun queryWalletAddress(): DAuthResult<WalletAddressData> {
        val address = CredentialsUtil.loadCredentials(false).address
        DAuthLogger.i("queryWalletAddress $address", TAG)
        return address?.let { DAuthResult.Success(WalletAddressData(address)) }
            ?: DAuthResult.SdkError(DAuthResult.SDK_ERROR_CANNOT_GET_ADDRESS)
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
        if (addressResult !is DAuthResult.Success){
            return DAuthResult.SdkError(DAuthResult.SDK_ERROR_CANNOT_GET_ADDRESS)
        }
        val address = addressResult.data.address
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
        dest: String,
        value: BigInteger,
        func: Function
    ): DAuthResult<TransactionReceipt> {
        return Web3Manager.execute(dest, value, func)
    }
}