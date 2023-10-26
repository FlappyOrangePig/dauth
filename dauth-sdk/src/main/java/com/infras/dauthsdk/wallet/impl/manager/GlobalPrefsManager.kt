package com.infras.dauthsdk.wallet.impl.manager

import android.annotation.SuppressLint
import android.content.Context

class GlobalPrefsManager internal constructor(
    private val context: Context,
) {
    companion object {
        private const val FILE_GLOBAL_PREFS = "global_prefs"
        private const val KEY_PRE_GENERATED = "pre_generated"
        private const val KEY_SDK_VERSION = "sdk_version"
    }

    private val prefs get() = context.getSharedPreferences(FILE_GLOBAL_PREFS, Context.MODE_PRIVATE)

    @SuppressLint("ApplySharedPref")
    fun setKeyPreGenerated(value: Boolean) {
        val edit = prefs.edit()
        edit.putBoolean(KEY_PRE_GENERATED, value)
        edit.commit()
    }

    fun getKeyPreGenerated(): Boolean {
        return prefs.getBoolean(KEY_PRE_GENERATED, false)
    }

    @SuppressLint("ApplySharedPref")
    fun setSdkVersion(versionName: String) {
        val edit = prefs.edit()
        edit.putString(KEY_SDK_VERSION, versionName)
        edit.commit()
    }

    fun getVersion(): String {
        return prefs.getString(KEY_SDK_VERSION, "").orEmpty()
    }
}