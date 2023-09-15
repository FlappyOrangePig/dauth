package com.infras.dauth.ui.fiat.transaction.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.entity.FiatOrderListItemEntity
import com.infras.dauth.repository.FiatTxRepository
import com.infras.dauthsdk.login.model.OrderListParam
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PendingOrdersViewModel : BaseViewModel() {

    private val repo = FiatTxRepository()
    private val _list = MutableLiveData<List<FiatOrderListItemEntity>>()
    val list: LiveData<List<FiatOrderListItemEntity>> = _list
    private val _refreshing = MutableLiveData<Boolean>()
    val refreshing: LiveData<Boolean> = _refreshing

    private suspend fun <T> refresh(block: suspend () -> T): T {
        _refreshing.value = true
        val r = block.invoke()
        _refreshing.value = false
        return r
    }

    fun requestOrders(state: String) {
        viewModelScope.launch {
            val r = refresh {
                repo.orderList(
                    OrderListParam(
                        next_id = 1,
                        page_size = FiatTxRepository.PAGE_SIZE,
                        state = state
                    )
                )

                delay(100)
                listOf(
                    FiatOrderListItemEntity(
                        "123123",
                        "Buy USDT",
                        "Unit Price 7.30 CNY",
                        "quantity 1,888.00 USDT",
                        "$123123",
                        "08/17/2023, 18:29",
                        "Canceled  >",
                    ),
                    FiatOrderListItemEntity(
                        "123123",
                        "Buy USDT",
                        "Unit Price 7.30 CNY",
                        "quantity 1,888.00 USDT",
                        "$123123",
                        "08/17/2023, 18:29",
                        "Fulfilled  >",
                    ),
                ).toMutableList()
            }
            _list.value = r
        }


    }
}//订单状态 CREATE_FAIL：订单创建失败,UNPAID：未⽀付,PAID：已⽀付,CANCEL：已取消,COMPLETED：已完成,APPEAL：申诉中,WITHDRAW_FAIL：提现失败,WITHDRAW_SUCCESS：提现成功