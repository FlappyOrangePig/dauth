package com.infras.dauth.repository

import com.infras.dauth.manager.AccountManager
import com.infras.dauthsdk.api.IFiatApi
import com.infras.dauthsdk.login.model.AccountDocumentationRequestParam
import com.infras.dauthsdk.login.model.AccountOpenParam
import com.infras.dauthsdk.login.model.CountryListParam
import com.infras.dauthsdk.login.model.CurrencyPriceParam
import com.infras.dauthsdk.login.model.OrderCreateParam
import com.infras.dauthsdk.login.model.OrderDetailParam
import com.infras.dauthsdk.login.model.OrderDetailRes
import com.infras.dauthsdk.login.model.OrderListParam
import com.infras.dauthsdk.login.model.PaymentQuoteParam

class FiatTxRepository: IFiatApi {

    companion object {
        private const val TAG = "FiatTxRepository"
        const val PAGE_SIZE = 100
    }

    val api get() = AccountManager.sdk.getFiatApi()

    override suspend fun accountDetail() = api.accountDetail()

    override suspend fun currencyList() = api.currencyList()

    override suspend fun accountDocumentationRequest(p: AccountDocumentationRequestParam) =
        api.accountDocumentationRequest(p)

    override suspend fun accountOpen(p: AccountOpenParam) = api.accountOpen(p)

    override suspend fun orderCreate(p: OrderCreateParam) = api.orderCreate(p)

    override suspend fun paymentQuote(p: PaymentQuoteParam) = api.paymentQuote(p)

    override suspend fun currencyPrice(p: CurrencyPriceParam) = api.currencyPrice(p)

    override suspend fun orderList(p: OrderListParam) = api.orderList(p)

    override suspend fun countryList(p: CountryListParam) = api.countryList(p)

    override suspend fun orderDetail(p: OrderDetailParam) = api.orderDetail(p)
}