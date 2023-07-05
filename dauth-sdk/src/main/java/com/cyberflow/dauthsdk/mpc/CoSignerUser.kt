package com.cyberflow.dauthsdk.mpc

import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.mpc.entity.JniOutBuffer
import kotlin.jvm.Throws

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

    @Throws(IllegalStateException::class)
    fun signRound(input: ByteArray): Pair<Boolean, ByteArray> {
        DAuthLogger.d("$logTag signRound", TAG)
        val handle = contextHandler ?: throw IllegalStateException("please call startRemoveSign first")
        val outBuffer = ArrayList<JniOutBuffer>()
        return when (jni.remoteSignRound(handle, remote, input, outBuffer)) {
            1 -> true to jni.getSignature(handle).toByteArray()
            0 -> false to outBuffer.first().bytes
            else -> throw IllegalStateException("remote key mismatch")
        }
    }
}