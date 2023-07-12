package com.cyberflow.dauthsdk.login.google

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.entity.LoginResultData
import com.cyberflow.dauthsdk.login.impl.ThirdPlatformLogin
import com.cyberflow.dauthsdk.login.model.AuthorizeToken2Param
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.wallet.ext.app
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException


private const val AUTH_TYPE_OF_GOOGLE = "30"

class GoogleLoginManager {
    val context get() = app()
    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            GoogleLoginManager()
        }
    }

    private fun signInClient(activity: Activity): GoogleSignInClient? {
        try {
            val googleClientId = DAuthSDK.impl.config.googleClientId.orEmpty()
            //requestIdToken需要使用Web客户端ID才能成功，不要使用安卓ClientID
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(googleClientId)
                .requestEmail()
                .build()
            return GoogleSignIn.getClient(activity, gso)
        } catch (e: NameNotFoundException) {
            DAuthLogger.e(e.stackTraceToString())
        }
       return null
    }

    fun googleSignInAuth(activity: Activity, requestCodeSignIn: Int, requestCodeOpenGoogleService: Int) {
        val api = GoogleApiAvailability.getInstance()
        val result = api.isGooglePlayServicesAvailable(activity)
        DAuthLogger.d("google available:$result")
        if (result != ConnectionResult.SUCCESS) {
            if (api.isUserResolvableError(result)) {
                DAuthLogger.d("let user open")
                val dialog = api.getErrorDialog(
                    activity,
                    result,
                    requestCodeOpenGoogleService
                ) {
                    activity.finish()
                }
                if (dialog != null) {
                    dialog.show()
                } else {
                    activity.finish()
                }
            }
            return
        }

        signInClient(activity)?.let {
            val signInIntent = it.signInIntent
            activity.startActivityForResult(signInIntent, requestCodeSignIn)
        }
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
             loginResultData = ThirdPlatformLogin.instance.thirdPlatFormLogin(authorizeParam)
         }
        return loginResultData
    }

}