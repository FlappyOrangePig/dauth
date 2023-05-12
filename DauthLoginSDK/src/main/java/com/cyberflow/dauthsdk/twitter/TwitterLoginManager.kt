package com.cyberflow.dauthsdk.twitter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cyberflow.dauthsdk.utils.DAuthLogger
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import com.twitter.sdk.android.core.identity.TwitterLoginButton
import com.twitter.sdk.android.core.internal.CommonUtils
import com.twitter.sdk.android.core.models.User

private const val CONSUMER_KEY = "tfCWoaQgJqsbAsYNKFM8r2rI3"
private const val CONSUMER_SECRET = "hUbRMtwQNgyaxRMCDaYRoezV9Z7xGoJk4i3kseFSFP4mfr3b9v"

private const val OAUTH_TWO_CLIENT_ID = "SUVaMUdQRGRHMjRYaDhoR2kweU06MTpjaQ"
private const val OAUTH_TWO_Client_SECRET = "s8hvX4YYmGxzp4h_afm_CCBgmtg7EH6uRjTeHGmuHRuiyYPGSV"
private const val OAUTH_TWO_ACCESS_TOKEN = "1656548404338753538-sR3G2djH8kMytF7iAvkyhxNbCxaIr3"
private const val OAUTH_TWO_ACCESS_TOKEN_SECRET = "AJSzCIw2zCI7vVw2F6haxpIJEbQdiBBCOluAsScC6m7A3"
private const val OAUTH_TWO_BEARER_TOKEN = "AAAAAAAAAAAAAAAAAAAAALGinQEAAAAAoZGBdFKg7jMnmjy6%2FKtaal3Z9ns%3DJydL7xthxkxRB0KoG1A358Bc5qpNXQ6eAr4toyStyu8Ub7Zv0l"
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

            CommonUtils.logOrThrowIllegalStateException(TwitterCore.TAG,

                "Callback must not be null, did you call setCallback?")
        }

    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == twitterAuthClient?.requestCode) {
            twitterAuthClient?.onActivityResult(requestCode, resultCode, data)
            val twitterApiClient  = TwitterCore.getInstance().apiClient
            val call = twitterApiClient.accountService.verifyCredentials(false,true,true)
            call.enqueue(object :Callback<User>() {
                override fun success(result: Result<User>?) {
                    val userInfo = result?.data
                    val email = result?.data?.email
                    DAuthLogger.d("get twitter userinfo==$userInfo")
                }

                override fun failure(exception: TwitterException?) {
                    TODO("Not yet implemented")
                }

            })
        }
    }

}