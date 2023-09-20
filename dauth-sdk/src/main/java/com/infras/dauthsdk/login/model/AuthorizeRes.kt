package com.infras.dauthsdk.login.model

import com.infras.dauthsdk.login.network.BaseResponse
import com.squareup.moshi.JsonClass

/**
 * Authorize res
 *
 * @property data
 * @constructor Create empty Authorize res
 */
@JsonClass(generateAdapter = true)
data class AuthorizeRes(
    val data: Data
) : BaseResponse() {
    /**
     * Data
     *
     * @constructor Create empty Data
     */
    @JsonClass(generateAdapter = true)
    class Data {
        var code: String? = null
    }
}

