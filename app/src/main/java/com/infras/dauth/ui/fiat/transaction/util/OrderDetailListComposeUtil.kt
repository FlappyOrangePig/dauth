package com.infras.dauth.ui.fiat.transaction.util

import com.infras.dauth.entity.FiatOrderDetailItemEntity
import com.infras.dauth.entity.FiatOrderState
import com.infras.dauth.ui.fiat.transaction.util.CurrencyCalcUtil.scale
import com.infras.dauthsdk.login.model.OrderDetailRes

object OrderDetailListComposeUtil {

    fun all(title: FiatOrderDetailItemEntity.Title, data: OrderDetailRes.Data) =
        listOf(title) + publicInfo(data) + txInfo(data)

    private fun publicInfo(data: OrderDetailRes.Data): MutableList<FiatOrderDetailItemEntity> {
        val fiatInfo = CurrencyCalcUtil.getFiatInfo(data.fiatCode)
        val fiatPrecision: Int? = fiatInfo?.fiatPrecision?.toInt()
        val fiatSymbol: String? = fiatInfo?.fiatSymbol ?: data.fiatCode
        val cryptoPrecision: Int? = CurrencyCalcUtil.getCryptoInfo(data.cryptoCode)?.cryptoPrecision

        return mutableListOf(
            FiatOrderDetailItemEntity.Group("Buy ${data.cryptoCode}"),
            FiatOrderDetailItemEntity.Text("Unit Price", "$fiatSymbol ${data.price.scale(fiatPrecision)}"),
            FiatOrderDetailItemEntity.Text("Quantity", "${data.quantity.scale(cryptoPrecision)} ${data.cryptoCode}"),
            FiatOrderDetailItemEntity.Text("Order amount", "$fiatSymbol ${data.amount.scale(fiatPrecision)}"),
            FiatOrderDetailItemEntity.Text(
                "Payment method",
                data.payMethodInfo?.payMethodName.orEmpty()
            ),
            FiatOrderDetailItemEntity.Split,
            FiatOrderDetailItemEntity.Group("Order information"),
            FiatOrderDetailItemEntity.Text("Order ID", data.orderId.orEmpty(), canCopy = true),
            FiatOrderDetailItemEntity.Text(
                "Order time",
                TimeUtil.getOrderTime(data.createTime),
                canCopy = false
            ),
            FiatOrderDetailItemEntity.Text("Account number", "?", canCopy = true),
            FiatOrderDetailItemEntity.Split,
        )
    }

    private fun txInfo(data: OrderDetailRes.Data): List<FiatOrderDetailItemEntity> {
        val txId = data.transactionId.orEmpty()
        if (txId.isEmpty()) {
            return listOf()
        }
        val state = data.state.orEmpty()

        return mutableListOf(
            FiatOrderDetailItemEntity.Group("Txs info"),
            FiatOrderDetailItemEntity.Text("Released Txs", txId),
            FiatOrderDetailItemEntity.Text("Status", data.state.orEmpty()),
        ).also {
            when (state) {
                FiatOrderState.COMPLETED -> {
                    FiatOrderDetailItemEntity.Text("Status", "Pending")
                }

                FiatOrderState.WITHDRAW_SUCCESS -> {
                    FiatOrderDetailItemEntity.Text("Status", "Success")
                }

                FiatOrderState.WITHDRAW_FAIL -> {
                    FiatOrderDetailItemEntity.Text("Status", "Failure")
                }
            }
        }
    }
}