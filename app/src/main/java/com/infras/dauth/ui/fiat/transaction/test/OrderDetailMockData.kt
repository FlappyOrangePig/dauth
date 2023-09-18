package com.infras.dauth.ui.fiat.transaction.test

import com.infras.dauth.R
import com.infras.dauth.entity.FiatOrderDetailItemEntity

object OrderDetailMockData {

    fun getDetailUIData(): MutableList<FiatOrderDetailItemEntity> {
        return mutableListOf(
            FiatOrderDetailItemEntity.Title(R.drawable.svg_ic_order_complete, "Pending for your payment", "Pay within 19:59s"),
            FiatOrderDetailItemEntity.Group("Buy USDT"),
            FiatOrderDetailItemEntity.Text("Order ID", "230817151844605", canCopy = true),
            FiatOrderDetailItemEntity.Text("Unit Price", "\$ 1.01"),
            FiatOrderDetailItemEntity.Text("Quantity", "1,888.00 USDT"),
            FiatOrderDetailItemEntity.Text("Order amount", "\$1,906.86"),
            FiatOrderDetailItemEntity.Text("Payment method", "AdvCash"),
            FiatOrderDetailItemEntity.Split,
            FiatOrderDetailItemEntity.Tips(
                cost = "\$1,906.86",
                accountName = "John Wick",
                accountNumber = "456638747627",
                imagePath = "",
            ),
        )
    }
}