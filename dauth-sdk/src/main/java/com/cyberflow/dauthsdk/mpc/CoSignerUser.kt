package com.cyberflow.dauthsdk.mpc

import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import java.lang.IllegalStateException

private const val TAG = "CoSignerUser"

class CoSignerUser(
    private val logTag: String,
    private val msgHash: String,
    private val key: String,
    private val keyIndex: Int,
    private val remoteIndices: Int
) {
    private var contextHandler: Long? = null
    private val jni get() = DAuthJni.getInstance()

    fun startRemoveSign(): ByteArray {
        DAuthLogger.d("$logTag startRemoveSign", TAG)
        val outBuffer = ArrayList<ByteArray>()
        contextHandler = jni.remoteSignMsg(msgHash, key, keyIndex, intArrayOf(remoteIndices), outBuffer)
        return outBuffer.first()
    }

    fun signRound(input: ByteArray): Pair<Boolean, ByteArray> {
        DAuthLogger.d("$logTag signRound", TAG)
        val handle = contextHandler ?: throw IllegalStateException("please call startRemoveSign first")
        val remoteIndices1 = remoteIndices
        val outBuffer = ArrayList<ByteArray>()
        val finished = jni.remoteSignRound(handle, remoteIndices1, input, outBuffer)
        return if (finished) {
            true to outBuffer.first()
        } else {
            false to outBuffer.first()
        }
    }
}