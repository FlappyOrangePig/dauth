package com.cyberflow.dauthsdk.google

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauthsdk.login.DAuthUser
import com.cyberflow.dauthsdk.model.AuthorizeToken2Param
import com.cyberflow.dauthsdk.network.AccountApi
import com.cyberflow.dauthsdk.utils.DAuthLogger
import com.cyberflow.dauthsdk.utils.SignUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val REQUEST_CODE = 9001
private const val TAG = "GoogleLoginManager"
private const val TEST_SERVER_CLIENT_ID = "535517245452-2qnvgf1sp5vgb9ra6ouelgnedllj4iqs.apps.googleusercontent.com"
class GoogleLoginManager {

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            GoogleLoginManager()
        }
    }

    //Google sdk init
    private fun signInClient(activity: Activity): GoogleSignInClient {
        //requestIdToken需要使用Web客户端ID才能成功，不要使用安卓ClientID
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(TEST_SERVER_CLIENT_ID)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso)
    }

    //登录
    fun googleSignInAuth(activity: Activity) {

        val code = checkGooglePlayServiceExist(activity)
        onCheckGooglePlayServices(activity, code)
        val signInIntent: Intent = signInClient(activity).signInIntent

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
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity)
    }

    fun onActivityResult(activity: AppCompatActivity, data: Intent?): DAuthUser? {
       var user: DAuthUser? = null
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            // Signed in successfully, show authenticated UI.
            val accountId = account?.id.toString()
            val accountIdToken = account?.idToken.toString()
            val scope = account?.grantedScopes
            DAuthLogger.e("account:$account, accountId:$accountId ,accountIdToken: $accountIdToken")
            user = DAuthUser(accountIdToken, accountId)

            val map = HashMap<String, String>()
            map.put("user_type", "30")
            map.put("id_token", accountIdToken)
            val sign = SignUtils.sign(map)
            val authorizeParam = AuthorizeToken2Param(
                access_token = null,
                refresh_token = null,
                30,
                sign,
                commonHeader = null,
                accountIdToken
            )
            activity.lifecycleScope.launch(Dispatchers.IO) {
                val data = AccountApi().authorizeExchangedToken(authorizeParam)
                withContext(Dispatchers.Main) {

                }
            }

        } catch (e: ApiException) {
            DAuthLogger.e(" google signInResult:failed code=" + e.statusCode)
        }
        return user
    }
}