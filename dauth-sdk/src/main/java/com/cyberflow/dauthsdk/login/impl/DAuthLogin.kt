package com.cyberflow.dauthsdk.login.impl

import android.app.Activity
import com.cyberflow.dauthsdk.login.api.ILoginApi
import com.cyberflow.dauthsdk.login.callback.BaseHttpCallback
import com.cyberflow.dauthsdk.login.callback.ResetPwdCallback
import com.cyberflow.dauthsdk.login.const.LoginConst
import com.cyberflow.dauthsdk.login.const.LoginConst.ACCOUNT
import com.cyberflow.dauthsdk.login.const.LoginConst.ACCOUNT_TYPE_OF_EMAIL
import com.cyberflow.dauthsdk.login.const.LoginConst.ACCOUNT_TYPE_OF_OWN
import com.cyberflow.dauthsdk.login.const.LoginConst.AUTH_CODE
import com.cyberflow.dauthsdk.login.const.LoginConst.CODE_CHALLENGE
import com.cyberflow.dauthsdk.login.const.LoginConst.CODE_CHALLENGE_METHOD
import com.cyberflow.dauthsdk.login.const.LoginConst.CODE_VERIFIER
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
import com.cyberflow.dauthsdk.login.google.GoogleLoginManager
import com.cyberflow.dauthsdk.login.model.*
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.twitter.TwitterLoginManager
import com.cyberflow.dauthsdk.login.utils.*
import com.cyberflow.dauthsdk.wallet.api.IWalletApi
import com.cyberflow.dauthsdk.wallet.impl.WalletHolder
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class DAuthLogin : ILoginApi, IWalletApi by WalletHolder.walletApi {
    companion object {
        val instance by lazy {
            DAuthLogin()
        }
    }

    override suspend fun loginApi(account: String, passWord: String): Int? {
        val map = HashMap<String, String>()
        map[LoginConst.USER_TYPE] = LoginConst.ACCOUNT_TYPE_OF_OWN
        map[LoginConst.ACCOUNT] = account
        map[LoginConst.PASSWORD] = passWord
        val sign = SignUtils.sign(map)
        val loginParam = LoginParam(
            LoginConst.ACCOUNT_TYPE_OF_OWN.toInt(),
            account = account,
            password = passWord,
            sign = sign
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
        map[USER_TYPE] = "10"
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
        val map = HashMap<String, String>()
        map[CODE_VERIFIER] = codeVerifier
        map[AUTH_CODE] = code
        val sign = SignUtils.sign(map)
        var tokenAuthenticationRes: TokenAuthenticationRes? = null
        runBlocking {
            val body = TokenAuthenticationParam(codeVerifier, code, sign)
            tokenAuthenticationRes = RequestApi().ownOauth2Token(body, didToken)
        }
        return tokenAuthenticationRes
    }

    /**
     * @param type 第三方账号类型 GOOGLE TWITTER FACEBOOK
     * @param activity
     */

    override fun loginWithTypeApi(type: String, activity: Activity) {
        when (type) {
            LoginType.GOOGLE -> {
                GoogleLoginManager.instance.googleSignInAuth(activity)
            }
            LoginType.TWITTER -> {
                TwitterLoginManager.instance.twitterLoginAuth(
                    activity, object : Callback<TwitterSession>() {
                        override fun success(result: Result<TwitterSession>?) {
                            val token = result?.data?.authToken
                            val userId = result?.data?.userId

                            DAuthLogger.d("twitter login success")
                        }

                        override fun failure(exception: TwitterException?) {
                            DAuthLogger.e("twitter login failed:$exception")
                        }

                    }
                )
            }
        }
    }

    /**
     * @param account 自有账号（字母和数字组合）
     * @param passWord 密码
     * @param confirmPwd 确认密码
     */

    override fun createDAuthAccountApi(
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
    override suspend fun loginByMobileOrEmailApi(
        account: String,
        verifyCode: String,
        type: Int
    ): Int {
        var loginCode : Int = -1
        val map = HashMap<String, String>()
        map[USER_TYPE] = type.toString()
        map[VERIFY_CODE] = verifyCode
        if (type == 10) {
            map[ACCOUNT] = account
            val sign = SignUtils.sign(map)
            val loginParam = LoginParam(type, sign, account = account, verify_code = verifyCode)
            val loginRes = withContext(Dispatchers.IO) {
                RequestApi().login(loginParam)
            }
            if (loginRes?.iRet == 0) {
                val data = loginRes.data
                val userInfo = JwtDecoder().decoded(data.did_token.orEmpty())

                val didToken = loginRes.data.did_token
                //DAuth 授权接口测试获取临时code
                val codeVerifier = JwtChallengeCode().generateCodeVerifier()
                val codeChallenge = JwtChallengeCode().generateCodeChallenge(codeVerifier)
                DAuthLogger.d("codeVerify == $codeVerifier")

                val code = withContext(Dispatchers.IO) {
                    //DAuth 授权接口测试获取code
                    loginAuth(codeChallenge, didToken.orEmpty())
                }

                val tokenAuthenticationRes = withContext(Dispatchers.IO) {
                    //DAuth 授权接口测试获取token
                    getDAuthToken(codeVerifier, code, didToken)
                }

                val accessToken = tokenAuthenticationRes?.data?.access_token.orEmpty()
                val authIdToken = tokenAuthenticationRes?.data?.id_token.orEmpty()
                val userId = JwtDecoder().decoded(authIdToken).sub.orEmpty()
                val authId = userInfo.sub.orEmpty()

                val queryWalletRes = withContext(Dispatchers.IO) {
                    RequestApi().queryWallet(accessToken, authId)
                }

                if (queryWalletRes?.address.isNullOrEmpty()) {
                    val code = withContext(Dispatchers.IO) {
                        createWallet("123456")
                    }
                    if (code == 0) { //绑定钱包
                        val bindWalletRes = withContext(Dispatchers.IO) {
                            RequestApi().bindWallet(accessToken, userId)
                        }
                        if (bindWalletRes?.iRet == 0) {
                            DAuthLogger.d("绑定钱包成功")
                        }
                        loginCode = 0
                    }

                } else {
                    loginCode = -1
                }

            }

        } else {        //TODO 手机号登录
            map[LoginConst.PHONE] = account
            val sign = SignUtils.sign(map)
            val loginParam = LoginParam(type, sign, phone = account, verify_code = verifyCode)

        }
        return loginCode
    }

    override fun logoutApi(openUid: String) {
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

    override fun setRecoverPasswordApi(callback: ResetPwdCallback) {

    }

    /**
     * @param phone 手机号
     * @param areaCode  区号
     */
    override fun sendPhoneVerifyCodeApi(phone: String, areaCode: String) {
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

    override suspend fun sendEmailVerifyCodeApi(email: String): Boolean {
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
    override fun bindPhoneApi(bindParams: BindPhoneParam) {
        ThreadPoolUtils.execute {
            RequestApi().bindPhone(bindParams)
        }
    }

    /**
     * @param email 邮箱
     * @param verifyCode 邮箱验证码
     */
    override fun bindEmailApi(email: String, verifyCode: String) {

    }

}