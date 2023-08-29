package com.infras.dauth.entity

data class TokenInfoOfTag(
    val tag: TagsEntity,
    val tokenInfoList: List<TokenInfo>
)