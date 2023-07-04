package com.cyberflow.dauthsdk.mpc

import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.mpc.DAuthJniInvoker.toSignResult
import com.cyberflow.dauthsdk.wallet.util.cleanHexPrefix
import com.cyberflow.dauthsdk.wallet.util.sha3String
import org.web3j.crypto.Sign.SignatureData

private const val TAG = "LocalMpcSign"

object LocalMpcSign {

    fun mpcSign(msg: String): SignatureData? {
        DAuthLogger.d("mpcSign $msg", TAG)

        val msgHash = msg.sha3String().cleanHexPrefix()

        val keys = MpcKeyStore.getAllKeys()

        keys.forEachIndexed { i, e ->
            DAuthLogger.d("$i)key ${e.length} ${e.substring(20)}", TAG)
        }

        // 模拟多轮签名
        val u1 = CoSignerUser(
            "coSigner1",
            msgHash,
            keys[0],
            MpcKeyIds.getLocalId(),
            MpcKeyIds.getRemoteIdsToSign()
        )
        val u2 = CoSignerUser(
            "coSigner2",
            msgHash,
            keys[1],
            MpcKeyIds.getRemoteIdsToSign(),
            MpcKeyIds.getLocalId()
        )

        val out1: ByteArray = u1.startRemoveSign()
        val out2: ByteArray = u2.startRemoveSign()

        var temp1: Pair<Boolean, ByteArray>? = null
        var temp2: Pair<Boolean, ByteArray>? = null

        var result1: Pair<Boolean, ByteArray> = false to out1
        var result2: Pair<Boolean, ByteArray> = false to out2

        var index = 1

        while (true) {
            DAuthLogger.d("poll ${index++}", TAG)

            if (!result1.first) {
                temp2 = u2.signRound(result1.second)
            }
            if (!result2.first) {
                temp1 = u1.signRound(result2.second)
            }

            result2 = temp2!!
            result1 = temp1!!

            // 都完成就退出
            if (result2.first && result1.first) {
                break
            }
        }

        val r1 = String(result1.second)
        val r2 = String(result2.second)

        DAuthLogger.d("mpcSign r1=$r1", TAG)
        DAuthLogger.d("mpcSign r2=$r2", TAG)

        return r1.toSignResult()?.toSignatureData()
    }
}