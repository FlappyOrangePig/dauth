package com.infras.dauthsdk.login.impl

import android.app.Activity
import android.app.Application
import android.content.Context
import com.infras.dauthsdk.api.ILoginApi
import com.infras.dauthsdk.api.SdkConfig
import com.infras.dauthsdk.api.annotation.DAuthAccountType
import com.infras.dauthsdk.api.annotation.SignType3rd
import com.infras.dauthsdk.api.entity.LoginResultData
import com.infras.dauthsdk.api.entity.ResponseCode
import com.infras.dauthsdk.api.entity.SetPasswordData
import com.infras.dauthsdk.login.callback.ThirdPartyCallback
import com.infras.dauthsdk.login.model.*
import com.infras.dauthsdk.login.network.BaseResponse
import com.infras.dauthsdk.login.twitter.TwitterLoginManager
import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.login.utils.JwtChallengeCode
import com.infras.dauthsdk.login.utils.JwtDecoder
import com.infras.dauthsdk.login.utils.maskSensitiveData
import com.infras.dauthsdk.login.view.ThirdPartyResultActivity
import com.infras.dauthsdk.wallet.ext.runCatchingWithLogSuspend
import com.infras.dauthsdk.wallet.impl.manager.Managers
import com.infras.dauthsdk.wallet.impl.manager.WalletManager
import kotlinx.coroutines.*

private const val TYPE_OF_WALLET_AUTH = 20
private const val USER_TYPE_OF_EMAIL = 10
private const val IS_REGISTER = 1       // 如果账号不存在则注册并登录
private const val AREA_CODE = "86"      // 手机区号
private const val VERIFY_CODE_LENGTH = 4

internal class DAuthLogin internal constructor() : ILoginApi {

    private val prefs get() = Managers.loginPrefs
    private val requestApi get() = Managers.requestApi
    private val deviceId get() = Managers.deviceId
    @Volatile
    private var logOutJob: Job? = null

    override fun initSDK(context: Context, config: SdkConfig) {
        DAuthLogger.i("DAuthLogin init sdk")
        // Twitter初始化
        TwitterLoginManager.instance.initTwitterSDK(context, config)
        val appContext = context.applicationContext as Application
        appContext.registerActivityLifecycleCallbacks(DAuthLifeCycle)
    }

    override suspend fun login(account: String, passWord: String): LoginResultData? {
        val loginParam = LoginParam(
            DAuthAccountType.ACCOUNT_TYPE_OF_EMAIL,
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
        val signMethod = "SHA-256"
        val body = AuthorizeParam(USER_TYPE_OF_EMAIL, codeChallengeCode, signMethod)
        val authorizeParam = requestApi.ownAuthorize(body, didToken)
        val code = authorizeParam?.data?.code.orEmpty()  //获取临时code
        DAuthLogger.i("ownAuthorize temp code=$code")
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
        return requestApi.ownOauth2Token(body, didToken)
    }

    override suspend fun loginWithType(
        @SignType3rd type: String,
        activity: Activity
    ): LoginResultData? = suspendCancellableCoroutine {
        when (type) {
            SignType3rd.GOOGLE -> {
                ThirdPartyResultActivity.launch(
                    ThirdPartyResultActivity.LAUNCH_TYPE_GOOGLE,
                    activity,
                    object : ThirdPartyCallback {
                        override fun onResult(loginResultData: LoginResultData?) {
                            it.resume(loginResultData, onCancellation = null)
                        }
                    })
            }
            SignType3rd.TWITTER -> {
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
     * @param password 密码
     * @param confirmPwd 确认密码
     */
    override suspend fun createDAuthAccount(
        account: String,
        password: String,
        confirmPwd: String
    ): Int? {
        val code: Int?
        val createAccountParam = CreateAccountParam(
            DAuthAccountType.ACCOUNT_TYPE_OF_OWN,
            this.deviceId,
            is_login = 1,
            password = password,
            confirm_password = confirmPwd,
            sex = 0,
            account = account
        )

        val createAccountRes = requestApi.createAccount(createAccountParam)
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
        val loginResultData: LoginResultData = if (type == DAuthAccountType.ACCOUNT_TYPE_OF_EMAIL) {
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
        val loginRes = requestApi.login(loginParam)
        if (loginRes?.isSuccess() == true) {
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
            val authId = JwtDecoder().decoded(authIdToken).sub
            val userId = userInfo.sub
            val expireTime = tokenAuthenticationRes?.data?.expire_in
            val userType = loginParam.user_type
            // 邮箱登录后台需要的did_token为authIdToken
            prefs.putLoginInfo(accessToken, authId, userId, refreshToken, expireTime, userType, authIdToken)
            DAuthLogger.d("手机号/邮箱验证码登录accessToken：${accessToken.maskSensitiveData()},refreshToken:$refreshToken")

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
                requestApi.logout(requestBody)
            }
        }
        clearAccountInfo()
    }

    internal fun clearAccountInfo() {
        prefs.clearLoginStateInfo()
        //walletManager.clearData()
    }

    override suspend fun setRecoverPassword(resetPwdParams: ResetByPasswordParam): SetPasswordData {
        val response = requestApi.resetByPassword(resetPwdParams)
        if(response?.isSuccess() == true) {
           return SetPasswordData(ResponseCode.RESPONSE_CORRECT_CODE,response.info)
        }
        return SetPasswordData(response?.ret, response?.info)
    }

    override suspend fun sendPhoneVerifyCode(phone: String, areaCode: String): BaseResponse? {
        val body = SendPhoneVerifyCodeParam(openudid = null, phone, areaCode)
        return requestApi.sendPhoneVerifyCode(body)
    }

    override suspend fun sendEmailVerifyCode(email: String): BaseResponse? {
        val body = SendEmailVerifyCodeParam(email)
        return requestApi.sendEmailVerifyCode(body)
    }

    override suspend fun bindPhone(param: BindPhoneParam) {
        requestApi.bindPhone(param)
    }

    override suspend fun bindEmail(email: String, verifyCode: String): BaseResponse? {
        val authId = prefs.getAuthId()
        val accessToken = prefs.getAccessToken()
        val body = BindEmailParam(authId, email, verifyCode, accessToken)
        return requestApi.bindEmail(body)
    }

    override suspend fun setPassword(param: SetPasswordParam): BaseResponse? {
        return requestApi.setPassword(param)
    }

    override suspend fun queryAccountByEmail(email: String): AccountRes? {
        val authId = prefs.getAuthId()
        val requestApi = requestApi
        val accessToken = prefs.getAccessToken()
        DAuthLogger.d("queryAccountByEmail accessToken:${accessToken.maskSensitiveData()}")
        val body = QueryByEMailParam(email, accessToken, authId)
        return requestApi.queryByEMail(body)
    }

    override suspend fun queryAccountByAuthid(): AccountRes? {
        val authId = prefs.getAuthId()
        val accessToken = prefs.getAccessToken()
        val body = QueryByAuthIdParam(authId, accessToken)
        return requestApi.queryByAuthId(body)
    }

    override suspend fun checkEmail(email: String, verifyCode: String): BaseResponse? {
        val body = CheckEmailParam(
            email,
            verifyCode
        )
        return requestApi.checkEmail(body)
    }
}