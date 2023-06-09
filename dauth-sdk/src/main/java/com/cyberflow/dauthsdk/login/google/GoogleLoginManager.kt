package com.cyberflow.dauthsdk.login.google

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import com.cyberflow.dauthsdk.wallet.api.IWalletApi
import com.cyberflow.dauthsdk.wallet.impl.WalletHolder
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.GoogleApiAvailability


private const val REQUEST_CODE = 9001
private const val TAG = "GoogleLoginManager"
private const val AUTH_TYPE_OF_GOOGLE = "30"
private const val USER_TYPE = "user_type"
class GoogleLoginManager : IWalletApi by WalletHolder.walletApi {

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            GoogleLoginManager()
        }
    }

    //Google sdk init
    private fun signInClient(activity: Activity): GoogleSignInClient? {
        try {
            val applicationInfo = activity.packageManager.getApplicationInfo(activity.packageName,
                PackageManager.GET_META_DATA)
            val metaData = applicationInfo.metaData
            val serverClientId = metaData.getString("com.google.android.gms.games.APP_ID").orEmpty()
            //requestIdToken需要使用Web客户端ID才能成功，不要使用安卓ClientID
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(serverClientId)
                .requestEmail()
                .build()
            return GoogleSignIn.getClient(activity, gso)
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
        }
       return null
    }

    //登录
    fun googleSignInAuth(activity: Activity) {

        val code = checkGooglePlayServiceExist(activity)
        onCheckGooglePlayServices(activity, code)
        val signInIntent: Intent = signInClient(activity)!!.signInIntent

        activity.startActivityForResult(signInIntent, REQUEST_CODE)
    }

    private fun onCheckGooglePlayServices(activity: Activity, code: Int) {

        // 验证是否已在此设备上安装并启用Google Play服务，以及此设备上安装的旧版本是否为此客户端所需的版本
        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(activity)

        //通过isUserResolvableError来确定是否可以通过用户操作解决错误
        if (GoogleApiAvailability.getInstance().isUserResolvableError(code)) {

            GoogleApiAvailability.getInstance().getErrorDialog(activity, code, 200)?.show()

        }

    }

    private fun checkGooglePlayServiceExist(activity: Activity): Int {
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity)

        return status
    }

}