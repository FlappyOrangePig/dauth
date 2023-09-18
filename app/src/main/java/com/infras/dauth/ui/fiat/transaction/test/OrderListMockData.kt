package com.infras.dauth.ui.fiat.transaction.test

import com.infras.dauth.entity.FiatOrderListItemEntity
import kotlinx.coroutines.delay

object OrderListMockData {

    suspend fun gen(): MutableList<FiatOrderListItemEntity> {
        delay(100)
        return listOf(
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
}