package com.cyberflow.dauthsdk.mpc

import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.mpc.entity.JniOutBuffer

private const val TAG = "CoSignerUser"

class CoSignerUser(
    private val logTag: String,
    private val msgHash: String,
    private val key: String,
    private val local: String,
    private val remote: String
) {
    private var contextHandler: Long? = null
    private val jni get() = DAuthJni.getInstance()

    fun startRemoveSign(): ByteArray {
        DAuthLogger.d("$logTag startRemoveSign", TAG)
        val outBuffer = ArrayList<JniOutBuffer>()
        contextHandler = jni.remoteSignMsg(msgHash, key, local, arrayOf(remote), outBuffer)
        return outBuffer.first().bytes
    }

    fun signRound(input: ByteArray): Pair<Boolean, ByteArray> {
        DAuthLogger.d("$logTag signRound", TAG)
        val handle = contextHandler ?: throw IllegalStateException("please call startRemoveSign first")
        val outBuffer = ArrayList<JniOutBuffer>()
        val finished = jni.remoteSignRound(handle, remote, input, outBuffer)
        return if (finished) {
            true to jni.getSignature(handle).toByteArray()
        } else {
            false to outBuffer.first().bytes
        }
    }
}