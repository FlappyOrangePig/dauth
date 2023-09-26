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
import com.infras.dauthsdk.login.model.OrderAppealParam
import com.infras.dauthsdk.login.model.OrderAppealRes
import com.infras.dauthsdk.login.model.OrderCancelAppealParam
import com.infras.dauthsdk.login.model.OrderCancelAppealRes
import com.infras.dauthsdk.login.model.OrderCancelParam
import com.infras.dauthsdk.login.model.OrderCancelRes
import com.infras.dauthsdk.login.model.OrderCreateParam
import com.infras.dauthsdk.login.model.OrderCreateRes
import com.infras.dauthsdk.login.model.OrderDetailParam
import com.infras.dauthsdk.login.model.OrderDetailRes
import com.infras.dauthsdk.login.model.OrderListParam
import com.infras.dauthsdk.login.model.OrderListRes
import com.infras.dauthsdk.login.model.OrderPaidParam
import com.infras.dauthsdk.login.model.OrderPaidRes
import com.infras.dauthsdk.login.model.PaymentQuoteParam
import com.infras.dauthsdk.login.model.PaymentQuoteRes
import com.infras.dauthsdk.login.model.QueryWithdrawConfParam
import com.infras.dauthsdk.login.model.QueryWithdrawConfRes

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

    /**
     * 订单详情
     *
     * @param p
     * @return
     */
    suspend fun orderDetail(p: OrderDetailParam): OrderDetailRes?

    /**
     * 我已支付
     *
     * @param p
     * @return
     */
    suspend fun orderPaid(p: OrderPaidParam): OrderPaidRes?

    /**
     * 订单申诉
     *
     * @param p
     * @return
     */
    suspend fun orderAppeal(p: OrderAppealParam): OrderAppealRes?

    /**
     * 订单取消
     *
     * @param p
     * @return
     */
    suspend fun orderCancel(p: OrderCancelParam): OrderCancelRes?

    /**
     * 取消申诉
     *
     * @param p
     * @return
     */
    suspend fun orderCancelAppeal(p: OrderCancelAppealParam): OrderCancelAppealRes?

    /**
     * 查询提币配置
     *
     * @param p
     * @return
     */
    suspend fun queryWithdrawConf(p: QueryWithdrawConfParam): QueryWithdrawConfRes?
}