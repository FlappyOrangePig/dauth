package com.infras.dauth.ui.fiat.transaction.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.entity.KycOpenAccountMethod
import com.infras.dauth.repository.FiatTxRepository
import com.infras.dauthsdk.login.model.AccountDocumentationRequestParam
import com.infras.dauthsdk.login.model.CountryListParam
import com.infras.dauthsdk.login.model.CountryListRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VerifySetIdCardViewModel : BaseViewModel() {

    private val repo = FiatTxRepository()
    private val _countries = MutableLiveData<List<CountryListRes.CountryInfo>>()
    val countries: LiveData<List<CountryListRes.CountryInfo>> = _countries
    private val _openAccountMethod = MutableLiveData<KycOpenAccountMethod>()
    val openAccountMethod: LiveData<KycOpenAccountMethod> = _openAccountMethod
    private val _fullName = MutableLiveData<Boolean>()
    val fullName: LiveData<Boolean> = _fullName
    private val _currentDocumentType = MutableLiveData<Int?>()
    val currentDocumentType: LiveData<Int?> = _currentDocumentType

    fun fetchCountry() {
        viewModelScope.launch {
            val r = showLoading {
                repo.countryList(CountryListParam())
            }
            if (r != null && r.isSuccess()) {
                val countryInfoList = withContext(Dispatchers.IO) {
                    r.data?.list.orEmpty().sortedBy {
                        it.countryName
                    }
                }
                _countries.value = countryInfoList
            }
        }
    }

    fun selectCountry(pos: Int) {
        val kycOpenAccountMethod = KycOpenAccountMethod(
            idCard = false,
            passport = false,
            driverSLicence = false
        )
        _openAccountMethod.value = kycOpenAccountMethod

        val countriesNotNull = countries.value ?: return
        val countryCode = countriesNotNull[pos].countryCode
        fetchOpenAccountMethod(countryCode)
    }

    private fun fetchOpenAccountMethod(countryCode: String) {
        viewModelScope.launch {
            val r = showLoading {
                repo.accountDocumentationRequest(AccountDocumentationRequestParam(countryCode))
            }
            if (r != null && r.isSuccess()) {
                val data = r.data
                if (data != null) {
                    var idCard = false
                    var passport = false
                    var driverSLicence = false
                    when (data.idType) {
                        4 -> {
                            idCard = true
                        }

                        1 -> {
                            passport = true
                        }

                        2 -> {
                            driverSLicence = true
                        }
                    }
                    val kycOpenAccountMethod = KycOpenAccountMethod(
                        idCard = idCard,
                        passport = passport,
                        driverSLicence = driverSLicence
                    )
                    _openAccountMethod.value = kycOpenAccountMethod

                    _currentDocumentType.value = when {
                        idCard -> 0
                        passport -> 1
                        driverSLicence -> 2
                        else -> null
                    }

                    // 应该是拉回来的但是现在服务端还没有
                    _fullName.value = true
                }
            }
        }
    }

    fun selectOpenAccountType(type: Int) {
        _currentDocumentType.value = type
    }
}