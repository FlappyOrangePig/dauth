package com.infras.dauth.ui.fiat.transaction.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.infras.dauth.R
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.ext.isAreaCode
import com.infras.dauth.ext.isMail
import com.infras.dauth.ext.isPhone
import com.infras.dauth.ext.isVerifyCode
import com.infras.dauth.manager.AppManagers
import com.infras.dauth.repository.FiatTxRepository
import com.infras.dauth.repository.SignInRepository
import com.infras.dauthsdk.login.model.CountryListParam
import com.infras.dauthsdk.login.model.CountryListRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VerifySetProfileViewModel : BaseViewModel() {

    private val repo = SignInRepository()
    private val fiatRepo = FiatTxRepository()
    private val resourceManager get() = AppManagers.resourceManager
    private val _commonEvent = Channel<VerifySetProfileEvent>(capacity = Channel.UNLIMITED)
    val commonEvent: Flow<VerifySetProfileEvent> = _commonEvent.receiveAsFlow()
    val showTab = MutableLiveData(0)

    val phoneContent = MutableLiveData("")
    val phoneCodeContent = MutableLiveData("")
    val mailContent = MutableLiveData("")
    val mailCodeContent = MutableLiveData("")
    private val areaSelected = MutableLiveData(0)
    val areaCodes = MutableLiveData<List<CountryListRes.CountryInfo>>()

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

    private suspend fun bindEmail(email: String, code: String) {
        val result = showLoading { repo.bindEmail(email, code) }
        val success = result != null && result.isSuccess()
        if (success) {
            _commonEvent.send(VerifySetProfileEvent.BindEmailSuccess)
        }
        val toast = resourceManager.getResponseDigest(result)
        toast(toast)
    }

    fun sendSms(phone: String, area: String) {
        viewModelScope.launch {
            val result = showLoading { repo.sendPhoneVerifyCode(phone, area) }
            val toast = resultString(result)
            toast(toast)
        }
    }

    private suspend fun bindPhone(phone: String, area: String, verifyCode: String) {
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

    fun handleContinue() = viewModelScope.launch {
        if (showTab.value == 0) {
            val mail = mailContent.value.orEmpty()
            if (!mail.isMail()) {
                toast("mail format error")
                return@launch
            }
            val mailCode = mailCodeContent.value.orEmpty()
            if (!mailCode.isVerifyCode()) {
                toast("code format error")
                return@launch
            }

            bindEmail(email = mail, code = mailCode)
        } else {
            val phone = phoneContent.value.orEmpty()
            if (!phone.isPhone()) {
                toast("phone format error")
                return@launch
            }
            val phoneCode = phoneCodeContent.value.orEmpty()
            if (!phoneCode.isVerifyCode()) {
                toast("code format error")
                return@launch
            }
            val pos = areaSelected.value!!
            val codes = areaCodes.value!!.map { it.phoneAreaCode }
            val areaCode = if (pos >= 0 && pos < codes.size) {
                codes[pos].removePrefix("+")
            } else {
                ""
            }
            if (!areaCode.isAreaCode()) {
                toast("area code format error")
                return@launch
            }

            bindPhone(phone, areaCode, phoneCode)
        }
    }

    fun selectAreaCode(pos: Int) {
        areaSelected.value = pos
    }

    fun fetchCountry() {
        viewModelScope.launch {
            val r = showLoading {
                fiatRepo.countryList(CountryListParam())
            }
            if (r != null && r.isSuccess()) {
                val countryInfoList = withContext(Dispatchers.IO) {
                    r.data?.list.orEmpty()
                        .asSequence()
                        .filter { it.isSupport }
                        .filter { it.phoneAreaCode.isNotEmpty() }
                        .filter { it.countryCode.isNotEmpty() }
                        .filter { it.countryName.isNotEmpty() }
                        .sortedBy { it.countryName }
                        .toMutableList()
                }
                areaCodes.value = countryInfoList
            }
        }
    }
}

sealed class VerifySetProfileEvent {
    object BindEmailSuccess : VerifySetProfileEvent()
    object BindPhoneSuccess : VerifySetProfileEvent()
}