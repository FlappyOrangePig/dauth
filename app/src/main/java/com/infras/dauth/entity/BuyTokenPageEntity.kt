package com.infras.dauth.entity

data class BuyTokenPageEntity(
    val isAmountMode: Boolean = false,
    val estimatedPrice: String = "",
    val inputValue: String = "0",
    val cryptoCode: String = "",
    val fiatCode: String = "",
)