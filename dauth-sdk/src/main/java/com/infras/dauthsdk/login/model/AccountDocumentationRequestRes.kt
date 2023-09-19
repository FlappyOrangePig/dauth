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
     * @property country
     * @property idTypeList
     * @constructor Create empty Data
     */
    class Data(
        val country: String,
        @Json(name = "id_type_list")
        val idTypeList: List<IdTypeInfo>
    )

    /**
     * Id type info
     *
     * @property idType 证件类型 PASSPORT：护照 DRIVERS：驾照 ID_CARD：⾝份证
     * @property sideNum
     * @constructor Create empty Id type info
     */
    class IdTypeInfo(
        @Json(name = "id_type")
        val idType: String,
        @Json(name = "side_num")
        val sideNum: Int,
    ) {
        companion object {
            const val PASSPORT = "PASSPORT"
            const val DRIVERS = "DRIVERS"
            const val ID_CARD = "ID_CARD"
        }
    }
}