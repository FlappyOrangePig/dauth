package com.cyberflow.dauthsdk.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.cyberflow.dauthsdk.CyberFlowApplication

import kotlin.reflect.KProperty

abstract class Preference<T> constructor(
    private val file: String,
    private val key: String,
    private val async: Boolean,
) {
    protected val sp: SharedPreferences
        get() = CyberFlowApplication.instance.getSharedPreferences(
            file,
            Context.MODE_PRIVATE
        )

    abstract operator fun getValue(nothing: Nothing?, property: KProperty<*>): T

    @SuppressLint("ApplySharedPref")
    operator fun setValue(nothing: Nothing?, property: KProperty<*>, s: T) {
        val editor = sp.edit().putString(key, s.toString())
        if (async) {
            editor.apply()
        } else {
            editor.commit()
        }
    }
}