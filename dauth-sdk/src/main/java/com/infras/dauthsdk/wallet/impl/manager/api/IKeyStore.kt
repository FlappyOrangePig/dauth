package com.infras.dauthsdk.wallet.impl.manager.api

interface IKeyStore {

    fun getAllKeys(): List<String>

    fun setAllKeys(keys: List<String>)

    fun setLocalKey(key: String)

    fun getLocalKey(): String

    fun clear()

    fun releaseTempKeys()
}