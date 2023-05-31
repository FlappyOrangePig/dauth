package com.cyberflow.dauthsdk

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import com.cyberflow.dauthsdk.constant.LoginType
import com.cyberflow.dauthsdk.google.GoogleLoginManager
import com.cyberflow.dauthsdk.`interface`.DAuthCallback
import com.cyberflow.dauthsdk.`interface`.ResetPwdCallback
import com.cyberflow.dauthsdk.model.CreateAccountParam
import com.cyberflow.dauthsdk.model.LoginParam
import com.cyberflow.dauthsdk.model.LoginRes
import com.cyberflow.dauthsdk.model.SendEmailVerifyCodeParam
import com.cyberflow.dauthsdk.network.RequestApi
import com.cyberflow.dauthsdk.twitter.TwitterLoginManager
import com.cyberflow.dauthsdk.utils.DAuthLogger
import com.cyberflow.dauthsdk.utils.SignUtils
import com.cyberflow.dauthsdk.utils.ThreadPoolUtils
import com.cyberflow.dauthsdk.utils.ValidatorUtil
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import kotlinx.coroutines.*


private const val ACCOUNT_TYPE_OF_OWN = 70  //自定义账号
private const val ACCOUNT_TYPE_OF_EMAIL = 10  //邮箱
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

    fun login(account: String, passWord: String, callback: DAuthCallback<LoginRes>) {
        var code: Int
        val map = HashMap<String, String>()
        map["user_type"] = "10"
        map["account"] = account
        map["verify_code"] = passWord
        val sign = SignUtils.sign(map)
        val loginParam = LoginParam(10, account = account, verify_code = passWord, sign = sign)
        ThreadPoolUtils.execute {
            val loginRes = RequestApi().login(loginParam)

            if (loginRes != null) {
                code = loginRes.iRet
                if (code == 0) {
                    callback.onResult(loginRes)
                } else {
                    callback.onFailed(loginRes.sMsg.orEmpty())
                }
            }
            val msg = loginRes?.sMsg
            DAuthLogger.d("login return : $msg")
        }

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


    fun createDAuthAccount(account: String, passWord: String, oPassWord: String) {
        val map = HashMap<String,String?>()
        var userType: Int = 0
        if(ValidatorUtil().isEmail(account)) {
            userType = ACCOUNT_TYPE_OF_EMAIL
            map["email"] = account
        } else {        //TODO 其他暂时默认自有账号登录
            userType = ACCOUNT_TYPE_OF_OWN
            map["account"] = account
        }

        map["user_type"] = userType.toString()
        map["uuid"] = "123456"

        map["is_login"] = "1"
        map["sex"] = "0"
        map["password"] = passWord
        map["confirm_password"] = oPassWord
        val sign = SignUtils.sign(map)
        val createAccountParam = CreateAccountParam(userType, "123456", sign, 1,
            passWord, passWord, sex = 0, email = account)
        ThreadPoolUtils.execute {
            val createAccount = RequestApi().createAccount(createAccountParam)
            DAuthLogger.d("createAccount return : ${createAccount?.sMsg}")
        }
    }


    fun logout() {

    }

    fun setRecoverPassword(callback: ResetPwdCallback) {
        callback.success()
    }

    fun sendVerifyCode(): Boolean = runBlocking {
        var isSend: Boolean

        val map = HashMap<String, String>()
        map["account"] = "453376077@qq.com"
        val sign = SignUtils.sign(map)
        val body = SendEmailVerifyCodeParam("453376077@qq.com", sign)

        withContext(Dispatchers.IO) {
            isSend = RequestApi().sendEmailVerifyCode(body)
        }

        DAuthLogger.d("发送验证码接口返回：$isSend")
        return@runBlocking isSend
    }


}