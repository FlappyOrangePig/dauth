package com.infras.dauthsdk.api

import com.infras.dauthsdk.login.model.AccountDetailRes
import com.infras.dauthsdk.login.model.AccountDocumentationRequestParam
import com.infras.dauthsdk.login.model.AccountDocumentationRequestRes
import com.infras.dauthsdk.login.model.AccountOpenParam
import com.infras.dauthsdk.login.model.AccountOpenRes
import com.infras.dauthsdk.login.model.CurrencyPriceParam
import com.infras.dauthsdk.login.model.CurrencyPriceRes
import com.infras.dauthsdk.login.model.DigitalCurrencyListRes
import com.infras.dauthsdk.login.model.OrderCreateParam
import com.infras.dauthsdk.login.model.OrderCreateRes
import com.infras.dauthsdk.login.model.PaymentQuoteParam
import com.infras.dauthsdk.login.model.PaymentQuoteRes

/**
 * I deposit api
 *
 * @constructor Create empty I deposit api
 */
interface IDepositApi {
    /**
     * Account detail
     *
     * @return
     */
    suspend fun accountDetail(): AccountDetailRes?

    /**
     * Currency list
     *
     * @return
     */
    suspend fun currencyList(): DigitalCurrencyListRes?

    /**
     * Account documentation request
     *
     * @param param
     * @return
     */
    suspend fun accountDocumentationRequest(param: AccountDocumentationRequestParam): AccountDocumentationRequestRes?

    /**
     * Account open
     *
     * @param p
     * @return
     */
    suspend fun accountOpen(p: AccountOpenParam): AccountOpenRes?

    /**
     * Order create
     *
     * @param p
     * @return
     */
    suspend fun orderCreate(p: OrderCreateParam): OrderCreateRes?

    /**
     * Payment quote
     *
     * @param p
     * @return
     */
    suspend fun paymentQuote(p: PaymentQuoteParam): PaymentQuoteRes?

    /**
     * Currency price
     *
     * @param p
     * @return
     */
    suspend fun currencyPrice(p: CurrencyPriceParam): CurrencyPriceRes?
}