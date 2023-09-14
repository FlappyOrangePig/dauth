package com.infras.dauthsdk.login.network

import com.infras.dauthsdk.api.IDepositApi
import com.infras.dauthsdk.login.infrastructure.ApiClient
import com.infras.dauthsdk.login.infrastructure.ReqUrl
import com.infras.dauthsdk.login.infrastructure.RequestConfig
import com.infras.dauthsdk.login.model.AccountDetailParam
import com.infras.dauthsdk.login.model.AccountDetailRes
import com.infras.dauthsdk.login.model.AccountDocumentationRequestParam
import com.infras.dauthsdk.login.model.AccountDocumentationRequestRes
import com.infras.dauthsdk.login.model.AccountOpenParam
import com.infras.dauthsdk.login.model.AccountOpenRes
import com.infras.dauthsdk.login.model.CurrencyPriceParam
import com.infras.dauthsdk.login.model.CurrencyPriceRes
import com.infras.dauthsdk.login.model.DigitalCurrencyListParam
import com.infras.dauthsdk.login.model.DigitalCurrencyListRes
import com.infras.dauthsdk.login.model.OrderCreateParam
import com.infras.dauthsdk.login.model.OrderCreateRes
import com.infras.dauthsdk.login.model.PaymentQuoteParam
import com.infras.dauthsdk.login.model.PaymentQuoteRes

internal class RequestApiDeposit internal constructor() : ApiClient(), IDepositApi {

    override suspend fun accountDetail(): AccountDetailRes? {
        val c = RequestConfig(ReqUrl.PathUrl("/deposit/v1/account/detail"))
        return request(c, AccountDetailParam())
    }

    override suspend fun currencyList(): DigitalCurrencyListRes? {
        val c = RequestConfig(ReqUrl.PathUrl("/deposit/v1/currency/list"))
        return request(c, DigitalCurrencyListParam())
    }

    override suspend fun accountDocumentationRequest(param: AccountDocumentationRequestParam): AccountDocumentationRequestRes? {
        val c = RequestConfig(ReqUrl.PathUrl("/deposit/v1/account/doc/request"))
        return request(c, param)
    }

    override suspend fun accountOpen(p: AccountOpenParam): AccountOpenRes? {
        val c = RequestConfig(ReqUrl.PathUrl("/deposit/v1/account/open"))
        return request(c, p)
    }

    override suspend fun orderCreate(p: OrderCreateParam): OrderCreateRes? {
        val c = RequestConfig(ReqUrl.PathUrl("/deposit/v1/order/create"))
        return request(c, p)
    }

    override suspend fun paymentQuote(p: PaymentQuoteParam): PaymentQuoteRes? {
        val c = RequestConfig(ReqUrl.PathUrl("/deposit/v1/payment/quote"))
        return request(c, p)
    }

    override suspend fun currencyPrice(p: CurrencyPriceParam): CurrencyPriceRes? {
        val c = RequestConfig(ReqUrl.PathUrl("/deposit/v1/currency/price"))
        return request(c, p)
    }
}