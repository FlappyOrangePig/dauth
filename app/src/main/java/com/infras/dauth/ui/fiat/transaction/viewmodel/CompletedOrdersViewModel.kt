package com.infras.dauth.ui.fiat.transaction.viewmodel

import androidx.lifecycle.viewModelScope
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.repository.FiatTxRepository
import com.infras.dauthsdk.login.model.OrderListParam
import kotlinx.coroutines.launch

class CompletedOrdersViewModel: BaseViewModel() {

    private val repo = FiatTxRepository()

    fun requestOrders(state: String) {
        viewModelScope.launch {
            repo.orderList(
                OrderListParam(
                    next_id = 1,
                    page_size = FiatTxRepository.PAGE_SIZE,
                    state = state
                )
            )
        }
    }
}