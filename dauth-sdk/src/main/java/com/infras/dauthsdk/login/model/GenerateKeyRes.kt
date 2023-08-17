package com.infras.dauthsdk.login.model

import com.infras.dauthsdk.login.network.BaseResponse
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class GenerateKeyRes(
    val data: Data
) : BaseResponse() {
    @JsonClass(generateAdapter = true)
    class Signaure(
        var m: String,
        var r: String,
        var s: String,
        var v: String,
    )
    @JsonClass(generateAdapter = true)
    class Data(
        val s0: String,
        val s1: String,
        val s2: String,
        val signaure: Signaure,
    )
}