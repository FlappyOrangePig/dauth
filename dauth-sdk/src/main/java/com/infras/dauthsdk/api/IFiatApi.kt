package com.infras.dauthsdk.api

import com.infras.dauthsdk.login.model.AccountDetailRes
import com.infras.dauthsdk.login.model.AccountDocumentationRequestParam
import com.infras.dauthsdk.login.model.AccountDocumentationRequestRes
import com.infras.dauthsdk.login.model.AccountOpenParam
import com.infras.dauthsdk.login.model.AccountOpenRes
import com.infras.dauthsdk.login.model.CountryListParam
import com.infras.dauthsdk.login.model.CountryListRes
import com.infras.dauthsdk.login.model.CurrencyPriceParam
import com.infras.dauthsdk.login.model.CurrencyPriceRes
import com.infras.dauthsdk.login.model.DigitalCurrencyListRes
import com.infras.dauthsdk.login.model.OrderCreateParam
import com.infras.dauthsdk.login.model.OrderCreateRes
import com.infras.dauthsdk.login.model.OrderListParam
import com.infras.dauthsdk.login.model.OrderListRes
import com.infras.dauthsdk.login.model.PaymentQuoteParam
import com.infras.dauthsdk.login.model.PaymentQuoteRes

/**
 * I fiat api
 *
 * @constructor Create empty I fiat api
 */
interface IFiatApi {
    /**
     * 账户状态查询
     *
     * @return
     */
    suspend fun accountDetail(): AccountDetailRes?

    /**
     * 货币列表
     *
     * @return
     */
    suspend fun currencyList(): DigitalCurrencyListRes?

    /**
     * Account documentation request
     *
     * @param p
     * @return
     */
    suspend fun accountDocumentationRequest(p: AccountDocumentationRequestParam): AccountDocumentationRequestRes?

    /**
     * KYC开户
     *
     * @param p
     * @return
     */
    suspend fun accountOpen(p: AccountOpenParam): AccountOpenRes?

    /**
     * 创建订单
     *
     * @param p
     * @return
     */
    suspend fun orderCreate(p: OrderCreateParam): OrderCreateRes?

    /**
     * 单一币种实时价格
     *
     * @param p
     * @return
     */
    suspend fun paymentQuote(p: PaymentQuoteParam): PaymentQuoteRes?

    /**
     * 非实时价格批量查询
     *
     * @param p
     * @return
     */
    suspend fun currencyPrice(p: CurrencyPriceParam): CurrencyPriceRes?

    /**
     * 订单列表
     *
     * @param p
     * @return
     */
    suspend fun orderList(p: OrderListParam): OrderListRes?

    /**
     * 国家列表
     *
     * @param p
     * @return
     */
    suspend fun countryList(p: CountryListParam): CountryListRes?
}