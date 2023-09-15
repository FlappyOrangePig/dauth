package com.infras.dauthsdk.login.model

import com.infras.dauthsdk.login.network.BaseResponse
import com.squareup.moshi.Json

/**
 * Account documentation request res
 *
 * @property data
 * @constructor Create empty Account documentation request res
 */
class AccountDocumentationRequestRes(
    val data: Data?,
) : BaseResponse() {
    /**
     * Data
     *
     * @property idType 证件类型 1：护照 2：驾照 4：⾝份证
     * @property sideNum 证件正反⾯， 0、1、2 ⾯
     * @constructor Create empty Data
     */
    class Data(
        @Json(name = "id_type")
        val idType: Int,
        @Json(name = "side_num")
        val sideNum: Int,
    )
}