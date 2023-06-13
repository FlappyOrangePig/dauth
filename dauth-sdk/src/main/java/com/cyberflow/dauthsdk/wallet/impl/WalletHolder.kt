package com.cyberflow.dauthsdk.wallet.impl

import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.IWalletApi
import com.cyberflow.dauthsdk.api.entity.CreateWalletData
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.login.model.BindWalletParam
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.login.utils.LoginPrefs
import com.cyberflow.dauthsdk.wallet.util.KeystoreUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "WalletWrapper"

object WalletHolder {
    private const val DEBUG = true
    val walletApi: IWalletApi by lazy { wrap(createWallet()) }

    init {
        KeystoreUtil.setupBouncyCastle()
    }

    private fun wrap(walletApi: IWalletApi): IWalletApi {
        return WalletWrapper(walletApi)
    }

    private fun createWallet(): IWalletApi = if (DEBUG) {
        EoaWallet()
    } else {
        DAuthWallet()
    }
}

private class WalletWrapper(private val walletApi: IWalletApi) : IWalletApi by walletApi {

    private val context get() = DAuthSDK.impl.context

    override suspend fun createWallet(passcode: String?): DAuthResult<CreateWalletData> {
        val createWalletResult = walletApi.createWallet(passcode)
        DAuthLogger.d("createWalletResult=$createWalletResult", TAG)

        val address = when (val addressResult = walletApi.queryWalletAddress()) {
            is DAuthResult.Success -> {
                addressResult.data.address
            }
            else -> {
                return DAuthResult.SdkError(DAuthResult.SDK_ERROR_CANNOT_GET_ADDRESS)
            }
        }
        DAuthLogger.d("address=$address", TAG)

        val accessToken = LoginPrefs(context).getAccessToken()
        val authId = LoginPrefs(context).getAuthId()
        val bindWalletParam = BindWalletParam(
            accessToken, authId, address, 11,
            "0x00",
            "0x00"
        )
        return withContext(Dispatchers.IO) {
            val response = RequestApi().bindWallet(bindWalletParam)
            val code = response?.iRet
            if (code != 0) {
                DAuthLogger.e("绑定钱包失败：${response?.sMsg}", TAG)
                DAuthResult.SdkError(DAuthResult.SDK_ERROR_BIND_WALLET)
            } else {
                DAuthResult.Success(CreateWalletData(address))
            }
        }
    }
}