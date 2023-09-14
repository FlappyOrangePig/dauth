package com.infras.dauth.repository

import com.infras.dauth.manager.AccountManager
import com.infras.dauthsdk.login.model.AccountOpenParam

class FiatTxRepository {

    companion object {
        private const val TAG = "FiatTxRepository"
    }

    val api get() = AccountManager.sdk.getDepositApi()

    suspend fun accountDetail() = api.accountDetail()

    suspend fun currencyList() = api.currencyList()

    suspend fun accountOpen(param: AccountOpenParam) = api.accountOpen(param)
}