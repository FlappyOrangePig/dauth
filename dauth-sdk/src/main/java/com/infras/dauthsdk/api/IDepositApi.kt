package com.infras.dauthsdk.api

import com.infras.dauthsdk.login.model.AccountDetailRes
import com.infras.dauthsdk.login.model.DigitalCurrencyListRes

interface IDepositApi {
    suspend fun accountDetail(): AccountDetailRes?
    suspend fun currencyList(): DigitalCurrencyListRes?
}