package com.infras.dauthsdk.login.model

import com.infras.dauthsdk.login.network.BaseResponse
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
