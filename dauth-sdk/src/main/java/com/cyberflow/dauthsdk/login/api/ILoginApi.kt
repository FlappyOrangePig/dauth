package com.cyberflow.dauthsdk.login.api

import android.app.Activity
import android.content.Intent
import com.cyberflow.dauthsdk.login.callback.BaseHttpCallback
import com.cyberflow.dauthsdk.login.callback.ResetPwdCallback
import com.cyberflow.dauthsdk.login.model.BindPhoneParam
import com.cyberflow.dauthsdk.login.model.LoginRes

interface ILoginApi {

    suspend fun loginApi(account: String, passWord: String) : Int?

    /**
     * @param type 第三方账号类型 GOOGLE TWITTER FACEBOOK
     * @param activity
     */

    fun loginWithTypeApi(type: String, activity: Activity)

    /**
     * @param account 自有账号（字母和数字组合）
     * @param passWord 密码
     * @param confirmPwd 确认密码
     */
    fun createDAuthAccountApi(account: String, passWord: String, confirmPwd: String) : Boolean

    /**
     * 手机号或邮箱登录
     */
    suspend fun loginByMobileOrEmailApi(account: String, verifyCode: String, type: Int) : Int?


    /**
     * 登出
     */
    fun logoutApi(openUid: String)


    /**
     * 重置密码
     */
    fun setRecoverPasswordApi(callback: ResetPwdCallback)

    /**
     * @param phone 手机号
     * @param areaCode  区号
     */
    fun sendPhoneVerifyCodeApi(phone: String, areaCode: String)

    /**
     * @param email 邮箱
     */
    suspend fun sendEmailVerifyCodeApi(email: String): Boolean

    /**
     * @param bindParams 对象
     *  包含 openudid(用户id)
     *  phone(手机号)
     *  phone_area_code(区号)
     *  verify_code(验证码)
     */
    fun bindPhoneApi(bindParams: BindPhoneParam)

    /**
     * @param email 邮箱
     * @param verifyCode 邮箱验证码
     */
    fun bindEmailApi(email: String, verifyCode: String)

//    suspend fun handleThirdPartyLogin(requestCode: Int, resultCode: Int, data: Intent?): Int
}