package com.cyberflow.dauthsdk.twitter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cyberflow.dauthsdk.login.DAuthUser
import com.cyberflow.dauthsdk.model.AuthorizeToken2Param
import com.cyberflow.dauthsdk.network.AccountApi
import com.cyberflow.dauthsdk.utils.DAuthLogger
import com.cyberflow.dauthsdk.utils.SignUtils
import com.cyberflow.dauthsdk.utils.ThreadPoolUtils
import com.google.gson.Gson
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import com.twitter.sdk.android.core.identity.TwitterLoginButton
import com.twitter.sdk.android.core.internal.CommonUtils
import com.twitter.sdk.android.core.models.User

private const val CONSUMER_KEY = "tfCWoaQgJqsbAsYNKFM8r2rI3"
private const val CONSUMER_SECRET = "hUbRMtwQNgyaxRMCDaYRoezV9Z7xGoJk4i3kseFSFP4mfr3b9v"

private const val TYPE_OF_TWITTER = 110L
private const val USER_TYPE = "user_type"
private const val USER_DATA = "user_data"

class TwitterLoginManager() {

    private var callback: Callback<TwitterSession>? = null

    @Volatile

    var authClient: TwitterAuthClient? = null

    companion object {

        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            TwitterLoginManager()
        }

    }

    fun initTwitterSDK(context: Context) {
        val config = TwitterConfig.Builder(context)
            .logger(DefaultLogger(Log.DEBUG))
            .twitterAuthConfig(TwitterAuthConfig(CONSUMER_KEY, CONSUMER_SECRET))
            .debug(true)
            .build()
        Twitter.initialize(config)
        DAuthLogger.d("twitter is init")
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

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == twitterAuthClient?.requestCode) {
            twitterAuthClient?.onActivityResult(requestCode, resultCode, data)
            val twitterApiClient = TwitterCore.getInstance().apiClient
            val call = twitterApiClient.accountService.verifyCredentials(false, true, true)
            call.enqueue(object : Callback<User>() {
                override fun success(result: Result<User>?) {
                    val twitterUserInfo = result?.data
                    val userData = DAuthUser()
                    userData.email = twitterUserInfo?.email
                    userData.openid = twitterUserInfo?.idStr
                    userData.nickname = twitterUserInfo?.screenName
                    userData.head_img_url = twitterUserInfo?.profileImageUrl
                    val gson = Gson()
                    val userDataStr = gson.toJson(userData)
                    val map = HashMap<String, String?>()
                    map[USER_TYPE] = TYPE_OF_TWITTER.toString()
                    map[USER_DATA] = userDataStr
                    val sign = SignUtils.sign(map)
                    val body = AuthorizeToken2Param(
                        access_token = null,
                        refresh_token = null,
                        user_type = TYPE_OF_TWITTER,
                        sign,
                        commonHeader = null,
                        id_token = null,
                        userDataStr
                    )

                    ThreadPoolUtils.execute {
                        val data = AccountApi().authorizeExchangedToken(body)
                        DAuthLogger.d("twitter login auth return : $data")
                    }

                    DAuthLogger.d("get twitter userinfo==$twitterUserInfo")
                }

                override fun failure(exception: TwitterException?) {
                    DAuthLogger.e("twitter auth exception:$exception")
                }

            })
        }
    }

}