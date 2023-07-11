package com.cyberflow.dauthsdk.wallet.impl

import androidx.annotation.Keep
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.mpc.entity.CommitTransRes
import com.cyberflow.dauthsdk.mpc.util.MoshiUtil
import com.cyberflow.dauthsdk.wallet.impl.manager.Managers
import com.cyberflow.dauthsdk.wallet.sol.EntryPoint.UserOperation
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.math.BigInteger
import kotlin.coroutines.resume

private const val TAG = "RelayerRequester"

object RelayerRequester {

    private val useDevServer get() = DAuthSDK.impl.config.useDevRelayerServer
    private val url = if (useDevServer) {
        "http://172.16.12.117:8888/relayer/committrans"
    } else {
        ConfigurationManager.urls().relayerUrl
    }

    suspend fun sendRequest(userOperation: UserOperation): Boolean {
        val sp = Managers.loginPrefs
        val accessToken = sp.getAccessToken()
        val openId = sp.getAuthId()

        val wireUserOp = WireUserOp(
            userOperation.sender,
            userOperation.nonce,
            userOperation.initCode,
            userOperation.callData,
            userOperation.callGasLimit,
            userOperation.verificationGasLimit,
            userOperation.preVerificationGas,
            userOperation.maxFeePerGas,
            userOperation.maxPriorityFeePerGas,
            userOperation.paymasterAndData,
            userOperation.signature
        )
        val transData = MoshiUtil.toJson(wireUserOp)
        DAuthLogger.d("transData=$transData", TAG)
        val input = FormBody.Builder()
            .add("open_id", openId)
            .add("access_token", accessToken)
            .add("client_id", DAuthSDK.impl.config.clientId.orEmpty())
            .add("transdata", transData)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(input)
            .build()

        val responseBody = suspendCancellableCoroutine { continuation ->
            val call = HttpClient.client.newCall(request)
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resume(null)
                }

                override fun onResponse(call: Call, response: Response) {
                    val r = try {
                        var finalRes: CommitTransRes? = null
                        if (response.isSuccessful) {
                            val body = response.body
                            if (body != null) {
                                val string = body.string()
                                val res = MoshiUtil.fromJson<CommitTransRes>(string)
                                if (res?.ret == 0) {
                                    finalRes = res
                                }
                            }
                        }
                        finalRes
                    } catch (t: Throwable) {
                        DAuthLogger.e(t.stackTraceToString())
                        null
                    }
                    continuation.resume(r)
                }
            })
        }
        return responseBody != null
    }
}

@Keep
class WireUserOp(
    val sender: String,
    val nonce: BigInteger,
    val initCode: ByteArray,
    val callData: ByteArray,
    val callGasLimit: BigInteger,
    val verificationGasLimit: BigInteger,
    val preVerificationGas: BigInteger,
    val maxFeePerGas: BigInteger,
    val maxPriorityFeePerGas: BigInteger,
    val paymasterAndData: ByteArray,
    val signature: ByteArray,
)