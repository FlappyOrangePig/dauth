package com.cyberflow.dauthsdk.mpc

import android.annotation.SuppressLint
import com.cyberflow.dauthsdk.wallet.ext.app

private const val FILE = "mpc_keystore"

object MpcKeyStore {

    private val context get() = app()
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
            et.commit()
        }
    }

    fun setLocalKey(key: String) {
        file.edit().let { et ->
            et.clear()
            et.putString(0.toString(), key)
            et.commit()
        }
    }

    fun getLocalKey(): String {
        return file.getString(0.toString(), null).orEmpty()/*.replace("0", "1")*/
    }

    @SuppressLint("ApplySharedPref")
    fun clear() {
        file.edit().clear().commit()
    }
}