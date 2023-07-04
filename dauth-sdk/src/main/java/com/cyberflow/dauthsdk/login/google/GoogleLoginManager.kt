package com.cyberflow.dauthsdk.login.google

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import com.cyberflow.dauthsdk.api.entity.LoginResultData
import com.cyberflow.dauthsdk.login.impl.ThirdPlatformLogin
import com.cyberflow.dauthsdk.login.model.AuthorizeToken2Param
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.wallet.ext.app
import com.cyberflow.dauthsdk.login.utils.ToastUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.abi.datatypes.Bool


private const val REQUEST_CODE = 9004
private const val AUTH_TYPE_OF_GOOGLE = "30"

class GoogleLoginManager {
    val context get() = app()
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
        if(onCheckGooglePlayServices(activity, code)) {
            val signInIntent: Intent = signInClient(activity)!!.signInIntent
            activity.startActivityForResult(signInIntent, REQUEST_CODE)
        } else {
            ToastUtil.show(activity.applicationContext,"Google service is unavailable")
            activity.finish()
        }
    }

    private fun onCheckGooglePlayServices(activity: Activity, code: Int) : Boolean {
        // 验证是否已在此设备上安装并启用Google Play服务，以及此设备上安装的旧版本是否为此客户端所需的版本
        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(activity)
        // 通过isUserResolvableError来确定是否可以通过用户操作解决错误
        if (GoogleApiAvailability.getInstance().isUserResolvableError(code)) {
            return false
        }
        return true
    }

    private fun checkGooglePlayServiceExist(activity: Activity): Int {
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity)

        return status
    }

    private fun getGoogleIdToken(data: Intent?) : String {
        var accountIdToken = ""
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            // Signed in successfully, show authenticated UI.
            val accountId = account?.id.toString()
            accountIdToken = account?.idToken.toString()
            DAuthLogger.e("account:$account, accountId:$accountId ,accountIdToken: $accountIdToken")
        }catch (e: Exception) {
            DAuthLogger.e("google sign failed:$e")
        }
        return accountIdToken
    }

     suspend fun googleAuthLogin(data: Intent?) : LoginResultData? {
         var loginResultData: LoginResultData? = null
         val accountIdToken = getGoogleIdToken(data)
         if(accountIdToken.isNotEmpty()) {
             val authorizeParam = AuthorizeToken2Param(
                 access_token = null,
                 refresh_token = null,
                 AUTH_TYPE_OF_GOOGLE,
                 commonHeader = null,
                 id_token = accountIdToken
             )
             withContext(Dispatchers.IO) {
                 loginResultData = ThirdPlatformLogin.instance.thirdPlatFormLogin(authorizeParam)
             }
         }
        return loginResultData
    }

}