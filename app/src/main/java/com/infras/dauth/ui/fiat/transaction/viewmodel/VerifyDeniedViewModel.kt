package com.infras.dauth.ui.fiat.transaction.viewmodel

import androidx.lifecycle.viewModelScope
import com.infras.dauth.app.BaseViewModel
import kotlinx.coroutines.launch

class VerifyDeniedViewModel : BaseViewModel() {

    fun requestPageData() {
        viewModelScope.launch {
            showLoading {


            }
        }
    }
}