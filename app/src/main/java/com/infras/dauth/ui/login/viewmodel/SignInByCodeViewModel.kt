package com.infras.dauth.ui.login.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.infras.dauth.ext.isMail
import com.infras.dauth.ext.isPhone
import com.infras.dauth.manager.sdk
import com.infras.dauth.ui.login.repository.SignInRepository
import com.infras.dauth.util.DemoPrefs
import com.infras.dauthsdk.api.annotation.DAuthAccountType

class SignInByCodeViewModel : ViewModel() {

    private val sdk get() = sdk()
    private val _account = MutableLiveData<String>()
    private val account: LiveData<String> = _account

    private val _verifyCode = MutableLiveData<String>()
    private val verifyCode: LiveData<String> = _verifyCode

    private val repo = SignInRepository()

    fun updateAccount(text: String) {
        _account.value = text
    }

    fun updateVerifyCode(text: String) {
        _verifyCode.value = text
    }

    suspend fun sendSubmitRequest(): Boolean {
        val account = this.account.value.orEmpty()
        val userType = when {
            (account.isMail()) -> {
                DAuthAccountType.ACCOUNT_TYPE_OF_EMAIL
            }

            (account.isPhone()) -> {
                DAuthAccountType.ACCOUNT_TYPE_OF_MOBILE
            }

            else -> return false
        }
        return repo.signIn {
            val verifyCode = this.verifyCode.value.orEmpty()
            sdk.loginByMobileOrEmail(account, verifyCode, userType)
        }.also {
            DemoPrefs.setLastEmail(account)
            DemoPrefs.setLastLoginType(0)
        }
    }

    suspend fun sendVerifyCode(): Boolean {
        val email = account.value.orEmpty()
        return if (email.isPhone()) {
            repo.sendPhoneVerifyCode(email, "86")
        } else if (email.isMail()) {
            repo.sendEmailVerifyCode(email)
        } else {
            false
        }
    }
}