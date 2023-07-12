package com.cyberflow.dauthsdk.api

import android.app.Activity
import android.content.Context
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.LoginResultData
import com.cyberflow.dauthsdk.api.entity.SetPasswordData
import com.cyberflow.dauthsdk.login.model.*
import com.cyberflow.dauthsdk.login.network.BaseResponse

interface ILoginApi {

    fun initSDK(context: Context, config: SdkConfig)

    suspend fun login(account: String, passWord: String) : LoginResultData?

    /**
     * @param type 第三方账号类型 GOOGLE TWITTER FACEBOOK
     * @param activity
     */

    suspend fun loginWithType(type: String, activity: Activity) : LoginResultData?

    /**
     * @param account 自有账号（字母和数字组合）
     * @param passWord 密码
     * @param confirmPwd 确认密码
     */
    suspend fun createDAuthAccount(account: String, passWord: String, confirmPwd: String) : Int?

    /**
     * 手机号或邮箱登录
     */
    suspend fun loginByMobileOrEmail(account: String, verifyCode: String, type: Int) : LoginResultData?


    /**
     * 登出
     */
    fun logout()


    /**
     * 重置密码
     */
    suspend fun setRecoverPassword(resetPwdParams: ResetByPasswordParam): SetPasswordData

    /**
     * @param phone 手机号
     * @param areaCode  区号
     */
    suspend fun sendPhoneVerifyCode(phone: String, areaCode: String): Boolean

    /**
     * @param email 邮箱
     */
    suspend fun sendEmailVerifyCode(email: String): BaseResponse?

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
    suspend fun bindEmail(email: String, verifyCode: String) : Boolean


    /**
     * EOA钱包授权登录
     */
    suspend fun link2EOAWallet(context: Context) : LoginResultData?

    /**
     * 设置密码
     * @param passWord
     */
    suspend fun setPassword(setPasswordParam: SetPasswordParam) : Int?

    /**
     * 根据邮箱查询用户
     * @param email
     */
    suspend fun queryAccountByEmail(email: String) : AccountRes?

    /**
     * 根据用户id查询用户信息
     * @param openId 用户id
     */
    suspend fun queryAccountByAuthid() : AccountRes?
}