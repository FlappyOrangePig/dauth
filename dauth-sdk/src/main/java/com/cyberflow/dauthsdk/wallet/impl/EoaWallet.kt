package com.cyberflow.dauthsdk.wallet.impl

import com.cyberflow.dauthsdk.api.IWalletApi
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
import com.cyberflow.dauthsdk.login.model.GetParticipantsParam
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.login.utils.LoginPrefs
import com.cyberflow.dauthsdk.mpc.DAuthJniInvoker
import com.cyberflow.dauthsdk.mpc.MpcKeyIds
import com.cyberflow.dauthsdk.mpc.MpcKeyStore
import com.cyberflow.dauthsdk.mpc.util.MergeResultUtil
import com.cyberflow.dauthsdk.mpc.util.MoshiUtil
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

    private val keystore get() = MpcKeyStore
    private val skipGenerateMergeResult get() = true

    override suspend fun createWallet(passcode: String?): DAuthResult<CreateWalletData> =
        withContext(Dispatchers.Default) {
            createWalletInner(passcode)
        }

    private suspend fun createWalletInner(passcode: String?): DAuthResult<CreateWalletData>{
        DAuthLogger.d("createWallet $passcode", TAG)
        val keys = DAuthJniInvoker.generateSignKeys()

        // 第一片存本地
        DAuthLogger.d("save 1st key", TAG)
        if (ConfigurationManager.saveAllKeys) {
            keystore.setAllKeys(keys.toList())
        } else {
            keystore.setLocalKey(keys.first())
        }

        // 生成EOA账号
        val msg = DAuthJniInvoker.genRandomMsg()
        val eoaAddress = DAuthJniInvoker.generateEoaAddress(msg.toByteArray(), keys)
        if (eoaAddress == null) {
            DAuthLogger.e("eoaAddress error", TAG)
            return DAuthResult.SdkError(SDK_ERROR_CANNOT_GENERATE_EOA_ADDRESS)
        }
        DAuthLogger.d("eoaAddress=$eoaAddress", TAG)

        // 秘钥求和
        val mergeResult = if (skipGenerateMergeResult) {
            "hahahaha"
        } else {
            MergeResultUtil.encode(keys)
        }
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

        // 拉取MPC服务器信息
        val prefs = LoginPrefs()
        val accessToken = prefs.getAccessToken()
        val authId = LoginPrefs().getAuthId()
        DAuthLogger.d("auth id=$authId")
        val participants = RequestApi().getParticipants(GetParticipantsParam())
        DAuthLogger.d("participants=${MoshiUtil.toJson(participants)}")

        // 第二片给DAuthServer
        val dauthKey = keys[MpcKeyIds.KEY_INDEX_DAUTH_SERVER]
        val bindWalletParam = BindWalletParam(
            accessToken, authId, aaAddress, 11,
            dauthKey,
            mergeResult
        )
        val response = RequestApi().bindWallet(bindWalletParam)
        val code = response?.iRet
        DAuthLogger.d("bind wallet:$code", TAG)
        return if (response != null && response.isSuccess()) {
            WalletPrefsV2.setAddresses(eoaAddress, aaAddress)
            DAuthResult.Success(CreateWalletData(aaAddress))
        } else {
            DAuthLogger.e("绑定钱包失败：${response?.sMsg}", TAG)
            DAuthResult.SdkError(DAuthResult.SDK_ERROR_BIND_WALLET)
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
        callData: ByteArray
    ): DAuthResult<String> {
        val addressResult = queryWalletAddress()
        if (addressResult !is DAuthResult.Success) {
            return DAuthResult.SdkError()
        }
        val address = addressResult.data.signerAddress
        val execResult = Web3Manager.executeUserOperation(address, callData)
        DAuthLogger.d("execute $execResult", TAG)
        return execResult
    }
}