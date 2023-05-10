package com.cyberflow.dauthsdk.facebook

import android.app.Activity
import android.content.Context
import android.util.Log
import com.facebook.AccessToken
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager

private const val TAG = "FacebookLoginManager"
private const val FACEBOOK_PERMISSIONS = "public_profile"
class FacebookLoginManager {

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            FacebookLoginManager()
        }
    }

    //在setContentView之前调用
    fun initFacebookSDK(context: Context) {
        FacebookSdk.sdkInitialize(context) {
            Log.e(TAG, "init fbSDK success")
        }
    }

    fun faceBookAuth(activity: Activity) {
        LoginManager.getInstance().logInWithReadPermissions(activity, listOf(FACEBOOK_PERMISSIONS))
        AccessToken.getCurrentAccessToken()
    }

}