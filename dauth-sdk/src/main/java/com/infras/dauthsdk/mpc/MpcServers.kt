package com.infras.dauthsdk.mpc

import com.infras.dauthsdk.login.model.GetParticipantsRes
import com.infras.dauthsdk.wallet.impl.manager.Managers
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
        val r = Managers.requestApiMpc.getParticipants()
        return if (r != null && r.isSuccess()) {
            r.data
        } else {
            null
        }
    }
}