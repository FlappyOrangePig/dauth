package com.infras.dauth.repository

import com.infras.dauth.manager.AccountManager
import com.infras.dauthsdk.login.model.AccountOpenParam
import com.infras.dauthsdk.login.model.CurrencyPriceParam
import com.infras.dauthsdk.login.model.OrderCreateParam
import com.infras.dauthsdk.login.model.PaymentQuoteParam

class FiatTxRepository {

    companion object {
        private const val TAG = "FiatTxRepository"
    }

    val api get() = AccountManager.sdk.getDepositApi()

    suspend fun accountDetail() = api.accountDetail()

    suspend fun currencyList() = api.currencyList()

    suspend fun accountOpen(p: AccountOpenParam) = api.accountOpen(p)

    suspend fun orderCreate(p: OrderCreateParam) = api.orderCreate(p)

    suspend fun paymentQuote(p: PaymentQuoteParam) = api.paymentQuote(p)

    suspend fun currencyPrice(p: CurrencyPriceParam) = api.currencyPrice(p)
}