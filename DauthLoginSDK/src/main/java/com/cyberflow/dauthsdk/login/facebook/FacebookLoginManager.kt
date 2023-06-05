package com.cyberflow.dauthsdk.login.facebook

import android.content.Context
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.facebook.FacebookSdk

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