package com.infras.dauthsdk.login.google

import android.app.Activity
import android.content.Intent
import androidx.core.app.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.infras.dauthsdk.api.DAuthSDK
import com.infras.dauthsdk.api.entity.LoginResultData
import com.infras.dauthsdk.login.impl.ThirdPlatformLogin
import com.infras.dauthsdk.login.model.AuthorizeToken2Param
import com.infras.dauthsdk.login.utils.DAuthLogger
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val AUTH_TYPE_OF_GOOGLE = 30

private const val TAG = "GoogleLoginManager"

class GoogleLoginManager {
    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            GoogleLoginManager()
        }
    }

    private fun signInClient(activity: Activity): GoogleSignInClient {
        val googleClientId = DAuthSDK.impl.config.googleClientId.orEmpty()
        return GoogleSignIn.getClient(
            activity,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(googleClientId)
                .requestEmail()
                .build()
        )
    }

    fun googleSignInAuth(activity: ComponentActivity, requestCodeSignIn: Int, requestCodeOpenGoogleService: Int) {
        val api = GoogleApiAvailability.getInstance()
        val result = api.isGooglePlayServicesAvailable(activity)
        DAuthLogger.d("google available:$result")
        if (result != ConnectionResult.SUCCESS) {
            if (api.isUserResolvableError(result)) {
                DAuthLogger.d("let user open")
                api.getErrorDialog(
                    activity,
                    result,
                    requestCodeOpenGoogleService
                )?.apply {
                    setOnDismissListener { activity.finish() }
                    show()
                } ?: activity.finish()
            } else {
                activity.finish()
            }
            return
        }

        val googleSignInClient = signInClient(activity)
        activity.lifecycleScope.launch {
            if (GoogleSignIn.getLastSignedInAccount(activity) != null) {
                // logout the account so that the user can change another account
                DAuthLogger.d("google sign out start...", TAG)
                try {
                    googleSignInClient.signOut().await()
                    DAuthLogger.d("google sign out done", TAG)
                } catch (e: Exception) {
                    // ignore
                    DAuthLogger.w( "google sign out error: $e", TAG)
                }
            }
            val signInIntent = googleSignInClient.signInIntent
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
            DAuthLogger.e("google sign failed:${e.stackTraceToString()}")
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