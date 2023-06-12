package com.cyberflow.dauthsdk.login.impl

import android.app.Activity
import android.app.Application
import android.content.Context
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.ILoginApi
import com.cyberflow.dauthsdk.api.IWalletApi
import com.cyberflow.dauthsdk.api.SdkConfig
import com.cyberflow.dauthsdk.login.callback.ResetPwdCallback
import com.cyberflow.dauthsdk.login.constant.LoginConst.ACCOUNT
import com.cyberflow.dauthsdk.login.constant.LoginConst.ACCOUNT_TYPE_OF_EMAIL
import com.cyberflow.dauthsdk.login.constant.LoginConst.ACCOUNT_TYPE_OF_OWN
import com.cyberflow.dauthsdk.login.constant.LoginConst.CODE_CHALLENGE
import com.cyberflow.dauthsdk.login.constant.LoginConst.CODE_CHALLENGE_METHOD
import com.cyberflow.dauthsdk.login.constant.LoginConst.GOOGLE
import com.cyberflow.dauthsdk.login.constant.LoginConst.OPEN_UID
import com.cyberflow.dauthsdk.login.constant.LoginConst.PHONE
import com.cyberflow.dauthsdk.login.constant.LoginConst.PHONE_AREA_CODE
import com.cyberflow.dauthsdk.login.constant.LoginConst.SIGN_METHOD
import com.cyberflow.dauthsdk.login.constant.LoginConst.TWITTER
import com.cyberflow.dauthsdk.login.constant.LoginConst.USER_TYPE
import com.cyberflow.dauthsdk.login.constant.LoginConst.VERIFY_CODE
import com.cyberflow.dauthsdk.login.model.AuthorizeParam
import com.cyberflow.dauthsdk.login.model.BindPhoneParam
import com.cyberflow.dauthsdk.login.model.CreateAccountParam
import com.cyberflow.dauthsdk.login.model.LoginParam
import com.cyberflow.dauthsdk.login.model.LogoutParam
import com.cyberflow.dauthsdk.login.model.SendEmailVerifyCodeParam
import com.cyberflow.dauthsdk.login.model.SendPhoneVerifyCodeParam
import com.cyberflow.dauthsdk.login.model.TokenAuthenticationParam
import com.cyberflow.dauthsdk.login.model.TokenAuthenticationRes
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.twitter.TwitterLoginManager
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.login.utils.JwtChallengeCode
import com.cyberflow.dauthsdk.login.utils.JwtDecoder
import com.cyberflow.dauthsdk.login.utils.LoginPrefs
import com.cyberflow.dauthsdk.login.utils.SignUtils
import com.cyberflow.dauthsdk.login.utils.ThreadPoolUtils
import com.cyberflow.dauthsdk.login.view.ThirdPartyResultActivity
import com.cyberflow.dauthsdk.login.view.WalletWebViewActivity
import com.cyberflow.dauthsdk.wallet.impl.WalletHolder
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.TwitterSession
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

private const val GOOGLE_REQUEST_CODE = 9004
private const val TWITTER_REQUEST_CODE = 140
class DAuthLogin : ILoginApi, IWalletApi by WalletHolder.walletApi {
private const val FIX_TWITTER_JS_ISSUE  = false

class DAuthLogin : ILoginApi {

    private val context get() = DAuthSDK.impl.context

    companion object {
        val instance: ILoginApi by lazy {
            DAuthLogin()
        }
    }

    override fun initSDK(context: Context, config: SdkConfig) {
        DAuthLogger.i("DAuthLogin init sdk")
        // Twitter初始化
        TwitterLoginManager.instance.initTwitterSDK(context, config)
        if (FIX_TWITTER_JS_ISSUE) {
            val appContext = context.applicationContext as Application
            appContext.registerActivityLifecycleCallbacks(DAuthLifeCycle)
        }
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

    override suspend fun loginWithType(
        type: String,
        activity: Activity
    ): Int? = suspendCancellableCoroutine {
        when (type) {
            GOOGLE -> {
                ThirdPartyResultActivity.launch(
                    GOOGLE_REQUEST_CODE,
                    activity,
                    object : ThirdPartyCallback {
                        override fun onResult(code: Int?) {
                            DAuthLogger.d("google 授权登录code：$code")
                            it.resume(code, onCancellation = null)
                        }
                    })
            }
            TWITTER -> {
                ThirdPartyResultActivity.launch(
                    TWITTER_REQUEST_CODE,
                    activity,
                    object : ThirdPartyCallback {
                        override fun onResult(code: Int?) {
                            DAuthLogger.d("twitter 授权登录code：$code")
                            it.resume(code, onCancellation = null)
                        }
                    })
            }
            else -> {
                it.resume(null, onCancellation = null) // Handle unsupported type or other cases
            }
        }
    }

    /**
     * @param account 自有账号（字母和数字组合）
     * @param passWord 密码
     * @param confirmPwd 确认密码
     */

    override suspend fun createDAuthAccount(
        account: String,
        passWord: String,
        confirmPwd: String
    ): Int? {
        var code: Int?
        withContext(Dispatchers.IO) {
            val createAccountParam = CreateAccountParam(
                ACCOUNT_TYPE_OF_OWN,
                "123456",
                is_login = 1,
                password = passWord,
                confirm_password = confirmPwd,
                sex = 0,
                account = account
            )

            val createAccountRes = RequestApi().createAccount(createAccountParam)
            code = createAccountRes?.iRet
        }
        return code
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

    override fun link2EOAWallet(context: Context) : Int? {
        WalletWebViewActivity.launch(context, false)
        return 0
    }

}