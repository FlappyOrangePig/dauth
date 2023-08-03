package com.infras.dauthsdk.api.entity


/**
 * Login result data
 *
 * @constructor Create empty Login result data
 */
sealed class LoginResultData {

    /**
     * Success
     *
     * @property needCreateWallet
     * @property accessToken
     * @property openId
     * @constructor Create empty Success
     */
    class Success(
        val needCreateWallet: Boolean,
        val openId: String,
        val accessToken: String,
    ) : LoginResultData()

    /**
     * Failure
     *
     * @property code
     * @constructor Create empty Failure
     */
    class Failure(val code: Int?) : LoginResultData()
}