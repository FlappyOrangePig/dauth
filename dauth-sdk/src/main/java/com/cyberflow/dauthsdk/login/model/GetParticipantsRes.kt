package com.cyberflow.dauthsdk.login.model

import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.login.network.BaseResponse

class GetParticipantsRes(
    val data: Data,
) : BaseResponse() {
    class Data(val participants: List<Participant>)
    class Participant(
        val id: Int,
        val get_key_url: String,
        val set_key_url: String,
        val sign_url: String,
    ) {
        override fun toString(): String {
            return "Participant(id=$id, get_key_url='$get_key_url', set_key_url='$set_key_url', sign_url='$sign_url')"
        }

        fun isValid(): Boolean {
            return set_key_url.isNotEmpty() && get_key_url.isNotEmpty() && sign_url.isNotEmpty()
        }

        fun isGetAndSetValid(): Boolean {
            return set_key_url.isNotEmpty() && get_key_url.isNotEmpty()
        }
    }

    companion object {
        fun Participant.getHookedGetKeyUrl(): String {
            return if (DAuthSDK.impl.config.useBuiltInAppServerUrl) {
                "https://api-test.x3live.info/x/secret/open/get"
            } else {
                this.get_key_url
            }
        }

        fun Participant.getHookedSetKeyUrl(): String {
            return if (DAuthSDK.impl.config.useBuiltInAppServerUrl) {
                "https://api-test.x3live.info/x/secret/open/set"
            } else {
                this.set_key_url
            }
        }
    }
}



