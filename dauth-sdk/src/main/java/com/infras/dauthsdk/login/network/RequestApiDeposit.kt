package com.infras.dauthsdk.login.network

import com.infras.dauthsdk.api.IDepositApi
import com.infras.dauthsdk.login.infrastructure.ApiClient
import com.infras.dauthsdk.login.infrastructure.ReqUrl
import com.infras.dauthsdk.login.infrastructure.RequestConfig
import com.infras.dauthsdk.login.model.AccountDetailParam
import com.infras.dauthsdk.login.model.AccountDetailRes
import com.infras.dauthsdk.login.model.DigitalCurrencyListParam
import com.infras.dauthsdk.login.model.DigitalCurrencyListRes

internal class RequestApiDeposit internal constructor() : ApiClient(), IDepositApi {

    override suspend fun accountDetail(): AccountDetailRes? {
        val c = RequestConfig(ReqUrl.PathUrl("/deposit/v1/account/detail"))
        return request(c, AccountDetailParam())
    }

    override suspend fun currencyList(): DigitalCurrencyListRes? {
        val c = RequestConfig(ReqUrl.PathUrl("/deposit/v1/currency/list"))
        return request(c, DigitalCurrencyListParam())
    }
}