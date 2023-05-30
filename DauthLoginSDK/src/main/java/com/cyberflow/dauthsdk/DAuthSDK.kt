package com.cyberflow.dauthsdk

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.cyberflow.dauthsdk.constant.LoginType
import com.cyberflow.dauthsdk.google.GoogleLoginManager
import com.cyberflow.dauthsdk.`interface`.ResetPwdCallback
import com.cyberflow.dauthsdk.model.CreateAccountParam
import com.cyberflow.dauthsdk.model.LoginParam
import com.cyberflow.dauthsdk.network.AccountApi
import com.cyberflow.dauthsdk.twitter.TwitterLoginManager
import com.cyberflow.dauthsdk.utils.DAuthLogger
import com.cyberflow.dauthsdk.utils.SignUtils
import com.cyberflow.dauthsdk.utils.ThreadPoolUtils
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession


private const val ACCOUNT_TYPE = 70  //自定义账号
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

    fun login(account: String, passWord: String? = null) {
//        AccountApi().login(LoginParam())
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


    fun createDAuthAccount(userName: String, passWord: String, oPassWord: String) : Boolean{
        val map = HashMap<String,String?>()
        map["user_type"] = ACCOUNT_TYPE.toString()
        map["uuid"] = "123456"
        map["account"] = "test123"
        map["is_login"] = "1"
        map["sex"] = "0"
        map["password"] = "Tt123456"
        map["confirm_password"] = "Tt123456"
        val sign = SignUtils.sign(map)
        val createAccountParam = CreateAccountParam(ACCOUNT_TYPE, "123456", sign, 1,
            "Tt123456", "Tt123456" , sex = 0, account = "test123",)
        ThreadPoolUtils.execute {
            val createAccount = AccountApi().createAccount(createAccountParam)
            DAuthLogger.d("createAccount return : $createAccount")
        }
        return true
    }


    fun logout() {

    }

    fun setRecoverPassword(callback: ResetPwdCallback) {
        callback.success()
    }

    fun sendVerifyCode() : Boolean {
        return true
    }

}