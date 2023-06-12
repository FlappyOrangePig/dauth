package com.cyberflow.dauthsdk.login.impl

import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.ILoginApi
import com.cyberflow.dauthsdk.login.model.AuthorizeToken2Param
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.login.utils.JwtDecoder
import com.cyberflow.dauthsdk.login.utils.LoginPrefs

class ThirdPlatformLogin {

    private val context get() = DAuthSDK.impl.context
    companion object {
        val instance by lazy {
            ThirdPlatformLogin()
        }
    }

    fun thirdPlatFormLogin(body: AuthorizeToken2Param) : Int {
        var loginResCode = 100001
        val authorizeToken2Res = RequestApi().authorizeExchangedToken(body)
        if (authorizeToken2Res?.iRet == 0) {
            val didToken = authorizeToken2Res.data?.did_token.orEmpty()
            val googleUserInfo = JwtDecoder().decoded(didToken)
            val accessToken = authorizeToken2Res.data?.d_access_token.orEmpty()
            val authId = googleUserInfo.sub.orEmpty()
            val queryWalletRes = RequestApi().queryWallet(accessToken, authId)
            LoginPrefs(context).setAccessToken(accessToken)
            LoginPrefs(context).setAuthID(authId)
            //没有钱包  返回errorCode
            if (queryWalletRes?.data?.address.isNullOrEmpty()) {
                loginResCode = 200001
            } else {
                // 该邮箱绑定过钱包
                loginResCode = 0
                DAuthLogger.d("第三方账号已绑定钱包，直接进入主页")
            }
        } else {
            loginResCode = 100001
            DAuthLogger.e("第三方认证登录失败 errCode == $loginResCode")
        }
        return loginResCode
    }

}