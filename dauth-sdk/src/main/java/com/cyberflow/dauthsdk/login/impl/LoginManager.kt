package com.cyberflow.dauthsdk.login.impl

import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.login.model.RefreshTokenParam
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.utils.LoginPrefs

class LoginManager private constructor() {

    private val context get() = DAuthSDK.impl.context
    companion object {
        val instance: LoginManager by lazy { LoginManager() }
    }

    suspend fun refreshAccessToken(authId: String, userType: Int): String {
        val refreshToken  = LoginPrefs(context).getRefreshToken()
        val body = RefreshTokenParam(authId, userType, refreshToken)
        val refreshTokenRes = RequestApi().refreshToken(body)
        return refreshTokenRes?.access_token.orEmpty()
    }

    fun autoLogin() {

    }
}