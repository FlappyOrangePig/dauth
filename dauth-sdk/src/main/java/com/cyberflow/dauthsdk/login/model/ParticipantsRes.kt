package com.cyberflow.dauthsdk.login.model

import com.cyberflow.dauthsdk.login.network.BaseResponse
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ParticipantsRes(
    val data: Data? = null
) : BaseResponse() {
    @JsonClass(generateAdapter = true)
    class Data {
        var list: List<Participants>? = null
    }
}
