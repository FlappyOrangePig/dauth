package com.cyberflow.dauthsdk.mpc

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.web3j.crypto.Hash
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.web3j.crypto.Sign.SignatureData
import kotlin.random.Random

private const val TAG = "DAuthJniInvoker"

private inline fun <T> runSpending(logInfo: String, block: () -> T): T {
    DAuthLogger.d("$logInfo >>>", TAG)
    val start = System.currentTimeMillis()
    val r = block.invoke()
    val spent = System.currentTimeMillis() - start
    DAuthLogger.d("$logInfo <<< spent $spent", TAG)
    return r
}

private fun ByteArray.printable(): String {
    val sb = StringBuilder()
    for (b in this) {
        sb.append(String.format("%02X ", b))
    }
    return sb.toString()
}

/**
 * 使用kotlin进行调用，方便使用内联函数
 */
object DAuthJniInvoker {
    private val jni by lazy { DAuthJni.getInstance() }
    private val keystore get() = MpcKeyStore

    fun initialize(){
        initializeInner()
    }

    private fun initializeInner() {
        jni.init()

        // 3签2
        val threshold = 2
        val nParties = 3

        val keyInSp = MpcKeyStore.getAllKeys()
        val keys = if (keyInSp.isEmpty()) {
            runSpending("generateSignKeys") {
                jni.generateSignKeys(threshold, nParties).also {
                    keystore.setAllKeys(it.toList())
                }
            }
        } else {
            keyInSp.toTypedArray()
        }
        /*runSpending("refreshKeys") {
            jni.refreshKeys(keys, nParties)
        }*/

        val msg = genRandomMsg()
        val msgHash = Hash.sha3String(genRandomMsg()).removePrefix("0x")

        runSpending("localSign") {
            val account = generateEoaAddress(msg, keys)
            DAuthLogger.d("localSign account=$account", TAG)
        }

        runSpending("remoteSignMsg") {
            DAuthLogger.d("remoteSignMsg msgHash=$msgHash", TAG)
            val byteArrayList = ArrayList<ByteArray>()
            jni.remoteSignMsg(msgHash, keys[0], 0, intArrayOf(1), byteArrayList)
            val returnedBytes = if (byteArrayList.isNotEmpty()){
                byteArrayList.first()
            } else{
                null
            }
            DAuthLogger.d("outBuffer[${returnedBytes?.size}]=${returnedBytes?.printable()}", TAG)
        }

        // 模拟多轮签名
        val u1 = CoSignerUser("coSigner1", msgHash, keys[0], 0, 1)
        val u2 = CoSignerUser("coSigner2", msgHash, keys[1], 1, 0)

        val out1: ByteArray = u1.startRemoveSign()
        val out2: ByteArray = u2.startRemoveSign()

        var temp1 : Pair<Boolean, ByteArray>? = null
        var temp2 : Pair<Boolean, ByteArray>? = null

        var result1 : Pair<Boolean, ByteArray> = false to out1
        var result2 : Pair<Boolean, ByteArray> = false to out2

        var index = 1

        while (true) {
            DAuthLogger.d("poll ${index++}", TAG)

            if (!result1.first){
                temp2 = u2.signRound(result1.second)
            }
            if (!result2.first){
                temp1 = u1.signRound(result2.second)
            }

            result2 = temp2!!
            result1 = temp1!!

            // 都完成就退出
            if (result2.first && result1.first){
                break
            }
        }

        val r1 = String(result1.second)
        val r2 = String(result2.second)
        DAuthLogger.d("\ncoSignResult1=${r1}\ncoSignResult2=${r2}", TAG)
        DAuthLogger.d("equals=${r1==r2}", TAG)
    }

    private fun localSignMsg(msg: String, keys: Array<String>): SignResult? {
        val msgHash = Hash.sha3String(msg).removePrefix("0x")
        val indies = intArrayOf(0, 1)
        val signedResultJson = jni.localSignMsg(msgHash, keys, indies)
        DAuthLogger.d("localSignMsg:$signedResultJson")
        return signedResultJson.toSignResult()
    }

    @VisibleForTesting
    fun getWalletAddressBySignature(msg: String, sd: SignatureData): String {
        val signedPublicKey = Sign.signedMessageToKey(msg.toByteArray(), sd)
        return "0x" + Keys.getAddress(signedPublicKey)
    }

    private fun generateEoaAddress(msg: String, keys: Array<String>): String? {
        val signResult = localSignMsg(msg, keys)
        if (signResult == null) {
            DAuthLogger.e("signResult null", TAG)
            return null
        }
        val sd = signResult.toSignatureData()
        return getWalletAddressBySignature(msg, sd)
    }

    /**
     * 生成随机消息。为了执行[DAuthJni.localSignMsg]
     */
    @VisibleForTesting
    fun genRandomMsg(): String{
        val seed = System.currentTimeMillis()
        val random = Random(seed)
        val long = random.nextLong()
        return long.toString()
    }

    private fun String.toSignResult() = try {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(SignResult::class.java)
        adapter.fromJson(this)
    } catch (e: Exception) {
        DAuthLogger.e(e.stackTraceToString(), TAG)
        null
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