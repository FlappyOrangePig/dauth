package com.cyberflow.dauthsdk.login.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalletRes(val name: String, val address: String) {
}