package com.infras.dauthsdk.api

import com.infras.dauthsdk.login.model.AccountDetailRes
import com.infras.dauthsdk.login.model.AccountDocumentationRequestParam
import com.infras.dauthsdk.login.model.AccountDocumentationRequestRes
import com.infras.dauthsdk.login.model.AccountOpenParam
import com.infras.dauthsdk.login.model.AccountOpenRes
import com.infras.dauthsdk.login.model.DigitalCurrencyListRes

interface IDepositApi {
    suspend fun accountDetail(): AccountDetailRes?
    suspend fun currencyList(): DigitalCurrencyListRes?
    suspend fun accountDocumentationRequest(param: AccountDocumentationRequestParam): AccountDocumentationRequestRes?
    suspend fun accountOpen(param: AccountOpenParam): AccountOpenRes?
}