package com.infras.dauthsdk.wallet.impl.manager

import android.annotation.SuppressLint
import android.content.Context

class GlobalPrefsManager internal constructor(
    private val context: Context,
) {
    companion object {
        private const val FILE_GLOBAL_PREFS = "global_prefs"
        private const val KEY_PRE_GENERATED = "pre_generated"
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
}