package com.cyberflow.dauthsdk.login.model

import com.cyberflow.dauthsdk.login.network.BaseResponse
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class ParticipantsRes(
    val data: @Contextual Data? = null
) : BaseResponse() {
    @Serializable
    class Data {
        val list: List<Participants>? = null
    }
}
