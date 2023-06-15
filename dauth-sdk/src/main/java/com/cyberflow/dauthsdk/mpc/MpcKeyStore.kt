package com.cyberflow.dauthsdk.mpc

import com.cyberflow.dauthsdk.api.DAuthSDK

private const val FILE = "mpc_keystore"

object MpcKeyStore {

    private val context get() = DAuthSDK.impl.context
    private val file get() = context.getSharedPreferences(FILE, 0)

    fun getAllKeys(): List<String> {
        val intArray = arrayListOf(0, 1, 2)
        val keyInSp = intArray.mapNotNull {
            file.getString(it.toString(), null)
        }
        return keyInSp
    }

    fun setAllKeys(keys: List<String>) {
        file.edit().let { et ->
            keys.forEachIndexed { index, s ->
                et.putString(index.toString(), s)
            }
            et.apply()
        }
    }

    fun clear() {
        file.edit().clear().apply()
    }
}