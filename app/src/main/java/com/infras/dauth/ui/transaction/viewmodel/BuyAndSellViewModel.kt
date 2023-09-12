package com.infras.dauth.ui.transaction.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.util.HideApiUtil
import com.infras.dauthsdk.login.model.AccountDetailRes
import com.infras.dauthsdk.login.model.DigitalCurrencyListRes
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class BuyAndSellViewModel : BaseViewModel() {

    private var _accountDetail = MutableLiveData<AccountDetailRes.Data?>()
    val accountDetail: LiveData<AccountDetailRes.Data?> = _accountDetail

    private val _toastEvent = Channel<String>(capacity = Channel.UNLIMITED)
    val toastEvent: Flow<String> = _toastEvent.receiveAsFlow()

    private val _currencyList = MutableLiveData<DigitalCurrencyListRes.Data?>()
    val currencyList: LiveData<DigitalCurrencyListRes.Data?> = _currencyList

    fun fetchAccountDetail() = viewModelScope.launch {
        val r = HideApiUtil.getDepositApi().accountDetail()
        if (r != null && r.isSuccess()) {
            _accountDetail.value = r.data
        }
    }

    fun fetchCurrencyList() = viewModelScope.launch {
        val r = HideApiUtil.getDepositApi().currencyList()
        if (r != null && r.isSuccess()) {
            _currencyList.value = r.data
        }
    }
}