package com.infras.dauth.manager

import android.annotation.SuppressLint

@SuppressLint("StaticFieldLeak")
internal object AppManagers {
    lateinit var resourceManager: ResourceManager
        private set
    lateinit var storageManager: StorageManager
        private set

    fun attach(
        resourceManager: ResourceManager,
        storageManager: StorageManager,
    ) {
        this.resourceManager = resourceManager
        this.storageManager = storageManager
    }
}