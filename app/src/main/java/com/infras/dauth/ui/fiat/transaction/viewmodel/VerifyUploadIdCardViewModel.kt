package com.infras.dauth.ui.fiat.transaction.viewmodel

import androidx.lifecycle.viewModelScope
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.repository.FiatTxRepository
import com.infras.dauthsdk.login.model.AccountOpenParam
import kotlinx.coroutines.launch

class VerifyUploadIdCardViewModel : BaseViewModel() {

    private val repo = FiatTxRepository()

    fun accountOpen(param: AccountOpenParam) {
        viewModelScope.launch {
            showLoading {
                repo.accountOpen(param)
            }
        }
    }
}