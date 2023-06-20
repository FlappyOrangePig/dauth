package com.cyberflow.dauthsdk.mpc

object MpcKeyIds {
    const val KEY_INDEX_LOCAL = 0
    const val KEY_INDEX_DAUTH_SERVER = 1
    const val KEY_INDEX_APP_SERVER = 2

    private fun getIndexes(): IntArray {
        return intArrayOf(KEY_INDEX_LOCAL, KEY_INDEX_DAUTH_SERVER, KEY_INDEX_APP_SERVER)
    }

    /**
     * 直接用索引作签名的ID
     */
    private fun getKeyId(index: Int): String {
        return "$index"
    }

    fun getKeyIds() = getIndexes().map {
        getKeyId(it)
    }.toTypedArray()

    fun getLocalId(): String {
        return getKeyId(KEY_INDEX_LOCAL)
    }

    fun getRemoteIdsToSign(): String {
        return getKeyId(KEY_INDEX_DAUTH_SERVER)
    }

    fun getIdsToSign(): Array<String> {
        return arrayOf(getLocalId(), getKeyId(KEY_INDEX_DAUTH_SERVER))
    }
}