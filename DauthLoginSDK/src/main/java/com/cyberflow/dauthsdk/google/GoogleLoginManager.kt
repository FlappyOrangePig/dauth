package com.cyberflow.dauthsdk.google

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.cyberflow.dauthsdk.callback.BaseHttpCallback
import com.cyberflow.dauthsdk.login.DAuthUser
import com.cyberflow.dauthsdk.model.AuthorizeToken2Param
import com.cyberflow.dauthsdk.network.BaseResponse
import com.cyberflow.dauthsdk.network.RequestApi
import com.cyberflow.dauthsdk.utils.DAuthLogger
import com.cyberflow.dauthsdk.utils.SignUtils
import com.cyberflow.dauthsdk.utils.ThreadPoolUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.gson.Gson

private const val REQUEST_CODE = 9001
private const val TAG = "GoogleLoginManager"
private const val TEST_SERVER_CLIENT_ID = "535517245452-2qnvgf1sp5vgb9ra6ouelgnedllj4iqs.apps.googleusercontent.com"
private const val AUTH_TYPE_OF_GOOGLE = "30"
private const val USER_TYPE = "user_type"
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
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity)

        return status
    }

    fun onActivityResult(data: Intent?): DAuthUser? {
       var user: DAuthUser? = null
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            // Signed in successfully, show authenticated UI.
            val accountId = account?.id.toString()
            val accountIdToken = account?.idToken.toString()
//            val data = JwtDecoder().decodeAndVerify(accountIdToken)
            DAuthLogger.e("account:$account, accountId:$accountId ,accountIdToken: $accountIdToken")
            user = DAuthUser(accountIdToken, accountId)

            val map = HashMap<String, String>()
            map[USER_TYPE] = AUTH_TYPE_OF_GOOGLE
            map["id_token"] = accountIdToken
            val sign = SignUtils.sign(map)
            val authorizeParam = AuthorizeToken2Param(
                access_token = null,
                refresh_token = null,
                AUTH_TYPE_OF_GOOGLE,
                sign,
                commonHeader = null,
                accountIdToken
            )
            ThreadPoolUtils.execute {
                val response = RequestApi().authorizeExchangedToken(authorizeParam)
                if(response?.iRet == 0) {
                    DAuthLogger.d("GoogleLogin success : ${response.data}")
                } else {
                    DAuthLogger.e("GoogleLogin failed : ${response?.data}")
                }
            }

        } catch (e: ApiException) {
            DAuthLogger.e(" google signInResult:failed code=" + e.statusCode)
        }
        return user
    }
}