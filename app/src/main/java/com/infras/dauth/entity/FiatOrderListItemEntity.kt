package com.infras.dauth.entity

data class FiatOrderListItemEntity(
    val orderId: String,
    val title: String,
    val unitPrice: String,
    val quantity: String,
    val totalPrice: String,
    val time: String,
    val state: String,
)