package com.cyberflow.dauthsdk.login.impl

import android.app.Activity
import android.app.Application
import android.content.Context
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.ILoginApi
import com.cyberflow.dauthsdk.api.SdkConfig
import com.cyberflow.dauthsdk.login.callback.ThirdPartyCallback
import com.cyberflow.dauthsdk.login.callback.WalletCallback
import com.cyberflow.dauthsdk.login.constant.LoginConst.ACCOUNT
import com.cyberflow.dauthsdk.login.constant.LoginConst.ACCOUNT_TYPE_OF_EMAIL
import com.cyberflow.dauthsdk.login.constant.LoginConst.ACCOUNT_TYPE_OF_OWN
import com.cyberflow.dauthsdk.login.constant.LoginConst.CODE_CHALLENGE
import com.cyberflow.dauthsdk.login.constant.LoginConst.CODE_CHALLENGE_METHOD
import com.cyberflow.dauthsdk.login.constant.LoginConst.GOOGLE
import com.cyberflow.dauthsdk.login.constant.LoginConst.OPEN_UID
import com.cyberflow.dauthsdk.login.constant.LoginConst.SIGN_METHOD
import com.cyberflow.dauthsdk.login.constant.LoginConst.TWITTER
import com.cyberflow.dauthsdk.login.constant.LoginConst.USER_TYPE
import com.cyberflow.dauthsdk.login.model.*
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.twitter.TwitterLoginManager
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.login.utils.JwtChallengeCode
import com.cyberflow.dauthsdk.login.utils.JwtDecoder
import com.cyberflow.dauthsdk.login.utils.LoginPrefs
import com.cyberflow.dauthsdk.login.utils.SignUtils
import com.cyberflow.dauthsdk.login.view.ThirdPartyResultActivity
import com.cyberflow.dauthsdk.login.view.WalletWebViewActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val GOOGLE_REQUEST_CODE = 9004
private const val TWITTER_REQUEST_CODE = 140
private const val FIX_TWITTER_JS_ISSUE = false
private const val TYPE_OF_WALLET_AUTH = "20"
private const val IS_REGISTER = 1       //如果账号不存在则注册并登录
private const val AREA_CODE = "86"      //手机区号
private const val VERIFY_CODE_LENGTH = 4
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

    private suspend fun getDAuthToken(
        codeVerifier: String,
        code: String,
        didToken: String?
    ): TokenAuthenticationRes? {
        var tokenAuthenticationRes: TokenAuthenticationRes? = null
        val body = TokenAuthenticationParam(codeVerifier, code)
        tokenAuthenticationRes = RequestApi().ownOauth2Token(body, didToken)
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
        loginResCode = if (type == ACCOUNT_TYPE_OF_EMAIL.toInt()) {
            var loginParam : LoginParam? = null
            loginParam = if(verifyCode.length == VERIFY_CODE_LENGTH) {
                LoginParam(
                    type,
                    account = account,
                    verify_code = verifyCode,
                    is_register = IS_REGISTER
                )
            } else {
                LoginParam(
                    type,
                    account = account,
                    password = verifyCode,
                    is_register = IS_REGISTER
                )
            }
            mobileOrEmailCommonReq(loginParam)
        } else {    //TODO 手机号登录
            val loginParam = LoginParam(
                type,
                phone = account,
                verify_code = verifyCode,
                phone_area_code = AREA_CODE
            )
            mobileOrEmailCommonReq(loginParam)
        }

        return loginResCode
    }

    private suspend fun mobileOrEmailCommonReq(loginParam: LoginParam?): Int? {
        var loginCode: Int? = null
        withContext(Dispatchers.IO) {
            val loginRes = RequestApi().login(loginParam)
            if (loginRes?.iRet == 0) {
                val data = loginRes.data
                val userInfo = JwtDecoder().decoded(data.did_token.orEmpty())

                val didToken = loginRes.data.did_token
                LoginPrefs(context).setDidToken(didToken.orEmpty())
                val codeVerifier = JwtChallengeCode().generateCodeVerifier()
                val codeChallenge = JwtChallengeCode().generateCodeChallenge(codeVerifier)
                DAuthLogger.d("codeVerify == $codeVerifier")

                val loginAuthCode = loginAuth(codeChallenge, didToken.orEmpty())
                val tokenAuthenticationRes =
                    getDAuthToken(codeVerifier, loginAuthCode, didToken)
                val accessToken = tokenAuthenticationRes?.data?.access_token.orEmpty()
                val expireTime = tokenAuthenticationRes?.data?.expire_in
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
                    loginCode = 200001
                } else {
                    // 该邮箱绑定过钱包
                    loginCode = 0
                    DAuthLogger.e("该邮箱绑定过钱包直接进入主页")
                }

            } else {
                //1000032  验证码已失效
                loginCode = loginRes?.iRet
            }
        }
        return loginCode
    }


    override fun logout(openUid: String) {
        val map = HashMap<String, String>()
        map[OPEN_UID] = openUid
        val sign = SignUtils.sign(map)
        val requestBody = LogoutParam(openUid, sign)
        if(RequestApi().logout(requestBody)) {
            LoginPrefs(context).clearLoginStateInfo()
        }
    }

    /**
     * 重置密码
     */

    override suspend fun setRecoverPassword(params: ResetByPasswordParam): Boolean {
        val response = RequestApi().resetByPassword(params)
        if(response?.iRet == 0) {
           return true
        }
        return false
    }

    /**
     * @param phone 手机号
     * @param areaCode  区号
     */
    override suspend fun sendPhoneVerifyCode(phone: String, areaCode: String): Boolean {
        var isSend = false

        val body = SendPhoneVerifyCodeParam(openudid = null, phone, areaCode)
        val response = RequestApi().sendPhoneVerifyCode(body)
        if (response?.iRet == 0) {
            isSend = true
            DAuthLogger.d("发送手机验证码成功")
        } else {
            DAuthLogger.e("发送手机验证码失败：${response?.sMsg}")
        }
        return isSend
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
    override suspend fun bindPhone(bindParams: BindPhoneParam) {
        RequestApi().bindPhone(bindParams)
    }

    /**
     * @param email 邮箱
     * @param verifyCode 邮箱验证码
     */
    override suspend fun bindEmail(email: String, verifyCode: String) {

    }

    /**
     * EOA钱包授权登录
     */
    override suspend fun link2EOAWallet(context: Context): Int = suspendCancellableCoroutine { continuation ->
        val callback = object : WalletCallback {
            override fun onResult(walletInfo: String) {
                val body = AuthorizeToken2Param(
                    user_type = TYPE_OF_WALLET_AUTH,
                    user_data = walletInfo
                )
                val code = ThirdPlatformLogin.instance.thirdPlatFormLogin(body)
                continuation.resume(code, onCancellation = null)
            }
        }
        WalletWebViewActivity.launch(context, false, callback)
    }

    /**
     * @param passWord 密码
     * 设置密码
     */
    override suspend fun setPassword(passWord: String): Int? {
        val didToken = LoginPrefs(context).getDidToken()
        val setPasswordParam = SetPasswordParam()
        setPasswordParam.password = passWord
        return RequestApi().setPassword(setPasswordParam, didToken)?.iRet
    }

    /**
     * @param email
     * @return accountRes
     * 根据邮箱查询用户信息
     */
    override suspend fun queryAccountByEmail(email: String): AccountRes? {
        val authId = LoginPrefs(context).getAuthId()
        val accessToken = LoginPrefs(context).getAccessToken()
        val body = QueryByEMailParam(email, authId, accessToken)
        return RequestApi().queryByEMail(body)
    }

}