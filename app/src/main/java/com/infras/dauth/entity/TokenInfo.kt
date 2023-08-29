package com.infras.dauth.entity

data class TokenInfo(
    val name: String,
    val issuer: String,
    val avatarUrl: String,
    val changeRange: String,
    val price: String,
)