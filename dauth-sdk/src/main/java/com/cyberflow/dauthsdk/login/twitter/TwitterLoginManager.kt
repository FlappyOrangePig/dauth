package com.cyberflow.dauthsdk.login.twitter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.login.api.SdkConfig
import com.cyberflow.dauthsdk.login.model.DAuthUser
import com.cyberflow.dauthsdk.login.model.AuthorizeToken2Param
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.utils.*
import com.cyberflow.dauthsdk.login.utils.LoginPrefs
import com.google.gson.Gson
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import com.twitter.sdk.android.core.identity.TwitterLoginButton
import com.twitter.sdk.android.core.internal.CommonUtils
import com.twitter.sdk.android.core.models.User
import kotlinx.coroutines.*
import kotlin.coroutines.resume


private const val TYPE_OF_TWITTER = "110"

class TwitterLoginManager {

    private var callback: Callback<TwitterSession>? = null
    private val context get() = (DAuthSDK.instance).context
    @Volatile

    var authClient: TwitterAuthClient? = null

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            TwitterLoginManager()
        }
    }

    fun initTwitterSDK(context: Context, sdkConfig: SdkConfig) {
        val config = TwitterConfig.Builder(context)
            .logger(DefaultLogger(Log.DEBUG))
            .twitterAuthConfig(TwitterAuthConfig(sdkConfig.twitterConsumerKey, sdkConfig.twitterConsumerSecret))
            .debug(true)
            .build()
        Twitter.initialize(config)
    }

    fun twitterLoginAuth(activity: Activity, callback: Callback<TwitterSession>?) {
        this.callback = callback
        checkCallback(callback)
        twitterAuthClient?.authorize(activity, callback)
    }


    private val twitterAuthClient: TwitterAuthClient?
        get() {
            if (authClient == null) {
                synchronized(TwitterLoginButton::class.java) {
                    if (authClient == null) {
                        authClient = TwitterAuthClient()
                    }
                }
            }
            return authClient
        }

    private fun checkCallback(callback: Callback<*>?) {
        if (callback == null) {
            CommonUtils.logOrThrowIllegalStateException(
                TwitterCore.TAG,
                "Callback must not be null, did you call setCallback?"
            )
        }
    }

    private suspend fun twitterAuth(requestCode: Int, resultCode: Int, data: Intent?): String? =
        suspendCancellableCoroutine {
        val userData = DAuthUser()
        twitterAuthClient?.onActivityResult(requestCode, resultCode, data)
        val twitterApiClient = TwitterCore.getInstance().apiClient
        val call = twitterApiClient.accountService.verifyCredentials(false, true, true)
        call.enqueue(object : Callback<User>() {
            override fun success(result: Result<User>?) {
                val twitterUserInfo = result?.data
                userData.email = twitterUserInfo?.email
                userData.openid = twitterUserInfo?.idStr
                userData.head_img_url = twitterUserInfo?.profileImageUrl
                userData.nickname = twitterUserInfo?.screenName
                val gson = Gson()
                val twitterUser = gson.toJson(userData)
                it.resume(twitterUser)
            }

            override fun failure(exception: TwitterException?) {
                DAuthLogger.e("twitter 授权失败: $exception")
                it.resume(null)
            }
        })
    }

    suspend fun twitterAuthLogin(requestCode: Int, resultCode: Int, data: Intent?): Int {
        var loginResCode = -1
        var twitterUser :String? = null
        try {
            twitterUser = twitterAuth(requestCode, resultCode, data)
        }catch (e: Exception) {
            DAuthLogger.e("suspendCancellableCoroutine exception:$e")
        }
        withContext(Dispatchers.IO) {
            val body = AuthorizeToken2Param(
                access_token = null,
                refresh_token = null,
                user_type = TYPE_OF_TWITTER,
                commonHeader = null,
                id_token = null,
                user_data = twitterUser
            )
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
                    loginResCode = 10001
                } else {
                    // 该邮箱绑定过钱包
                    loginResCode = 0
                    DAuthLogger.d("该twitter账号已绑定钱包，直接进入主页")
                }
            } else {
                loginResCode = 100001
                DAuthLogger.e("app第三方认证登录失败 errCode == $loginResCode")
            }
        }
        return loginResCode
    }

}