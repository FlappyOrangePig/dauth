package com.cyberflow.dauthsdk.login.google

import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import com.cyberflow.dauthsdk.login.DAuthSDK
import com.cyberflow.dauthsdk.login.model.*
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.utils.*
import com.cyberflow.dauthsdk.wallet.api.IWalletApi
import com.cyberflow.dauthsdk.wallet.impl.WalletHolder
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


private const val REQUEST_CODE = 9001
private const val TAG = "GoogleLoginManager"
private const val AUTH_TYPE_OF_GOOGLE = "30"
private const val USER_TYPE = "user_type"
class GoogleLoginManager : IWalletApi by WalletHolder.walletApi {
    private val context get() = (DAuthSDK.instance).context
    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            GoogleLoginManager()
        }
    }

    //Google sdk init
    private fun signInClient(activity: Activity): GoogleSignInClient {
        var serverClientId = ""
        try {
            val applicationInfo = activity.packageManager.getApplicationInfo(activity.packageName,
                PackageManager.GET_META_DATA)
            val metaData = applicationInfo.metaData
            serverClientId = metaData.getString("com.google.android.gms.games.APP_ID").orEmpty()
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
        }
        //requestIdToken需要使用Web客户端ID才能成功，不要使用安卓ClientID
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(serverClientId)
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

      suspend fun onActivityResult(data: Intent?): Int {
        var loginResCode = 10000
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            // Signed in successfully, show authenticated UI.
            val accountId = account?.id.toString()
            val accountIdToken = account?.idToken.toString()
            DAuthLogger.e("account:$account, accountId:$accountId ,accountIdToken: $accountIdToken")

            val authorizeParam = AuthorizeToken2Param(
                access_token = null,
                refresh_token = null,
                AUTH_TYPE_OF_GOOGLE,
                commonHeader = null,
                id_token = accountIdToken
            )
            withContext(Dispatchers.IO) {
                val authExchangedTokenRes = RequestApi().authorizeExchangedToken(authorizeParam)
                if (authExchangedTokenRes?.iRet == 0) {
                    val didToken = authExchangedTokenRes.data?.did_token.orEmpty()
                    val googleUserInfo = JwtDecoder().decoded(didToken)
                    val accessToken = authExchangedTokenRes.data?.d_access_token.orEmpty()
                    val authId = googleUserInfo.sub.orEmpty()
                    val queryWalletRes = RequestApi().queryWallet(accessToken, authId)
                    LoginPrefs(context).setAccessToken(accessToken)
                    LoginPrefs(context).setAuthID(authId)
                    //没有钱包  返回errorCode
                    if (queryWalletRes?.data?.address.isNullOrEmpty()) {
                        loginResCode = 10001
                    } else {
                        // 该邮箱绑定过钱包
                        loginResCode = 0
                        DAuthLogger.d("该google账号已绑定钱包，直接进入主页")
                    }
                } else {
                    loginResCode = 100001
                    DAuthLogger.e("app第三方认证登录失败 errCode == $loginResCode")
                }
            }

        } catch (e: ApiException) {
            DAuthLogger.e(" google signInResult:failed code=" + e.statusCode)
        }
        return loginResCode
    }
}