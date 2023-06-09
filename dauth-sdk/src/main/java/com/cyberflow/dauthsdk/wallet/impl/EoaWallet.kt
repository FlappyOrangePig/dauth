package com.cyberflow.dauthsdk.wallet.impl

import android.content.Context
import com.cyberflow.dauthsdk.DAuthSDK
import com.cyberflow.dauthsdk.login.model.BindWalletParam
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.login.utils.LoginPrefs
import com.cyberflow.dauthsdk.wallet.api.IWalletApi
import com.cyberflow.dauthsdk.wallet.util.CredentialsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger

/**
 * 模拟钱包实现类。
 * 在AA钱包开发完成前先创建假数据。
 */
class EoaWallet internal constructor(): IWalletApi {
    private val context get() = (DAuthSDK.instance).context
    override fun initWallet(context: Context) {

    }

    override suspend fun createWallet(passcode: String?): Int? {
        DAuthLogger.d("钱包创建成功")
        var code :Int?

        val accessToken = LoginPrefs(context).getAccessToken()
        val authId = LoginPrefs(context).getAuthId()
        val bindWalletParam = BindWalletParam(
            accessToken, authId, "0x11", 11,
            "0xff",
            "0x"
        )
        withContext(Dispatchers.IO) {
            val response = RequestApi().bindWallet(bindWalletParam)
            code = response?.iRet
            if (code != 0) {
                DAuthLogger.e("绑定钱包失败：${response?.sMsg}")
            }
        }
        return code
    }

    override suspend fun queryWalletAddress(): String {
        return CredentialsUtil.loadCredentials().address.also {
            DAuthLogger.i("queryWalletAddress $it")
        }
    }

    override suspend fun queryWalletBalance(): BigInteger? {
        val address = queryWalletAddress()
        return if (address.isNotEmpty()) {
            val big = Web3Manager.getBalance(address)
            big
        } else {
            null
        }.also { DAuthLogger.i("queryWalletBalance $it") }
    }

    override suspend fun estimateGas(toUserId: String, amount: BigInteger): BigInteger? {
        val address = queryWalletAddress()
        return Web3Manager.estimateGas(address, toUserId, amount).also {
            DAuthLogger.d("estimateGas from=$address to=$toUserId amount=$amount result=$it")
        }
    }

    override suspend fun sendTransaction(toAddress: String, amount: BigInteger):String? {
        DAuthLogger.d("sendTransaction $toAddress $amount")
        return Web3Manager.sendTransaction(toAddress, amount).also {
            DAuthLogger.i("sendTransaction to=$toAddress amount=$amount result=$it")
        }
    }
}