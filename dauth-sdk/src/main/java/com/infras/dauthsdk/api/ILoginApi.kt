package com.infras.dauthsdk.api

import android.app.Activity
import android.content.Context
import com.infras.dauthsdk.api.annotation.DAuthAccountType
import com.infras.dauthsdk.api.annotation.SignType3rd
import com.infras.dauthsdk.api.entity.LoginResultData
import com.infras.dauthsdk.api.entity.SetPasswordData
import com.infras.dauthsdk.login.model.*
import com.infras.dauthsdk.login.network.BaseResponse

/**
 * I login api
 *
 * @constructor Create empty I login api
 */
interface ILoginApi {

    /**
     * Init sdk
     *
     * @param context
     * @param config
     */
    fun initSDK(context: Context, config: SdkConfig)

    /**
     * Login
     *
     * @param account
     * @param passWord
     * @return
     */
    suspend fun login(account: String, passWord: String): LoginResultData?

    /**
     * Login with type
     *
     * @param type
     * @param activity
     * @return
     */
    suspend fun loginWithType(@SignType3rd type: String, activity: Activity): LoginResultData?

    /**
     * Create d auth account
     *
     * @param account
     * @param password
     * @param confirmPwd
     * @return
     */
    suspend fun createDAuthAccount(account: String, password: String, confirmPwd: String): Int?

    /**
     * Login by mobile or email
     *
     * @param account
     * @param verifyCode
     * @param type
     * @return
     */
    suspend fun loginByMobileOrEmail(
        account: String,
        verifyCode: String,
        @DAuthAccountType type: Int
    ): LoginResultData?

    /**
     * Logout
     *
     */
    fun logout()

    /**
     * Set recover password
     *
     * @param resetPwdParams
     * @return
     */
    suspend fun setRecoverPassword(resetPwdParams: ResetByPasswordParam): SetPasswordData

    /**
     * Send phone verify code
     *
     * @param phone
     * @param areaCode
     * @return
     */
    suspend fun sendPhoneVerifyCode(phone: String, areaCode: String): BaseResponse?

    /**
     * Send email verify code
     *
     * @param email
     * @return
     */
    suspend fun sendEmailVerifyCode(email: String): BaseResponse?

    /**
     * Bind phone
     *
     * @param param
     */
    suspend fun bindPhone(param: BindPhoneParam): BaseResponse?

    /**
     * Bind email
     *
     * @param email
     * @param verifyCode
     * @return
     */
    suspend fun bindEmail(email: String, verifyCode: String): BaseResponse?

    /**
     * Set password
     *
     * @param param
     * @return
     */
    suspend fun setPassword(param: SetPasswordParam): BaseResponse?

    /**
     * Query account by email
     *
     * @param email
     * @return
     */
    suspend fun queryAccountByEmail(email: String): AccountRes?

    /**
     * Query account by authid
     *
     * @return
     */
    suspend fun queryAccountByAuthid(): AccountRes?

    /**
     * Check email
     *
     * @param email
     * @param verifyCode
     * @return
     */
    suspend fun checkEmail(email: String, verifyCode: String): BaseResponse?
}