package com.infras.dauth.ui.fiat.transaction.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.entity.KycOpenAccountMethod
import com.infras.dauth.repository.FiatTxRepository
import com.infras.dauth.util.ToastUtil
import com.infras.dauthsdk.login.model.AccountDocumentationRequestParam
import com.infras.dauthsdk.login.model.AccountDocumentationRequestRes
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
    private val _currentDocumentType = MutableLiveData<String?>()
    val currentDocumentType: LiveData<String?> = _currentDocumentType

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
        // 国家变更，清空认证方式，等接口返回
        val kycOpenAccountMethod = KycOpenAccountMethod(listOf())
        _openAccountMethod.value = kycOpenAccountMethod

        val countriesNotNull = countries.value ?: return
        val country = countriesNotNull[pos]

        // 国家变更，名字模式变更
        _fullName.value = country.useFullName

        val countryCode = country.countryCode
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

                    data.idTypeList.forEach {
                        when (it.idType) {
                            "ID_CARD" -> {
                                idCard = true
                            }

                            "PASSPORT" -> {
                                passport = true
                            }

                            "DRIVERS" -> {
                                driverSLicence = true
                            }
                        }
                    }


                    val kycOpenAccountMethod = KycOpenAccountMethod(
                        data.idTypeList
                    )
                    _openAccountMethod.value = kycOpenAccountMethod

                    val type = when {
                        idCard -> AccountDocumentationRequestRes.IdTypeInfo.ID_CARD
                        passport -> AccountDocumentationRequestRes.IdTypeInfo.PASSPORT
                        driverSLicence -> AccountDocumentationRequestRes.IdTypeInfo.DRIVERS
                        else -> null
                    }
                    selectOpenAccountType(type)
                }
            }
        }
    }

    fun selectOpenAccountType(type: String?) {
        _currentDocumentType.value = type
    }

    fun getPictureCount(): Int? {
        val methods = openAccountMethod.value ?: return null
        return methods.idTypeList.find { it.idType == currentDocumentType.value }?.sideNum
    }
}