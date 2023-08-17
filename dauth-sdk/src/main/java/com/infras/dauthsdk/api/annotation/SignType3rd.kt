package com.infras.dauthsdk.api.annotation

import androidx.annotation.StringDef

/**
 * Sign type3rd
 *
 * @constructor Create empty Sign type3rd
 */
@Retention(value = AnnotationRetention.SOURCE)
@StringDef(
    SignType3rd.GOOGLE,
    SignType3rd.TWITTER,
)
annotation class SignType3rd {
    companion object {
        const val GOOGLE = "GOOGLE"
        const val TWITTER = "TWITTER"
    }
}