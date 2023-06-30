package com.cyberflow.dauthsdk.login.model

import com.cyberflow.dauthsdk.login.network.BaseResponse

class GetParticipantsRes(
    val data: Data,
): BaseResponse() {
    class Data(val participants: Array<Participant>)
    class Participant(
        val id: Int,
        val key_url: String,
        val sign_url: String,
    )
}