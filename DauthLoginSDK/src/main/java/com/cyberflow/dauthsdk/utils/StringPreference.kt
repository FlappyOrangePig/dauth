package com.cyberflow.dauthsdk.utils

import kotlin.reflect.KProperty

class StringPreference(
    file: String,
    private val key: String,
    async: Boolean = true,
    private val defaultValue: String = ""
) : Preference<String>(file, key, async) {
    override fun getValue(nothing: Nothing?, property: KProperty<*>): String {
        return sp.getString(key, defaultValue).orEmpty()
    }
}