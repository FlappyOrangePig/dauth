package com.cyberflow.dauthsdk.facebook

import android.app.Activity
import android.content.Context
import com.cyberflow.dauthsdk.utils.DAuthLogger
import com.facebook.AccessToken
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager

private const val FACEBOOK_PERMISSIONS = "public_profile"
class FacebookLoginManager {

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            FacebookLoginManager()
        }
    }

    //在setContentView之前调用  此方法已废弃  fb会自动初始化
    fun initFacebookSDK(context: Context) {
        FacebookSdk.sdkInitialize(context.applicationContext) {
            DAuthLogger.e( "init fbSDK success")
        }
    }
}