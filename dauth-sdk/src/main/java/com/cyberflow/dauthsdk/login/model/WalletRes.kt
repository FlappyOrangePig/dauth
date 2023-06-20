package com.cyberflow.dauthsdk.login.model

import kotlinx.serialization.Serializable

@Serializable
data class WalletRes(val name: String, val address: String) {
}