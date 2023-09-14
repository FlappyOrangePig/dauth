package com.infras.dauth.ui.login.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.ext.isMail
import com.infras.dauth.ext.isPhone
import com.infras.dauth.manager.AccountManager
import com.infras.dauth.repository.SignInRepository
import com.infras.dauth.util.DemoPrefs
import com.infras.dauthsdk.api.annotation.DAuthAccountType

class SignInByPasswordViewModel : BaseViewModel() {

    private val sdk get() = AccountManager.sdk
    private val _account = MutableLiveData<String>()
    private val account: LiveData<String> = _account

    private val _password = MutableLiveData<String>()
    private val password: LiveData<String> = _password

    private val repo = SignInRepository()

    fun updateAccount(text: String) {
        _account.value = text
    }

    fun updatePassword(text: String) {
        _password.value = text
    }

    suspend fun sendSubmitRequest(): Boolean {
        val account = this.account.value.orEmpty()
        when {
            account.isMail() -> {
                DAuthAccountType.ACCOUNT_TYPE_OF_EMAIL
            }

            account.isPhone() -> {
                // 接口暂不支持
                return false
            }

            else -> return false
        }

        return repo.signIn {
            val password = this.password.value.orEmpty()
            sdk.login(account, password)
        }.also {
            DemoPrefs.setLastEmail(account)
            DemoPrefs.setLastLoginType(1)
        }
    }
}