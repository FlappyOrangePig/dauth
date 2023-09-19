package com.infras.dauth.ui.fiat.transaction.viewmodel

import androidx.lifecycle.viewModelScope
import com.infras.dauth.MyApplication
import com.infras.dauth.R
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.manager.AppManagers
import com.infras.dauth.repository.SignInRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class VerifySetProfileViewModel : BaseViewModel() {

    private val repo = SignInRepository()
    private val resourceManager get() = AppManagers.resourceManager
    private val _commonEvent = Channel<VerifySetProfileEvent>(capacity = Channel.UNLIMITED)
    val commonEvent: Flow<VerifySetProfileEvent> = _commonEvent.receiveAsFlow()

    private fun resultString(result: Boolean): String {
        return resourceManager.getString(
            if (result) {
                R.string.success
            } else {
                R.string.failure
            }
        )
    }

    fun sendEmailVerifyCode(account: String) {
        viewModelScope.launch {
            val result = showLoading { repo.sendEmailVerifyCode(account) }
            val toast = resultString(result)
            toast(toast)
        }
    }

    fun bindEmail(email: String, code: String) {
        viewModelScope.launch {
            val result = showLoading { repo.bindEmail(email, code) }
            val success = result != null && result.isSuccess()
            if (success) {
                _commonEvent.send(VerifySetProfileEvent.BindEmailSuccess)
            }
            val toast = resourceManager.getResponseDigest(result)
            toast(toast)
        }
    }

    fun sendSms(phone: String, area: String) {
        viewModelScope.launch {
            val result = showLoading { repo.sendPhoneVerifyCode(phone, area) }
            val toast = resultString(result)
            toast(toast)
        }
    }

    fun bindPhone(phone: String, area: String, verifyCode: String) {
        viewModelScope.launch {
            val result = showLoading {
                repo.bindPhone(
                    phone = phone,
                    areaCode = area,
                    verifyCode = verifyCode
                )
            }
            val success = result != null && result.isSuccess()
            if (success) {
                _commonEvent.send(VerifySetProfileEvent.BindPhoneSuccess)
            }
            val toast = resourceManager.getResponseDigest(result)
            toast(toast)
        }
    }
}

sealed class VerifySetProfileEvent {
    object BindEmailSuccess : VerifySetProfileEvent()
    object BindPhoneSuccess : VerifySetProfileEvent()
}