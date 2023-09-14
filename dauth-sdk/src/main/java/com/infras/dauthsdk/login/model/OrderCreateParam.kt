package com.infras.dauthsdk.login.model

class OrderCreateParam(
    val quote_asset_name: String,
    val quote_type: String, // 报价类型：AMOUNT，QUANTITY
    val asset_type: String, // 报价资产类型：FIAT，CRYPTO
    val asset_name: String,
    val trade_type: String, // 交易资产类型：FIAT，CRYPTO
    val amount: String,
    val paymethod_id: String,
    val withdraw_address: String,
    val chain_id: String,
) : IAccessTokenRequest, IAuthorizationRequest