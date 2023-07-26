package com.cyberflow.dauthsdk.login.impl

import com.cyberflow.dauthsdk.api.entity.ResponseCode
import com.cyberflow.dauthsdk.login.model.RefreshTokenParam
import com.cyberflow.dauthsdk.login.model.RefreshTokenParamRes
import com.cyberflow.dauthsdk.login.network.BaseResponse
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.login.utils.maskSensitiveData
import com.cyberflow.dauthsdk.wallet.impl.manager.Managers

internal class TokenManager private constructor() {

    companion object {
        internal val instance: TokenManager by lazy {
            TokenManager()
        }
    }

    private val prefs get() = Managers.loginPrefs
    private val requestApi get() = Managers.requestApi

    private suspend fun refreshToken(): RefreshTokenParamRes? {
        // 刷新token
        val authId = prefs.getAuthId()
        val userType = prefs.getUserType()
        val refreshToken = prefs.getRefreshToken()
        val body = RefreshTokenParam(authId, userType, refreshToken)
        return requestApi.refreshToken(body)
    }

    internal suspend fun <T> authenticatedRequest(request: () -> T?): T? {
        val response = request()
        val baseResponse = response as? BaseResponse ?: return null
        if (baseResponse.ret == ResponseCode.TOKEN_IS_INVALIDATE) {
            val refreshResponse = refreshToken()
            if (refreshResponse != null && refreshResponse.isSuccess()) {
                val newAccessToken = refreshResponse.data?.accessToken
                val newRefreshToken = refreshResponse.data?.refreshToken
                val expireTime = refreshResponse.data?.expireIn
                DAuthLogger.d("save new token:${newAccessToken?.maskSensitiveData()}")
                prefs.putLoginInfo(newAccessToken, refreshToken = newRefreshToken, expireTime = expireTime)
                return request()
            }
        }
        return response
    }

}
