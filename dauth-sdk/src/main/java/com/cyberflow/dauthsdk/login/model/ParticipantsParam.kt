package com.cyberflow.dauthsdk.login.model

import kotlinx.serialization.Serializable

@Serializable
data class ParticipantsParam(
    val access_token: String,
    val authid: String
) {

}