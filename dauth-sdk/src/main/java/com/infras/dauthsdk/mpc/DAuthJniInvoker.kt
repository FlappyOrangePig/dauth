package com.infras.dauthsdk.mpc

import androidx.annotation.Keep
import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.wallet.impl.manager.Managers
import com.infras.dauthsdk.wallet.util.cleanHexPrefix
import com.infras.dauthsdk.wallet.util.prependHexPrefix
import com.infras.dauthsdk.wallet.util.sha3
import com.infras.dauthsdk.wallet.util.toHexString
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.web3j.crypto.Sign.SignatureData
import kotlin.random.Random

private const val TAG = "DAuthJniInvoker"

/**
 * 使用kotlin进行调用，方便使用内联函数
 * 本地模拟多签参见[LocalMpcSign.mpcSign]
 */
object DAuthJniInvoker {
    private const val THRESHOLD = 2
    private const val PARTIES = 3
    private val jni by lazy { DAuthJni.getInstance() }

    fun localSignMsg(msgHash: String, keys: Array<String>): SignResult? {
        val keyIds = MpcKeyIds.getKeyIds()
        DAuthLogger.d("localSignMsg: ${keyIds.contentToString()}", TAG)
        val signedResultJson = jni.localSignMsg(
            msgHash.cleanHexPrefix(),
            keyIds.take(2).toTypedArray(),
            keys.take(2).toTypedArray()
        )
        DAuthLogger.d("localSignMsg result=$signedResultJson", TAG)
        return signedResultJson.toSignResult()
    }

    fun getWalletAddress(msgHash: ByteArray, sd: SignatureData): String? {
        return try {
            // 这个方法有时候崩溃，比如返回的r是31位
            val signedPublicKey = Sign.signedMessageHashToKey(msgHash, sd)
            Keys.getAddress(signedPublicKey).prependHexPrefix()
        } catch (e: Exception) {
            DAuthLogger.e(e.stackTraceToString())
            null
        }
    }

    fun generateEoaAddress(msg: ByteArray, keys: Array<String>): String? {
        val msgHash = msg.sha3()
        val msgHashHex = msgHash.toHexString()
        val signResult = localSignMsg(msgHashHex, keys)
        if (signResult == null) {
            DAuthLogger.e("signResult null", TAG)
            return null
        }
        val sd = signResult.toSignatureData()
        return getWalletAddress(msgHash, sd)
    }

    /**
     * 生成随机消息。为了执行[DAuthJni.localSignMsg]
     */
    fun genRandomMsg(): String{
        val seed = System.currentTimeMillis()
        val random = Random(seed)
        val long = random.nextLong()
        return long.toString()
    }

    fun String.toSignResult() = try {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(SignResult::class.java)
        adapter.fromJson(this)
    } catch (e: Exception) {
        DAuthLogger.e(e.stackTraceToString(), TAG)
        null
    }

    fun generateSignKeys(): Array<String> {
        // 3签2
        /*val authId = Managers.loginPrefs.getAuthId()
        if (authId.isEmpty()) {
            DAuthLogger.e("auth id empty", TAG)
            return emptyArray()
        }*/

        val ids = MpcKeyIds.getKeyIds()
        val r = jni.generateSignKeys(THRESHOLD, PARTIES, ids)
        DAuthLogger.d("----generateSignKeys----", TAG)
        /*r.forEachIndexed { index, s ->
            DAuthLogger.d("key$index) len=${s.length}\n$s", TAG)
        }*/
        return r
    }
}

@Keep
class SignResult(
    private val rh: String,
    private val sh: String,
    private val r: String,
    private val s: String,
    private val v: Int
) {
    fun toSignatureData(): SignatureData {
        val r = rh.hexStringToByteArray()
        val s = sh.hexStringToByteArray()
        // MPC.so返回的结果为0或者1
        // 但是以太坊的合法值范围是[27, 34]
        // 参考方法signedMessageHashToKey
        val vByte = (v + 27).toByte()
        return SignatureData(vByte, r, s)
    }

    private fun String.hexStringToByteArray(): ByteArray {
        val hexChars = this.toCharArray()
        val result = ByteArray(this.length / 2)
        for (i in 0 until this.length step 2) {
            val firstNibble = Character.digit(hexChars[i], 16)
            val secondNibble = Character.digit(hexChars[i + 1], 16)
            val byteValue = firstNibble shl 4 or secondNibble
            result[i / 2] = byteValue.toByte()
        }
        return result
    }

    override fun toString(): String {
        return "SignResult(rh='$rh', sh='$sh', r='$r', s='$s', v=$v)"
    }
}