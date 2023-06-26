package com.cyberflow.dauthsdk.wallet.impl

import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.IWalletApi
import com.cyberflow.dauthsdk.api.SdkConfig
import com.cyberflow.dauthsdk.api.entity.CreateWalletData
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.DAuthResult.Companion.SDK_ERROR_CANNOT_GENERATE_EOA_ADDRESS
import com.cyberflow.dauthsdk.api.entity.DAuthResult.Companion.SDK_ERROR_GET_AA_ADDRESS_ERROR
import com.cyberflow.dauthsdk.api.entity.DAuthResult.Companion.SDK_ERROR_MERGE_RESULT
import com.cyberflow.dauthsdk.api.entity.EstimateGasData
import com.cyberflow.dauthsdk.api.entity.SendTransactionData
import com.cyberflow.dauthsdk.api.entity.TokenType
import com.cyberflow.dauthsdk.api.entity.WalletAddressData
import com.cyberflow.dauthsdk.api.entity.WalletBalanceData
import com.cyberflow.dauthsdk.login.model.BindWalletParam
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.login.utils.LoginPrefs
import com.cyberflow.dauthsdk.mpc.DAuthJniInvoker
import com.cyberflow.dauthsdk.mpc.MpcKeyIds
import com.cyberflow.dauthsdk.mpc.MpcKeyStore
import com.cyberflow.dauthsdk.mpc.util.MergeResultUtil
import com.cyberflow.dauthsdk.wallet.util.CredentialsUtil
import com.cyberflow.dauthsdk.wallet.util.WalletPrefsV2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.abi.datatypes.Function
import org.web3j.protocol.core.methods.response.TransactionReceipt
import java.math.BigInteger

private const val TAG = "EoaWallet"

/**
 * 模拟钱包实现类。
 * 在AA钱包开发完成前先创建假数据。
 */
class EoaWallet internal constructor(): IWalletApi {

    private val context get() = DAuthSDK.impl.context
    private val keystore get() = MpcKeyStore

    override fun initWallet(chain: SdkConfig.ChainInfo) {

    }

    override suspend fun createWallet(passcode: String?): DAuthResult<CreateWalletData> =
        withContext(Dispatchers.Default) {
            createWalletInner(passcode)
        }

    private suspend fun createWalletInner(passcode: String?): DAuthResult<CreateWalletData>{
        DAuthLogger.d("createWallet $passcode", TAG)
        val keys = DAuthJniInvoker.generateSignKeys()

        // 第一片存本地
        DAuthLogger.d("save 1st key", TAG)
        //keystore.setLocalKey(keys.first())
        keystore.setAllKeys(keys.toList())

        // 生成EOA账号
        val msg = DAuthJniInvoker.genRandomMsg()
        val eoaAddress = DAuthJniInvoker.generateEoaAddress(msg.toByteArray(), keys)
        if (eoaAddress == null) {
            DAuthLogger.e("eoaAddress error", TAG)
            return DAuthResult.SdkError(SDK_ERROR_CANNOT_GENERATE_EOA_ADDRESS)
        }
        DAuthLogger.d("eoaAddress=$eoaAddress", TAG)

        // 秘钥求和
        val mergeResult = MergeResultUtil.encode(keys)
        val mergeResultLen = mergeResult.length
        DAuthLogger.d("key merge len=$mergeResultLen", TAG)
        if (mergeResultLen == 0) {
            return DAuthResult.SdkError(SDK_ERROR_MERGE_RESULT)
        }

        // 根据EOA地址获取AA地址
        val aaAddress = Web3Manager.getAaAddressByEoaAddress(eoaAddress)
        if (aaAddress == null) {
            DAuthLogger.d("aa address failed")
            return DAuthResult.SdkError(SDK_ERROR_GET_AA_ADDRESS_ERROR)
        }
        if (aaAddress.length <= 2) {
            DAuthLogger.d("aa address too short")
            return DAuthResult.SdkError(SDK_ERROR_GET_AA_ADDRESS_ERROR)
        }
        DAuthLogger.d("aa address=$aaAddress")

        // 第二片给DAuthServer
        val accessToken = LoginPrefs(context).getAccessToken()
        val authId = LoginPrefs(context).getAuthId()
        val dauthKey = keys[MpcKeyIds.KEY_INDEX_DAUTH_SERVER]
        val bindWalletParam = BindWalletParam(
            accessToken, authId, aaAddress, 11,
            dauthKey,
            mergeResult
        )
        val response = RequestApi().bindWallet(bindWalletParam)
        val code = response?.iRet
        DAuthLogger.d("bind wallet:$code", TAG)
        return if (code != 0) {
            DAuthLogger.e("绑定钱包失败：${response?.sMsg}", TAG)
            DAuthResult.SdkError(DAuthResult.SDK_ERROR_BIND_WALLET)
        } else {
            WalletPrefsV2().setAddresses(eoaAddress, aaAddress)
            DAuthResult.Success(CreateWalletData(aaAddress))
        }
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