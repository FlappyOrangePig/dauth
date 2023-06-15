package com.cyberflow.dauthsdk.login.impl

import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.login.model.RefreshTokenParam
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.login.utils.LoginPrefs
import java.util.concurrent.TimeUnit

val context get() = DAuthSDK.impl.context

class TokenManager() {

    companion object {
        val instance: TokenManager by lazy {
            TokenManager()
        }
    }

    fun validateToken(): Boolean {
        val tokenExpirationTime = LoginPrefs(context).getExpireTime()
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

    fun refreshToken(): String? {
        // 刷新token
        val authId = LoginPrefs(context).getAuthId()
        val userType = LoginPrefs(context).getUserType()
        val refreshToken = LoginPrefs(context).getRefreshToken()
        val body = RefreshTokenParam(authId, userType, refreshToken)
        val response = RequestApi().refreshToken(body)
        if(response?.iRet == 0) {
            val expireTime = response.data?.expire_in
            val accessToken = response.data?.access_token
            DAuthLogger.d("刷新accessToken成功:$accessToken")
            LoginPrefs(context).setAccessToken(accessToken.orEmpty())
            expireTime?.let {
                LoginPrefs(context).setExpireTime(it)
                DAuthLogger.d("刷新后accessToken过期时间:$it")
            }
            return accessToken
        }
        return null
    }

    private fun updateToken(newToken: String?) {
        // 更新令牌信息
        LoginPrefs(context).setAccessToken(newToken.orEmpty())
    }

    inline fun <reified T> authenticatedRequest(crossinline request: (accessToken: String?) -> T
    ): T? {
        val isValidate = validateToken()
        return if (isValidate) {
            val accessToken = LoginPrefs(context).getAccessToken()
            DAuthLogger.e("TokenManager accessToken is valid:$accessToken")
            request(accessToken)
        } else {
            // 30天未操作，accessToken不刷新
            DAuthLogger.e("Token is invalid.")
            null
        }
    }

}
