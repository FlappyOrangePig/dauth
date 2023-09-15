package com.infras.dauthsdk.login.model

class OrderCreateParam(
    val trade_model: String, // 交易模式：FREE，FAST，FIAT
    val quote_type: String, // 报价类型：AMOUNT，QUANTITY
    val fiat_code: String,
    val crypto_code: String,
    val amount: String,
    val paymethod_id: String,
    val withdraw_address: String,
    val chain_id: String,
) : IAccessTokenRequest, IAuthorizationRequest