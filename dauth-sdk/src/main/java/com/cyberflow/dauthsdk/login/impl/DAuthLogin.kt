package com.cyberflow.dauthsdk.login.impl

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import com.cyberflow.dauthsdk.DAuthSDK
import com.cyberflow.dauthsdk.login.api.ILoginApi
import com.cyberflow.dauthsdk.login.api.bean.SdkConfig
import com.cyberflow.dauthsdk.login.callback.ResetPwdCallback
import com.cyberflow.dauthsdk.login.const.LoginConst.ACCOUNT
import com.cyberflow.dauthsdk.login.const.LoginConst.ACCOUNT_TYPE_OF_EMAIL
import com.cyberflow.dauthsdk.login.const.LoginConst.ACCOUNT_TYPE_OF_OWN
import com.cyberflow.dauthsdk.login.const.LoginConst.CODE_CHALLENGE
import com.cyberflow.dauthsdk.login.const.LoginConst.CODE_CHALLENGE_METHOD
import com.cyberflow.dauthsdk.login.const.LoginConst.CONFIRM_PASSWORD
import com.cyberflow.dauthsdk.login.const.LoginConst.IS_LOGIN
import com.cyberflow.dauthsdk.login.const.LoginConst.OPEN_UID
import com.cyberflow.dauthsdk.login.const.LoginConst.PASSWORD
import com.cyberflow.dauthsdk.login.const.LoginConst.PHONE
import com.cyberflow.dauthsdk.login.const.LoginConst.PHONE_AREA_CODE
import com.cyberflow.dauthsdk.login.const.LoginConst.SEX
import com.cyberflow.dauthsdk.login.const.LoginConst.SIGN_METHOD
import com.cyberflow.dauthsdk.login.const.LoginConst.USER_TYPE
import com.cyberflow.dauthsdk.login.const.LoginConst.UUID
import com.cyberflow.dauthsdk.login.const.LoginConst.VERIFY_CODE
import com.cyberflow.dauthsdk.login.constant.LoginType
import com.cyberflow.dauthsdk.login.model.*
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.twitter.TwitterLoginManager
import com.cyberflow.dauthsdk.login.utils.*
import com.cyberflow.dauthsdk.login.view.ThirdPartyResultActivity
import com.cyberflow.dauthsdk.wallet.api.IWalletApi
import com.cyberflow.dauthsdk.wallet.impl.WalletHolder
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.*


private const val TWITTER_REQUEST_CODE = 140
private const val GOOGLE_REQUEST_CODE = 9001
private const val AUTH_TYPE_OF_GOOGLE = "30"

class DAuthLogin : ILoginApi, IWalletApi by WalletHolder.walletApi {

    val context get() = (DAuthSDK.instance).context
    private var _context: Context? = null
    private var _config: SdkConfig? = null

    companion object {
        val instance by lazy {
            DAuthLogin()
        }
    }

    override fun initSDK(context: Context, config: SdkConfig) {
        val appContext = context.applicationContext as Application
        this._context = appContext
        this._config = config
        //Twitter初始化
        TwitterLoginManager.instance.initTwitterSDK(context, config)
        initWallet(appContext)
        appContext.registerActivityLifecycleCallbacks(DAuthLifeCycle)
        DAuthLogger.i("init sdk")
    }


    override suspend fun login(account: String, passWord: String): Int? {
        val loginParam = LoginParam(
            ACCOUNT_TYPE_OF_OWN.toInt(),
            account = account,
            password = passWord,
        )

        val loginRes = RequestApi().login(loginParam)

        if (loginRes?.iRet == 0) {
            val didToken = loginRes.data.did_token
            //DAuth 授权接口测试获取临时code
            val codeVerifier = JwtChallengeCode().generateCodeVerifier()
            val codeChallenge = JwtChallengeCode().generateCodeChallenge(codeVerifier)
            DAuthLogger.d("codeVerify == $codeVerifier")
            val code = withContext(Dispatchers.IO) {
                loginAuth(codeChallenge, didToken.orEmpty())
            }
            //DAuth 授权接口测试获取token
            val tokenAuthenticationRes = getDAuthToken(codeVerifier, code, didToken)
            DAuthLogger.e("sdk授权登录返回：$tokenAuthenticationRes.")
            DAuthLogger.e("登录成功 didToken == $didToken,loginRes== ${loginRes.data}")
        } else {
            DAuthLogger.e("登录失败：${loginRes?.sMsg}")
        }
        return loginRes?.iRet
    }

    /**
     * @param codeChallengeCode sha256算法生成的一个code
     * @return 服务端返回临时code
     */
    private suspend fun loginAuth(codeChallengeCode: String, didToken: String): String {
        val map = HashMap<String, String>()
        var code = ""
        map[USER_TYPE] = ACCOUNT_TYPE_OF_EMAIL
        map[CODE_CHALLENGE] = codeChallengeCode
        map[CODE_CHALLENGE_METHOD] = SIGN_METHOD
        val sign = SignUtils.sign(map)

        val body = AuthorizeParam(10, codeChallengeCode, SIGN_METHOD, sign)
        val authorizeParam = withContext(Dispatchers.IO) {
            RequestApi().ownAuthorize(body, didToken)
        }

        code = authorizeParam?.data?.code.orEmpty()  //获取临时code
        DAuthLogger.e("ownAuthorize 临时code： $code ")

        return code
    }

    /**
     * 自有账号授权登录获取token
     */

    private fun getDAuthToken(
        codeVerifier: String,
        code: String,
        didToken: String?
    ): TokenAuthenticationRes? {
        var tokenAuthenticationRes: TokenAuthenticationRes? = null
        runBlocking {
            val body = TokenAuthenticationParam(codeVerifier, code)
            tokenAuthenticationRes = RequestApi().ownOauth2Token(body, didToken)
        }
        return tokenAuthenticationRes
    }

    /**
     * @param type 第三方账号类型 GOOGLE TWITTER FACEBOOK
     * @param activity
     */

    override suspend fun loginWithType(type: String, activity: Activity) {
        when (type) {
            LoginType.GOOGLE -> {
                val intent = Intent(activity, ThirdPartyResultActivity::class.java)
                intent.putExtra("type", 0)
                activity.startActivityForResult(intent, GOOGLE_REQUEST_CODE)
            }
            LoginType.TWITTER -> {
                val intent = Intent(activity, ThirdPartyResultActivity::class.java)
                intent.putExtra("type", 1)
                activity.startActivityForResult(intent, TWITTER_REQUEST_CODE)
            }
        }
    }

    /**
     * @param account 自有账号（字母和数字组合）
     * @param passWord 密码
     * @param confirmPwd 确认密码
     */

    override fun createDAuthAccount(
        account: String,
        passWord: String,
        confirmPwd: String
    ): Boolean {
        var isSuccess = false
        val map = HashMap<String, String>()
        val userType = ACCOUNT_TYPE_OF_OWN
        map[ACCOUNT] = account
        map[USER_TYPE] = userType
        map[UUID] = "123456"
        map[IS_LOGIN] = "1"
        map[SEX] = "0"
        map[PASSWORD] = passWord
        map[CONFIRM_PASSWORD] = confirmPwd
        val sign = SignUtils.sign(map)
        val createAccountParam = CreateAccountParam(
            userType, "123456", sign, 1,
            passWord, confirm_password = confirmPwd, sex = 0, account = account
        )
        val feature = ThreadPoolUtils.submit {
            val createAccountRes = RequestApi().createAccount(createAccountParam)
            if (createAccountRes?.iRet == 0) {
                isSuccess = true
            }

        }
        val f = feature.get()
        return isSuccess
    }

    /**
     * @param account 手机号或邮箱
     * @param verifyCode 验证码
     * @param type  10(邮箱) 60(手机)
     */
    override suspend fun loginByMobileOrEmail(
        account: String,
        verifyCode: String,
        type: Int
    ): Int? {
        var loginResCode: Int? = -1
        val map = HashMap<String, String>()
        map[USER_TYPE] = type.toString()
        map[VERIFY_CODE] = verifyCode
        if (type == ACCOUNT_TYPE_OF_EMAIL.toInt()) {
            map[ACCOUNT] = account
            val sign = SignUtils.sign(map)
            val loginParam = LoginParam(type, sign, account = account, verify_code = verifyCode)
            withContext(Dispatchers.IO) {
                val loginRes = RequestApi().login(loginParam)
                if (loginRes?.iRet == 0) {
                    val data = loginRes.data
                    val userInfo = JwtDecoder().decoded(data.did_token.orEmpty())

                    val didToken = loginRes.data.did_token
                    val codeVerifier = JwtChallengeCode().generateCodeVerifier()
                    val codeChallenge = JwtChallengeCode().generateCodeChallenge(codeVerifier)
                    DAuthLogger.d("codeVerify == $codeVerifier")

                    val loginCode = loginAuth(codeChallenge, didToken.orEmpty())
                    val tokenAuthenticationRes = getDAuthToken(codeVerifier, loginCode, didToken)
                    val accessToken = tokenAuthenticationRes?.data?.access_token.orEmpty()
                    val authIdToken = tokenAuthenticationRes?.data?.id_token.orEmpty()
                    val userId = JwtDecoder().decoded(authIdToken).sub.orEmpty()
                    val authId = userInfo.sub.orEmpty()     //查询用户信息时用
                    LoginPrefs(context).setAccessToken(accessToken)
                    LoginPrefs(context).setAuthID(userId)
                    val queryWalletRes = withContext(Dispatchers.IO) {
                        RequestApi().queryWallet(accessToken, userId)
                    }

                    if (queryWalletRes?.data?.address.isNullOrEmpty()) {
                        // 该邮箱没有钱包
                        loginResCode = 10001
                    } else {
                        // 该邮箱绑定过钱包
                        loginResCode = 0
                        DAuthLogger.e("该邮箱绑定过钱包直接进入主页")
                    }

                } else {
                    //1000032  验证码已失效
                    loginResCode = loginRes?.iRet
                }
            }
        } else {    //TODO 手机号登录

        }

        return loginResCode
    }



    override fun logout(openUid: String) {
        val map = HashMap<String, String>()
        map[OPEN_UID] = openUid
        val sign = SignUtils.sign(map)
        val requestBody = LogoutParam(openUid, sign)
        ThreadPoolUtils.execute {
            RequestApi().logout(requestBody)
        }
    }

    /**
     * 重置密码
     */

    override fun setRecoverPassword(callback: ResetPwdCallback) {

    }

    /**
     * @param phone 手机号
     * @param areaCode  区号
     */
    override fun sendPhoneVerifyCode(phone: String, areaCode: String) {
        val map = HashMap<String, String>()
        map[PHONE] = phone
        map[PHONE_AREA_CODE] = areaCode
        val sign = SignUtils.sign(map)
        val body = SendPhoneVerifyCodeParam(openudid = null, phone, areaCode, sign)
        ThreadPoolUtils.execute {
            RequestApi().sendPhoneVerifyCode(body)
        }
    }

    /**
     * @param email 邮箱
     */
    override suspend fun sendEmailVerifyCode(email: String): Boolean {
        var isSend = false
        val map = HashMap<String, String>()
        map[ACCOUNT] = email
        val body = SendEmailVerifyCodeParam(email)

        val response = RequestApi().sendEmailVerifyCode(body)
        if (response?.iRet == 0) {
            isSend = true
            DAuthLogger.d("发送邮箱验证码成功")
        } else {
            DAuthLogger.e("发送邮箱验证码失败：${response?.sMsg}")
        }

        return isSend

    }

    /**
     * @param bindParams 对象
     *  包含 openudid(用户id)
     *  phone(手机号)
     *  phone_area_code(区号)
     *  verify_code(验证码)
     */
    override fun bindPhone(bindParams: BindPhoneParam) {
        ThreadPoolUtils.execute {
            RequestApi().bindPhone(bindParams)
        }
    }

    /**
     * @param email 邮箱
     * @param verifyCode 邮箱验证码
     */
    override fun bindEmail(email: String, verifyCode: String) {

    }

    override suspend fun thirdPartyCallback(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Int? {

        return withContext(Dispatchers.IO) {
            var code: Int? = -1
            when (requestCode) {

                GOOGLE_REQUEST_CODE -> {
                    code = googleAuthLogin(data)
                }

                TWITTER_REQUEST_CODE -> {
                    code = TwitterLoginManager.instance.twitterAuthLogin()
                }
            }
            code
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

    private suspend fun  googleAuthLogin(data: Intent?) : Int {
        val accountIdToken = getGoogleIdToken(data)
        var loginResCode: Int
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
        return loginResCode
    }



}