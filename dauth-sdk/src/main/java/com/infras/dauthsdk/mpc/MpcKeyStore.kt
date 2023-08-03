package com.infras.dauthsdk.mpc

import android.annotation.SuppressLint
import android.content.Context
import com.infras.dauthsdk.login.utils.DAuthLogger

private const val TAG = "MpcKeyStore"
private const val FILE = "mpc_keystore"
private const val KEY_MERGE_RESULT = "key_merge_result"

class MpcKeyStore internal constructor(private val context: Context) {
    private val file get() = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun getAllKeys(): List<String> {
        val intArray = arrayListOf(0, 1, 2)
        val keyInSp = intArray.mapNotNull {
            file.getString(it.toString(), null)
        }
        return keyInSp
    }

    fun setAllKeys(keys: List<String>) {
        DAuthLogger.d("setAllKeys ${keys.map { it.length }}", TAG)
        file.edit().let { et ->
            MpcKeyIds.getKeyIds().forEach { et.remove(it) }
            keys.forEachIndexed { index, s ->
                et.putString(index.toString(), s)
            }
            et.commit()
        }
    }

    fun setLocalKey(key: String) {
        DAuthLogger.d("setLocalKey ${key.length}", TAG)
        file.edit().let { et ->
            MpcKeyIds.getKeyIds().forEach { et.remove(it) }
            et.putString(MpcKeyIds.KEY_INDEX_LOCAL.toString(), key)
            et.commit()
        }
    }

    fun getLocalKey(): String {
        return file.getString(MpcKeyIds.getLocalId(), null).orEmpty()/*.replace("0", "1")*/
    }

    @SuppressLint("ApplySharedPref")
    fun clear() {
        DAuthLogger.d("clear" , TAG)
        file.edit().clear().commit()
    }

    @SuppressLint("ApplySharedPref")
    fun setMergeResult(mr: String) {
        DAuthLogger.d("setMergeResult ${mr.length}" , TAG)
        val editor = file.edit()
        editor.putString(KEY_MERGE_RESULT, mr)
        editor.commit()
    }

    fun getMergeResult(): String {
        return file.getString(KEY_MERGE_RESULT, null).orEmpty()
    }

    @SuppressLint("ApplySharedPref")
    fun releaseTempKeys() {
        DAuthLogger.d("releaseTempKeys" , TAG)
        val editor = file.edit()
        editor.remove(KEY_MERGE_RESULT)
        editor.remove(MpcKeyIds.KEY_INDEX_DAUTH_SERVER.toString())
        editor.remove(MpcKeyIds.KEY_INDEX_APP_SERVER.toString())
        editor.commit()
    }
}