package com.infras.dauth.repository

import com.infras.dauth.R
import com.infras.dauth.manager.AccountManager
import com.infras.dauth.manager.AppManagers
import com.infras.dauth.util.LogUtil
import com.infras.dauthsdk.api.entity.DAuthResult
import com.infras.dauthsdk.api.entity.LoginResultData
import com.infras.dauthsdk.login.model.BindPhoneParam
import com.infras.dauthsdk.login.network.BaseResponse

sealed class SignInResult {

    protected fun string(res: Int): String = AppManagers.resourceManager.getString(res)

    abstract fun digest(): String

    object Success : SignInResult() {
        override fun digest(): String {
            return string(R.string.success)
        }
    }

    class CreateWalletFailed(private val msg: String) : SignInResult() {
        override fun digest(): String {
            return "create wallet failed, $msg"
        }
    }

    class ServerError(private val code: Int) : SignInResult() {
        override fun digest(): String {
            return "server error, $code"
        }
    }

    object NetworkError : SignInResult() {
        override fun digest(): String {
            return string(R.string.network_error)
        }
    }

    object UnknownError : SignInResult() {
        override fun digest(): String {
            return "unknown error"
        }
    }
}

class SignInRepository constructor() {

    companion object {
        private const val TAG = "SignInRepository"
    }

    private val sdk get() = AccountManager.sdk

    suspend fun signIn(signInBlock: suspend () -> LoginResultData?): SignInResult {
        val result = signInBlock.invoke()
        return handleResult(result)
    }

    private suspend fun handleResult(loginResultData: LoginResultData?): SignInResult {
        return when (loginResultData) {
            is LoginResultData.Success -> {
                if (loginResultData.needCreateWallet) {
                    val createResult = sdk.createWallet(false)
                    if (createResult is DAuthResult.Success) {
                        LogUtil.d(TAG, "创建的aa钱包地址：${createResult.data.address}")
                        SignInResult.Success
                    } else {
                        SignInResult.CreateWalletFailed(createResult.getError().orEmpty())
                    }
                } else {
                    val idToken = loginResultData.accessToken
                    // 处理登录成功逻辑
                    LogUtil.d(TAG, "登录成功，返回的ID令牌：$idToken")
                    SignInResult.Success
                }
            }

            is LoginResultData.Failure -> {
                val failureCode = loginResultData.code
                // 处理登录失败逻辑
                LogUtil.d(TAG, "登录失败，返回的errorCode：$failureCode")

                failureCode?.let { SignInResult.ServerError(failureCode) }
                    ?: SignInResult.NetworkError
            }

            else -> {
                LogUtil.e(TAG, "用户取消授权")
                SignInResult.UnknownError
            }
        }
    }

    suspend fun sendEmailVerifyCode(account: String): Boolean {
        return sdk.sendEmailVerifyCode(account)?.isSuccess() ?: false
    }

    suspend fun sendPhoneVerifyCode(phone: String, areaCode: String): Boolean {
        return sdk.sendPhoneVerifyCode(phone, areaCode)?.isSuccess() ?: false
    }

    suspend fun bindEmail(email: String, verifyCode: String): BaseResponse? {
        return sdk.bindEmail(email, verifyCode)
    }

    suspend fun bindPhone(phone: String, areaCode: String, verifyCode: String): BaseResponse? {
        return sdk.bindPhone(
            BindPhoneParam(
                phone = phone,
                phone_area_code = areaCode,
                verify_code = verifyCode
            )
        )
    }
}