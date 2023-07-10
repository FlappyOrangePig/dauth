package com.cyberflow.dauthsdk.login.impl

import androidx.annotation.VisibleForTesting
import com.cyberflow.dauthsdk.api.entity.ResponseCode
import com.cyberflow.dauthsdk.login.model.RefreshTokenParam
import com.cyberflow.dauthsdk.login.network.BaseResponse
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.login.utils.LoginPrefs
import com.cyberflow.dauthsdk.wallet.ext.app
import com.cyberflow.dauthsdk.wallet.impl.manager.Managers
import java.util.concurrent.TimeUnit

val context get() = app()

class TokenManager private constructor() {

    companion object {
        val instance: TokenManager by lazy {
            TokenManager()
        }
    }

    val prefs get() = Managers.loginPrefs

    suspend fun validateToken(): Boolean {
        val tokenExpirationTime = prefs.getExpireTime()
        val currentTime = System.currentTimeMillis() / 1000
        // refreshToken过期时间
        val expirationTime = tokenExpirationTime + TimeUnit.DAYS.toMillis(30) / 1000
        return when {
            currentTime < tokenExpirationTime -> true // 令牌尚未过期
            // refreshToken未过期, accessToken过期
            (currentTime < expirationTime) && (currentTime > tokenExpirationTime) -> {
                val refreshedAccessToken = refreshToken()
                if (refreshedAccessToken != null) {
                    updateToken(refreshedAccessToken) // 刷新令牌成功，更新令牌信息
                    true
                } else {
                    false // 刷新令牌失败
                }
            }
            else -> {
                // accessToken最后一次有效期到现在超过了30天，需要重新授权
                false
            }
        }
    }

    suspend fun refreshToken(): String? {
        // 刷新token
        val authId = prefs.getAuthId()
        val userType = prefs.getUserType()
        val refreshToken = prefs.getRefreshToken()
        val body = RefreshTokenParam(authId, userType, refreshToken)
        val response = RequestApi().refreshToken(body)
        if(response?.iRet == 0) {
            val expireTime = response.data?.expireIn
            val accessToken = response.data?.accessToken
            DAuthLogger.d("刷新accessToken成功:$accessToken")
            prefs.setAccessToken(accessToken.orEmpty())
            expireTime?.let {
                prefs.setExpireTime(it)
                DAuthLogger.d("刷新后accessToken过期时间:$it")
            }
            return accessToken
        }
        return null
    }

    private fun updateToken(newToken: String?) {
        // 更新令牌信息
        prefs.setAccessToken(newToken.orEmpty())
    }

    suspend inline fun <T> authenticatedRequest(crossinline request: (accessToken: String?) -> T?): T? {
        val isValidate = validateToken()
        return if (isValidate) {
            val accessToken = prefs.getAccessToken()
            DAuthLogger.e("TokenManager accessToken is valid: $accessToken")
            val response = request(accessToken)
            val baseResponse = response as? BaseResponse ?: return null
            if (baseResponse.iRet == ResponseCode.TOKEN_IS_INVALIDATE) {
                val newAccessToken = refreshToken()
                request(newAccessToken)
            } else {
                response
            }
        } else {
            DAuthLogger.e("Token is invalid.")
            null
        }
    }

}
