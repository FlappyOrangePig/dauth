package com.cyberflow.dauthsdk.login.impl

import com.cyberflow.dauthsdk.api.entity.LoginResultData
import com.cyberflow.dauthsdk.api.entity.ResponseCode
import com.cyberflow.dauthsdk.login.model.AuthorizeToken2Param
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.login.utils.JwtDecoder
import com.cyberflow.dauthsdk.login.utils.LoginPrefs
import com.cyberflow.dauthsdk.wallet.impl.manager.Managers
import com.cyberflow.dauthsdk.wallet.impl.manager.WalletManager


class ThirdPlatformLogin private constructor() {

    private val prefs get() = Managers.loginPrefs

    companion object {
        val instance by lazy {
            ThirdPlatformLogin()
        }
    }

    suspend fun thirdPlatFormLogin(body: AuthorizeToken2Param) : LoginResultData {
        var loginResultData: LoginResultData? = null
        val authorizeToken2Res = RequestApi().authorizeExchangedToken(body)
        if (authorizeToken2Res?.iRet == ResponseCode.RESPONSE_CORRECT_CODE) {
            val didToken = authorizeToken2Res.data?.didToken.orEmpty()
            val googleUserInfo = JwtDecoder().decoded(didToken)
            val accessToken = authorizeToken2Res.data?.accessToken.orEmpty()
            DAuthLogger.d("第三方登录后获取的accessToken：$accessToken")
            val refreshToken = authorizeToken2Res.data?.refreshToken.orEmpty()
            val authId = googleUserInfo.sub.orEmpty()
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

            // 钱包未创建
            if (Managers.walletManager.getState() != WalletManager.STATE_OK) {
                return LoginResultData.Failure(
                    ResponseCode.AA_WALLET_IS_NOT_CREATE,
                    accessToken,
                    authId
                )
            }

            // 该邮箱绑定过钱包
            loginResultData = LoginResultData.Success(ResponseCode.RESPONSE_CORRECT_CODE, accessToken, authId)
            DAuthLogger.d("第三方账号已绑定钱包，直接进入主页")
        } else {
            val loginResCode = authorizeToken2Res?.iRet
            loginResultData = LoginResultData.Failure(loginResCode)
            DAuthLogger.e("第三方认证登录失败 errCode == $loginResCode")
        }
        return loginResultData
    }

}