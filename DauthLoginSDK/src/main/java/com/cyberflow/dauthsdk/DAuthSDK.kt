package com.cyberflow.dauthsdk

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.cyberflow.dauthsdk.constant.LoginType
import com.cyberflow.dauthsdk.google.GoogleLoginManager
import com.cyberflow.dauthsdk.twitter.TwitterLoginManager
import com.cyberflow.dauthsdk.utils.DAuthLogger
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession


class DAuthSDK {

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            DAuthSDK()
        }
    }

    fun initSDK(activity: Activity , appId: String, appKey: String) {
        val intent = Intent()
        intent.component =
            ComponentName("com.cyberflow.dauthsdk", "com.cyberflow.dauthsdk.view.DAuthActivity");
        activity.startActivity(intent);
    }

    fun login() {

    }

     fun loginWithType(type: String, activity: Activity) {
        when (type) {
            LoginType.GOOGLE -> {
                GoogleLoginManager.instance.googleSignInAuth(activity)
            }
            LoginType.TWITTER -> {
                TwitterLoginManager.instance.twitterLoginAuth(
                    activity, object : Callback<TwitterSession>() {
                        override fun success(result: Result<TwitterSession>?) {
                            val token = result?.data?.authToken
                            val userId = result?.data?.userId

                            DAuthLogger.d("twitter login success")
                        }

                        override fun failure(exception: TwitterException?) {
                            DAuthLogger.e("twitter login failed:$exception")
                        }

                    }
                )
            }
        }
    }

    fun createDAuthAccount(userName: String, passWord: String, oPassWord: String) {

    }


    fun logout() {

    }

}