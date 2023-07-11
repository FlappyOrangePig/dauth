package com.cyberflow.dauthsdk.mpc

import com.cyberflow.dauthsdk.login.model.GetParticipantsRes
import com.cyberflow.dauthsdk.login.network.RequestApiMpc
import com.cyberflow.dauthsdk.login.utils.LoginPrefs
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * MPC服务端信息，只在内存中缓存
 */
object MpcServers {

    @Volatile
    private var servers: GetParticipantsRes.Data? = null
    private val mutex = Mutex()

    suspend fun getServers() = servers ?: mutex.withLock {
        servers ?: fetchServers().also { servers = it }
    }

    private suspend fun fetchServers(): GetParticipantsRes.Data? {
        val r = RequestApiMpc().getParticipants()
        return if (r != null && r.isSuccess()) {
            r.data
        } else {
            null
        }
    }
}