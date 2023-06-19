package com.cyberflow.dauthsdk.login.model

import com.cyberflow.dauthsdk.login.network.BaseResponse

data class ParticipantsRes(
    val data: Data? = null
) : BaseResponse() {
    class Data {
        val list: List<Participants>? = null
    }
}
