package com.infras.dauthsdk.mpc.websocket

import android.os.CountDownTimer
import com.infras.dauthsdk.api.DAuthSDK
import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.mpc.CoSignerUser
import com.infras.dauthsdk.mpc.DAuthJniInvoker.toSignResult
import com.infras.dauthsdk.mpc.MpcKeyIds
import com.infras.dauthsdk.mpc.entity.MpcWsHeader
import com.infras.dauthsdk.mpc.util.MoshiUtil
import com.infras.dauthsdk.mpc.util.ZipUtil
import com.infras.dauthsdk.wallet.impl.ConfigurationManager
import com.infras.dauthsdk.wallet.impl.HttpClient
import com.infras.dauthsdk.wallet.impl.manager.Managers
import com.infras.dauthsdk.wallet.util.ThreadUtil
import com.infras.dauthsdk.wallet.util.cleanHexPrefix
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.web3j.crypto.Sign.SignatureData
import kotlin.coroutines.resume

private const val TAG = "WebSocket"
private const val MPC_REQUEST_HEADER = "WebSocket-Mpc-Request"

internal class WebsocketManager private constructor() {
    companion object {
        internal val instance by lazy { WebsocketManager() }
        private val useDevServer get() = DAuthSDK.impl.config.useDevWebSocketServer
        private val serverUrl = if (useDevServer) {
            "ws://172.16.12.117:9001/"
        } else {
            "wss://${ConfigurationManager.stage().baseUrlHost}/mpc/sign"
        }
    }

    suspend fun mpcSign(msgHash: String): SignatureData? {
        //必须在主线程调用，onEvent也在主线程回调
        ThreadUtil.assertInMainThread(true)
        val r = suspendCancellableCoroutine { continuation ->
            val s = createSession(msgHash.cleanHexPrefix())
            if (s == null) {
                continuation.resume(null)
            } else {
                s.onEvent = { event ->
                    if (event == DAuthWsSession.RESULT_SUCCESS) {
                        continuation.resume(s.signatureData!!)
                    } else {
                        continuation.resume(null)
                    }
                }
            }
        }
        DAuthLogger.d("mpcSign result:${MoshiUtil.toJson(r)}")
        return r
    }

    private fun createSession(msgHash: String): DAuthWsSession? {
        val url = serverUrl
        DAuthLogger.d("create socket $url", TAG)

        val sp = Managers.loginPrefs
        val accessToken = sp.getAccessToken()
        val openId = sp.getAuthId()

        val localKey = Managers.mpcKeyStore.getLocalKey()
        DAuthLogger.d("localKey len=${localKey.length}", TAG)
        if (localKey.isEmpty()) {
            return null
        }

        val user = CoSignerUser(
            "coSigner",
            msgHash,
            localKey,
            MpcKeyIds.getLocalId(),
            MpcKeyIds.getRemoteIdsToSign()
        )
        // 获得第一个输入
        val signed = user.startRemoveSign()

        // 创建请求
        val mpcWsHeader = MpcWsHeader(
            src = MpcKeyIds.getLocalId(),
            openid = openId,
            token = accessToken,
            bm = msgHash
        )
        val json = MoshiUtil.toJson(mpcWsHeader)
        DAuthLogger.d("json=$json", TAG)

        val request = Request.Builder()
            .url(url)
            .addHeader(MPC_REQUEST_HEADER, json)
            .build()

        return DAuthWsSession(
            client = HttpClient.client,
            request = request,
            signed = signed,
            user = user,
        )
    }
}

class DAuthWsSession(
    client: OkHttpClient,
    request: Request,
    private val signed: ByteArray,
    private val user: CoSignerUser,
) : WebSocketListener() {

    companion object {
        const val RESULT_SUCCESS = 4001
        const val RESULT_FAILURE = 4002

        private const val SEND_TIMEOUT = 5_000L
    }

    var onEvent: ((event: Int) -> Unit)? = null
    private var timer: CountDownTimer? = null
    var signatureData: SignatureData? = null

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            ThreadUtil.runOnMainThread {
                DAuthLogger.d("connect opened, send ...", TAG)
                trySendWithIoSwitch(signed)
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            super.onMessage(webSocket, bytes)
            val byteArray = bytes.toByteArray()
            val decompressed = ZipUtil.decompress(byteArray)

            ThreadUtil.runOnMainThread {
                stopCloseTimer()
                DAuthLogger.d("received binary size=${bytes.size}", TAG)

                val (result, outBuffer) = try {
                    user.signRound(decompressed)
                } catch (t: Throwable) {
                    DAuthLogger.e(t.stackTraceToString(), TAG)
                    closeWith(RESULT_FAILURE)
                    return@runOnMainThread
                }

                DAuthLogger.d("signRound: $result", TAG)
                if (result) {
                    val data = String(outBuffer).toSignResult()?.toSignatureData()
                    if (data == null) {
                        closeWith(RESULT_FAILURE)
                        return@runOnMainThread
                    }
                    this@DAuthWsSession.signatureData = data
                    closeWith(RESULT_SUCCESS)
                } else {
                    trySendWithIoSwitch(outBuffer)
                }
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            ThreadUtil.runOnMainThread {
                DAuthLogger.d("connection failed: ${t.stackTraceToString()}", TAG)
                dispatchEvent(RESULT_FAILURE)
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            ThreadUtil.runOnMainThread {
                DAuthLogger.d("connection closed: $reason", TAG)

                if (code == RESULT_SUCCESS) {
                    dispatchEvent(RESULT_SUCCESS)
                } else {
                    dispatchEvent(RESULT_FAILURE)
                }
            }
        }
    }

    private val ws = client.newWebSocket(request, listener)

    private fun dispatchEvent(event: Int) {
        DAuthLogger.d("dispatchEvent $event", TAG)
        onEvent?.invoke(event)
    }

    private fun closeWith(event: Int) {
        DAuthLogger.d("closeWith $event", TAG)
        ws.close(event, "")
    }

    private fun trySendWithIoSwitch(byteArray: ByteArray) {
        ThreadUtil.runOnWorkerThread {
            val zipped = ZipUtil.compress(byteArray)
            ThreadUtil.runOnMainThread {
                trySend(zipped)
            }
        }
    }

    private fun trySend(byteArray: ByteArray) {
        val r = ws.send(byteArray.toByteString())
        DAuthLogger.d("trySend ${byteArray.size} $r", TAG)
        if (!r) {
            closeWith(RESULT_FAILURE)
        } else {
            restartCloseTimer()
        }
    }

    private fun stopCloseTimer() {
        timer?.let {
            it.cancel()
            timer = null
        }
    }

    private fun restartCloseTimer() {
        DAuthLogger.v("restart timer", TAG)
        timer?.cancel()
        timer = object : CountDownTimer(SEND_TIMEOUT, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                DAuthLogger.d("send timeout", TAG)
                closeWith(RESULT_FAILURE)
            }
        }.start()
    }
}