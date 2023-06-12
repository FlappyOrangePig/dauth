package com.cyberflow.dauthsdk.api

import android.app.Activity
import android.content.Context
import com.cyberflow.dauthsdk.login.api.SdkConfig
import com.cyberflow.dauthsdk.login.callback.ResetPwdCallback
import com.cyberflow.dauthsdk.login.callback.ThirdPartyCallback
import com.cyberflow.dauthsdk.login.model.BindPhoneParam

interface ILoginApi {

    fun initSDK(context: Context, config: SdkConfig)

    suspend fun login(account: String, passWord: String) : Int?

    /**
     * @param type 第三方账号类型 GOOGLE TWITTER FACEBOOK
     * @param activity
     */

    suspend fun loginWithType(type: String, activity: Activity) : Int?

    /**
     * @param account 自有账号（字母和数字组合）
     * @param passWord 密码
     * @param confirmPwd 确认密码
     */
    suspend fun createDAuthAccount(account: String, passWord: String, confirmPwd: String) : Int?

    /**
     * 手机号或邮箱登录
     */
    suspend fun loginByMobileOrEmail(account: String, verifyCode: String, type: Int) : Int?


    /**
     * 登出
     */
    fun logout(openUid: String)


    /**
     * 重置密码
     */
    fun setRecoverPassword(callback: ResetPwdCallback)

    /**
     * @param phone 手机号
     * @param areaCode  区号
     */
    fun sendPhoneVerifyCode(phone: String, areaCode: String)

    /**
     * @param email 邮箱
     */
    suspend fun sendEmailVerifyCode(email: String): Boolean

    /**
     * @param bindParams 对象
     *  包含 openudid(用户id)
     *  phone(手机号)
     *  phone_area_code(区号)
     *  verify_code(验证码)
     */
    fun bindPhone(bindParams: BindPhoneParam)

    /**
     * @param email 邮箱
     * @param verifyCode 邮箱验证码
     */
    fun bindEmail(email: String, verifyCode: String)


    /**
     * EOA钱包授权登录
     */
    fun link2EOAWallet(context: Context) : Int?

    /**
     * @param
     */


}