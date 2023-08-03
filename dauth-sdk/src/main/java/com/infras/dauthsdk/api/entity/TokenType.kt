package com.infras.dauthsdk.api.entity

sealed class TokenType {
    object Eth : TokenType()

    /**
     * 同质化代币如：USDT
     */
    class ERC20(val contractAddress: String) : TokenType()

    /**
     * 非同质化代币
     */
    class ERC721(val contractAddress: String) : TokenType()
}