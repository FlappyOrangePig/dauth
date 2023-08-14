package com.infras.dauthsdk.login.impl

import com.infras.dauthsdk.api.entity.LoginResultData
import com.infras.dauthsdk.api.entity.ResponseCode
import com.infras.dauthsdk.login.model.AuthorizeToken2Param
import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.login.utils.JwtDecoder
import com.infras.dauthsdk.login.utils.maskSensitiveData
import com.infras.dauthsdk.wallet.impl.manager.Managers
import com.infras.dauthsdk.wallet.impl.manager.WalletManager


class ThirdPlatformLogin private constructor() {

    private val requestApi get() = Managers.requestApi

    companion object {
        val instance by lazy {
            ThirdPlatformLogin()
        }
    }

    suspend fun thirdPlatFormLogin(body: AuthorizeToken2Param) : LoginResultData {
        var loginResultData: LoginResultData? = null
        val authorizeToken2Res = requestApi.authorizeExchangedToken(body)
        if (authorizeToken2Res == null){
            DAuthLogger.e("exchange token network error")
            return LoginResultData.Failure(null)
        }

        if (authorizeToken2Res.isSuccess()) {
            val didToken = authorizeToken2Res.data?.didToken.orEmpty()
            DAuthLogger.d("didToken:$didToken")
            val googleUserInfo = JwtDecoder().decoded(didToken)
            val accessToken = authorizeToken2Res.data?.accessToken.orEmpty()
            DAuthLogger.d("accessToken:${accessToken.maskSensitiveData()}")
            val refreshToken = authorizeToken2Res.data?.refreshToken.orEmpty()
            val authId = googleUserInfo.sub
            val expireTime = authorizeToken2Res.data?.expireIn
            val userType = body.user_type
            Managers.loginPrefs.putLoginInfo(
                accessToken,
                authId,
                userId = null,
                refreshToken,
                expireTime,
                userType,
                didToken
            )

            val needCreateWallet = Managers.walletManager.getState() != WalletManager.STATE_OK

            // 该邮箱绑定过钱包
            loginResultData = LoginResultData.Success(
                accessToken = accessToken,
                openId = authId,
                needCreateWallet = needCreateWallet
            )
            DAuthLogger.d("exchange token success")
        } else {
            val loginResCode = authorizeToken2Res.ret
            loginResultData = LoginResultData.Failure(loginResCode)
            DAuthLogger.e("exchange token failure:$loginResCode")
        }
        return loginResultData
    }

}