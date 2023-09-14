package com.infras.dauth.entity

import android.os.Parcelable
import com.infras.dauthsdk.login.model.DigitalCurrencyListRes
import kotlinx.parcelize.Parcelize

@Parcelize
class BuyWithPageInputEntity(
    val crypto_info: DigitalCurrencyListRes.CryptoInfo,
    val fiat_info: DigitalCurrencyListRes.FiatInfo,
    val buyAmount: String
) : Parcelable