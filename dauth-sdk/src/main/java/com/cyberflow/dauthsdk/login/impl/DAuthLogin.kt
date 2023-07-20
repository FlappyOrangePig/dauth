package com.cyberflow.dauthsdk.login.impl

import android.app.Activity
import android.app.Application
import android.content.Context
import com.cyberflow.dauthsdk.api.ILoginApi
import com.cyberflow.dauthsdk.api.SdkConfig
import com.cyberflow.dauthsdk.api.entity.ResponseCode
import com.cyberflow.dauthsdk.api.entity.LoginResultData
import com.cyberflow.dauthsdk.api.entity.SetPasswordData
import com.cyberflow.dauthsdk.login.callback.ThirdPartyCallback
import com.cyberflow.dauthsdk.login.callback.WalletCallback
import com.cyberflow.dauthsdk.login.constant.LoginConst.ACCOUNT_TYPE_OF_EMAIL
import com.cyberflow.dauthsdk.login.constant.LoginConst.ACCOUNT_TYPE_OF_OWN
import com.cyberflow.dauthsdk.login.constant.LoginConst.GOOGLE
import com.cyberflow.dauthsdk.login.constant.LoginConst.SIGN_METHOD
import com.cyberflow.dauthsdk.login.constant.LoginConst.TWITTER
import com.cyberflow.dauthsdk.login.model.*
import com.cyberflow.dauthsdk.login.network.BaseResponse
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.twitter.TwitterLoginManager
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.login.utils.JwtChallengeCode
import com.cyberflow.dauthsdk.login.utils.JwtDecoder
import com.cyberflow.dauthsdk.login.view.ThirdPartyResultActivity
import com.cyberflow.dauthsdk.login.view.WalletWebViewActivity
import com.cyberflow.dauthsdk.wallet.ext.runCatchingWithLogSuspend
import com.cyberflow.dauthsdk.wallet.impl.manager.Managers
import com.cyberflow.dauthsdk.wallet.impl.manager.WalletManager
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers

private const val FIX_TWITTER_JS_ISSUE = false
private const val TYPE_OF_WALLET_AUTH = 20
private const val USER_TYPE_OF_EMAIL = 10
private const val IS_REGISTER = 1       // 如果账号不存在则注册并登录
private const val AREA_CODE = "86"      // 手机区号
private const val VERIFY_CODE_LENGTH = 4

class DAuthLogin : ILoginApi {

    companion object {
        val instance: ILoginApi by lazy {
            DAuthLogin()
        }
    }

    private val prefs get() = Managers.loginPrefs
    private val walletManager get() = Managers.walletManager
    @Volatile
    private var logOutJob: Job? = null

    override fun initSDK(context: Context, config: SdkConfig) {
        DAuthLogger.i("DAuthLogin init sdk")
        // Twitter初始化
        TwitterLoginManager.instance.initTwitterSDK(context, config)
        if (FIX_TWITTER_JS_ISSUE) {
            val appContext = context.applicationContext as Application
            appContext.registerActivityLifecycleCallbacks(DAuthLifeCycle)
        }
    }


    override suspend fun login(account: String, passWord: String): LoginResultData? {
        val loginParam = LoginParam(
            ACCOUNT_TYPE_OF_EMAIL.toInt(),
            account = account,
            password = passWord,
            is_register = IS_REGISTER
        )
        return mobileOrEmailCommonReq(loginParam)
    }

    /**
     * @param codeChallengeCode sha256算法生成的一个code
     * @return 服务端返回临时code
     */
    private suspend fun loginAuth(codeChallengeCode: String, didToken: String): String {
        val body = AuthorizeParam(USER_TYPE_OF_EMAIL, codeChallengeCode, SIGN_METHOD)
        val authorizeParam = RequestApi().ownAuthorize(body, didToken)
        val code = authorizeParam?.data?.code.orEmpty()  //获取临时code
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
        val body = TokenAuthenticationParam(codeVerifier, code)
        return RequestApi().ownOauth2Token(body, didToken)
    }

    /**
     * @param type 第三方账号类型 GOOGLE TWITTER FACEBOOK
     * @param activity
     */

    override suspend fun loginWithType(
        type: String,
        activity: Activity
    ): LoginResultData? = suspendCancellableCoroutine {
        when (type) {
            GOOGLE -> {
                ThirdPartyResultActivity.launch(
                    ThirdPartyResultActivity.LAUNCH_TYPE_GOOGLE,
                    activity,
                    object : ThirdPartyCallback {
                        override fun onResult(loginResultData: LoginResultData?) {
                            it.resume(loginResultData, onCancellation = null)
                        }
                    })
            }
            TWITTER -> {
                ThirdPartyResultActivity.launch(
                    ThirdPartyResultActivity.LAUNCH_TYPE_TWITTER,
                    activity,
                    object : ThirdPartyCallback {
                        override fun onResult(loginResultData: LoginResultData?) {
                            it.resume(loginResultData, onCancellation = null)
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
        val code: Int?
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
        code = createAccountRes?.ret
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
    ): LoginResultData {
        val loginResultData: LoginResultData = if (type == ACCOUNT_TYPE_OF_EMAIL.toInt()) {
            val loginParam: LoginParam = if(verifyCode.length == VERIFY_CODE_LENGTH) {
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
        } else {
            val loginParam = LoginParam(
                type,
                phone = account,
                verify_code = verifyCode,
                phone_area_code = AREA_CODE,
                is_register = IS_REGISTER
            )
            mobileOrEmailCommonReq(loginParam)
        }

        return loginResultData
    }

    private suspend fun mobileOrEmailCommonReq(loginParam: LoginParam): LoginResultData {
        val loginRes = RequestApi().login(loginParam)
        if (loginRes?.ret == ResponseCode.RESPONSE_CORRECT_CODE) {
            val didToken = loginRes.data?.didToken.orEmpty()
            val userInfo = JwtDecoder().decoded(didToken)
            val codeVerifier = JwtChallengeCode().generateCodeVerifier()
            val codeChallenge = JwtChallengeCode().generateCodeChallenge(codeVerifier)
            DAuthLogger.d("codeVerify == $codeVerifier")

            val loginAuthCode = loginAuth(codeChallenge, didToken)
            val tokenAuthenticationRes = getDAuthToken(codeVerifier, loginAuthCode, didToken)
            val accessToken = tokenAuthenticationRes?.data?.access_token.orEmpty()
            val refreshToken = tokenAuthenticationRes?.data?.refresh_token
            val authIdToken = tokenAuthenticationRes?.data?.id_token.orEmpty()
            val authId = JwtDecoder().decoded(authIdToken).sub.orEmpty()
            val userId = userInfo.sub.orEmpty()
            val expireTime = tokenAuthenticationRes?.data?.expire_in
            val userType = loginParam.user_type
            // 邮箱登录后台需要的did_token为authIdToken
            prefs.putLoginInfo(accessToken, authId, userId, refreshToken, expireTime, userType, authIdToken)
            DAuthLogger.d("手机号/邮箱验证码登录accessToken：$accessToken,refreshToken:$refreshToken")

            val needCreateWallet = Managers.walletManager.getState() != WalletManager.STATE_OK
            return LoginResultData.Success(
                accessToken = accessToken,
                openId = authId,
                needCreateWallet = needCreateWallet
            )
        } else {
            // 其他错误
            return LoginResultData.Failure(loginRes?.ret)
        }
    }

    override fun logout() {
        val authid = prefs.getAuthId()
        val accessToken = prefs.getAccessToken()
        val requestBody = LogoutParam(authid, accessToken)
        logOutJob?.cancel()
        @OptIn(DelicateCoroutinesApi::class)
        logOutJob = GlobalScope.launch {
            runCatchingWithLogSuspend {
                RequestApi().logout(requestBody)
            }
        }
        clearAccountInfo()
    }

    internal fun clearAccountInfo() {
        prefs.clearLoginStateInfo()
        walletManager.clearData()
    }

    /**
     * 重置密码
     * @param resetPwdParams
     * @return SetPasswordData(code: Int?, msg: String?)
     */

    override suspend fun setRecoverPassword(resetPwdParams: ResetByPasswordParam): SetPasswordData {
        val response = RequestApi().resetByPassword(resetPwdParams)
        if(response?.ret == ResponseCode.RESPONSE_CORRECT_CODE) {
           return SetPasswordData(ResponseCode.RESPONSE_CORRECT_CODE,response.info)
        }
        return SetPasswordData(response?.ret, response?.info)
    }

    /**
     * @param phone 手机号
     * @param areaCode  区号
     */
    override suspend fun sendPhoneVerifyCode(phone: String, areaCode: String): Boolean {
        val body = SendPhoneVerifyCodeParam(openudid = null, phone, areaCode)
        val response = RequestApi().sendPhoneVerifyCode(body)
        if (response?.ret == ResponseCode.RESPONSE_CORRECT_CODE) {
            DAuthLogger.d("发送手机验证码成功")
            return true
        } else {
            DAuthLogger.e("发送手机验证码失败：${response?.info}")
        }
        return false
    }

    /**
     * @param email 邮箱
     */
    override suspend fun sendEmailVerifyCode(email: String): BaseResponse? {
        val body = SendEmailVerifyCodeParam(email)
        return RequestApi().sendEmailVerifyCode(body)
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
    override suspend fun bindEmail(email: String, verifyCode: String) : Boolean {
        val authId = prefs.getAuthId()
        val accessToken = prefs.getAccessToken()
        val body = BindEmailParam(authId, email, verifyCode, accessToken)
        val response = RequestApi().bindEmail(body)
        if(response?.ret == ResponseCode.RESPONSE_CORRECT_CODE) {
            return true
        }
        DAuthLogger.e("绑定邮箱失败 errorCode == ${response?.ret}")
        return false
    }

    /**
     * EOA钱包授权登录
     */
    override suspend fun link2EOAWallet(context: Context): LoginResultData? = suspendCancellableCoroutine {
            continuation ->
        val callback = object : WalletCallback {
            override fun onResult(walletInfo: String) {
                val body = AuthorizeToken2Param(
                    user_type = TYPE_OF_WALLET_AUTH,
                    user_data = walletInfo
                )
                CoroutineScope(Dispatchers.IO).launch {
                    val loginResultData = ThirdPlatformLogin.instance.thirdPlatFormLogin(body)
                    continuation.resume(loginResultData, null)
                }
            }
        }
        WalletWebViewActivity.launch(context, false, callback)
    }

    /**
     * @param passWord 密码
     * 设置密码
     */
    override suspend fun setPassword(passwordParam: SetPasswordParam): Int? {
        val didToken = prefs.getDidToken()
        DAuthLogger.e("设置密码时传的didToken:"+didToken)
        return RequestApi().setPassword(passwordParam, didToken)?.ret
    }

    /**
     * @param email
     * @return accountRes
     * 根据邮箱查询用户信息
     */
    override suspend fun queryAccountByEmail(email: String): AccountRes? {
        val authId = prefs.getAuthId()
        val requestApi = RequestApi()
        val accessToken = prefs.getAccessToken()
        DAuthLogger.d("queryAccountByEmail accessToken:$accessToken")
        val body = QueryByEMailParam(email, accessToken, authId)
        return requestApi.queryByEMail(body)
    }

    /**
     * @return accountRes
     * 根据邮箱查询用户信息
     */
    override suspend fun queryAccountByAuthid(): AccountRes? {
        val authId = prefs.getAuthId()
        val accessToken = prefs.getAccessToken()
        val body = QueryByAuthIdParam(authId, accessToken)
        return RequestApi().queryByAuthId(body)
    }
}