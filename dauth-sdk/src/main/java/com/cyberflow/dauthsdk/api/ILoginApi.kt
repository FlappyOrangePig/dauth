package com.cyberflow.dauthsdk.api

import android.app.Activity
import android.content.Context
import com.cyberflow.dauthsdk.login.model.AccountRes
import com.cyberflow.dauthsdk.login.model.BindPhoneParam
import com.cyberflow.dauthsdk.login.model.ResetByPasswordParam

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
    suspend fun setRecoverPassword(resetPwdParams: ResetByPasswordParam): Boolean

    /**
     * @param phone 手机号
     * @param areaCode  区号
     */
    suspend fun sendPhoneVerifyCode(phone: String, areaCode: String): Boolean

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
    suspend fun bindPhone(bindParams: BindPhoneParam)

    /**
     * @param email 邮箱
     * @param verifyCode 邮箱验证码
     */
    suspend fun bindEmail(email: String, verifyCode: String)


    /**
     * EOA钱包授权登录
     */
    suspend fun link2EOAWallet(context: Context) : Int?

    /**
     * 设置密码
     * @param passWord
     */
    suspend fun setPassword(passWord: String) : Int?

    /**
     * 根据邮箱查询用户
     * @param email
     */
    suspend fun queryAccountByEmail(email: String) : AccountRes?
}