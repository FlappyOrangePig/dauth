package com.infras.dauth.entity

import com.infras.dauthsdk.login.model.DigitalCurrencyListRes

class PayMethodChooseListEntity(
    val payMethodInfo: DigitalCurrencyListRes.PayMethodInfo,
    val price: String,
    @Transient
    var isSelected: Boolean = false
)