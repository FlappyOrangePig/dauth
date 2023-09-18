package com.infras.dauth.ui.fiat.transaction.util

import com.infras.dauth.entity.FiatOrderDetailItemEntity
import com.infras.dauth.entity.FiatOrderState
import com.infras.dauthsdk.login.model.OrderDetailRes

object OrderDetailListComposeUtil {

    fun all(title: FiatOrderDetailItemEntity.Title, data: OrderDetailRes.Data) =
        listOf(title) + publicInfo(data) + txInfo(data)

    private fun publicInfo(data: OrderDetailRes.Data) = mutableListOf(
        FiatOrderDetailItemEntity.Group("Buy ?"),
        FiatOrderDetailItemEntity.Text("Unit Price", data.price.orEmpty()),
        FiatOrderDetailItemEntity.Text("Quantity", data.quantity.orEmpty()),
        FiatOrderDetailItemEntity.Text("Order amount", data.amount.orEmpty()),
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