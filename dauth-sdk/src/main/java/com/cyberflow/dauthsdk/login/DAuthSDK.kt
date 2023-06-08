package com.cyberflow.dauthsdk.login

import android.app.Activity
import android.app.Application
import android.content.Context
import com.cyberflow.dauthsdk.login.api.ILoginApi
import com.cyberflow.dauthsdk.login.api.bean.SdkConfig
import com.cyberflow.dauthsdk.login.callback.ResetPwdCallback
import com.cyberflow.dauthsdk.login.impl.DAuthLifeCycle
import com.cyberflow.dauthsdk.login.impl.LoginHolder
import com.cyberflow.dauthsdk.login.model.BindPhoneParam
import com.cyberflow.dauthsdk.login.twitter.TwitterLoginManager
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.wallet.api.IWalletApi
import com.cyberflow.dauthsdk.wallet.impl.WalletHolder


class DAuthSDK private constructor() : ILoginApi by LoginHolder.loginApi,
    IWalletApi by WalletHolder.walletApi {

    companion object {
        val instance by lazy {
            DAuthSDK()
        }
    }

    private var _context: Context? = null
    internal val context get() = _context ?: throw RuntimeException("please call initSDK() first")
    private var _config: SdkConfig? = null
    private val config get() = _config ?: throw RuntimeException("please call initSDK() first")

    /**
     * 所有后续SDK的方法都应在SDK初始化成功之后调用
     */
    fun initSDK(context: Context, config: SdkConfig) {
        val appContext = context.applicationContext as Application
        this._context = appContext
        this._config = config
        //Twitter初始化
        TwitterLoginManager.instance.initTwitterSDK(context, config)
        initWallet(appContext)
        appContext.registerActivityLifecycleCallbacks(DAuthLifeCycle)
        DAuthLogger.i("init sdk")
    }

    suspend fun login(account: String, passWord: String) {
        loginApi(account, passWord)
    }


    /**
     * @param type 第三方账号类型 GOOGLE TWITTER FACEBOOK
     * @param activity
     */
    fun loginWithType(type: String, activity: Activity) {
       loginWithTypeApi(type, activity)
    }

    /**
     * @param account 自有账号（字母和数字组合）
     * @param passWord 密码
     * @param confirmPwd 确认密码
     */

    fun createDAuthAccount(account: String, passWord: String, confirmPwd: String): Boolean {
        return createDAuthAccountApi(account, passWord, confirmPwd)
    }


    /**
     * @param account 手机号或邮箱
     * @param verifyCode 验证码
     * @param type  10(邮箱) 60(手机)
     */
    suspend fun loginByMobileOrEmail(account: String, verifyCode: String, type: Int) : Int? {
        return loginByMobileOrEmailApi(account, verifyCode, type)
    }


    fun logout(openUid: String) {
        logoutApi(openUid)
    }

    fun setRecoverPassword(callback: ResetPwdCallback) {
       setRecoverPasswordApi(callback)
    }

    /**
     * @param phone 手机号
     * @param areaCode  区号
     */
    fun sendPhoneVerifyCode(phone: String, areaCode: String) {
       sendPhoneVerifyCodeApi(phone, areaCode)
    }

    /**
     * @param email 邮箱
     */
    suspend fun sendEmailVerifyCode(email: String): Boolean {
        return sendEmailVerifyCodeApi(email)
    }

    /**
     * @param bindParams 对象
     *  包含 openudid(用户id)
     *  phone(手机号)
     *  phone_area_code(区号)
     *  verify_code(验证码)
     */
    fun bindPhone(bindParams: BindPhoneParam) {
        bindPhoneApi(bindParams)
    }

    /**
     * @param email 邮箱
     * @param verifyCode 邮箱验证码
     */
    fun bindEmail(email: String, verifyCode: String) {
        bindEmailApi(email, verifyCode)
    }
}