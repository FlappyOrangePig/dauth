package com.infras.dauth.ui.fiat.transaction.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.entity.FiatOrderListItemEntity
import com.infras.dauth.repository.FiatTxRepository
import com.infras.dauth.ui.fiat.transaction.test.OrderListMockData
import com.infras.dauth.ui.fiat.transaction.util.CurrencyCalcUtil
import com.infras.dauth.ui.fiat.transaction.util.CurrencyCalcUtil.scale
import com.infras.dauth.ui.fiat.transaction.util.TimeUtil
import com.infras.dauthsdk.login.model.OrderListParam
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

    fun requestOrders(state: String?) {
        viewModelScope.launch {
            refresh {
                val r = repo.orderList(
                    OrderListParam(
                        next_id = 0,
                        page_size = FiatTxRepository.PAGE_SIZE,
                        state = state
                    )
                )
                if (r != null && r.isSuccess()) {
                    val list = r.data?.list.orEmpty()
                    val newValue = if (false) {
                        OrderListMockData.gen()
                    } else {
                        list.map {
                            val d = TimeUtil.getOrderTime(it.create_time)
                            val fiatInfo = CurrencyCalcUtil.getFiatInfo(it.fiat_code)
                            val cryptoInfo = CurrencyCalcUtil.getCryptoInfo(it.crypto_code)
                            val fiatPrecision = fiatInfo?.fiatPrecision?.toInt()

                            FiatOrderListItemEntity(
                                orderId = it.out_order_id.toString(),
                                title = "Buy ${it.crypto_code}",
                                unitPrice = "Unit Price ${it.price.scale(fiatPrecision)} ${it.fiat_code}",
                                quantity = "Quantity ${it.quantity.scale(cryptoInfo?.cryptoPrecision)} ${it.crypto_code}",
                                totalPrice = "${it.amount.scale(fiatPrecision)} ${it.fiat_code}",
                                time = d,
                                state = "${it.state.orEmpty()} >",
                            )
                        }
                    }
                    _list.value = newValue
                } else {
                    _list.value = listOf()
                }
            }
        }
    }
}