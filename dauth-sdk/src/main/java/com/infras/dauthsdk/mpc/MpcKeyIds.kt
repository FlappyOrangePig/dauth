package com.infras.dauthsdk.mpc

/**
 * MPC角色标识映射
 * sdk内部使用index来标记签名节点是什么角色，而服务端和so使用id来标记角色
 */
internal object MpcKeyIds {
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

    fun getDAuthId(): String {
        return getKeyId(KEY_INDEX_DAUTH_SERVER)
    }

    fun getAppId(): String {
        return getKeyId(KEY_INDEX_APP_SERVER)
    }

    fun getRemoteIdsToSign(): String {
        return getKeyId(KEY_INDEX_DAUTH_SERVER)
    }
}