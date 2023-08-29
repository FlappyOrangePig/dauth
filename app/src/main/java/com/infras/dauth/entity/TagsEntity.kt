package com.infras.dauth.entity

data class TagsEntity(
    val title: String,
    val selected: Boolean = false,
    val onClick: () -> Unit,
)