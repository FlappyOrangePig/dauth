package com.infras.dauth.entity

import com.infras.dauthsdk.login.model.DigitalCurrencyListRes

data class BuyAndSellPageEntity(
    val buyTab: List<TokenInfoOfTag>,
    val fiatList: List<DigitalCurrencyListRes.FiatInfo>,
    var fiatSelectIndex: Int?,
) {
    data class TokenInfoOfTag(
        val tag: TagsEntity,
        val tokenInfoList: List<TokenInfo>
    )

    data class TagsEntity(
        val title: String,
        val selected: Boolean = false,
        val onClick: () -> Unit,
    )

    data class TokenInfo(
        val name: String,
        val issuer: String,
        val avatarUrl: String,
        val changeRange: String,
        val price: String,
        val crypto: DigitalCurrencyListRes.CryptoInfo,
    )
}

