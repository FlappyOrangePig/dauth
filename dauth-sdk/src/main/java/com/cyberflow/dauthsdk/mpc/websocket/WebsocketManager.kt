package com.cyberflow.dauthsdk.mpc.websocket

import android.os.CountDownTimer
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.login.utils.LoginPrefs
import com.cyberflow.dauthsdk.mpc.CoSignerUser
import com.cyberflow.dauthsdk.mpc.DAuthJniInvoker
import com.cyberflow.dauthsdk.mpc.MpcKeyIds
import com.cyberflow.dauthsdk.mpc.MpcKeyStore
import com.cyberflow.dauthsdk.mpc.entity.MpcWsHeader
import com.cyberflow.dauthsdk.mpc.util.MoshiUtil
import com.cyberflow.dauthsdk.wallet.impl.HttpClient
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.web3j.crypto.Hash

private const val TAG = "WebSocket"
private const val MPC_REQUEST_HEADER = "WebSocket-Mpc-Request"

class WebsocketManager private constructor() {
    companion object {
        private const val ECHO_SERVER_URL = "wss://ws.postman-echo.com/raw"

        //private const val SERVER_URL = "ws://api.infras.online/mpc/sign"
        private const val SERVER_URL = "ws://172.16.12.35:9001/" // Tyler-local
        val instance by lazy { WebsocketManager() }
    }

    fun createDefaultSession(): DAuthWsSession {
        return createSession(SERVER_URL)
    }

    private fun createSession(url: String): DAuthWsSession {
        DAuthLogger.d("create socket $url", TAG)

        // 获取另一篇远端签名key的id
        val remoteId = MpcKeyIds.getRemoteIdsToSign()
        //WebSocket-Mpc-Request:{"signtype":"gg18","src":"co_signer1","openid":"014ebdf3a3789e211f81d9b8c771d717","token":"accesstoken"}
        val sp = LoginPrefs(DAuthSDK.impl.context)
        val accessToken = sp.getAccessToken()
        val openId = sp.getAuthId()

        val msg = DAuthJniInvoker.genRandomMsg()
        val msgHash = Hash.sha3String(msg).removePrefix("0x")
        val localKey = MpcKeyStore.getLocalKey()
        DAuthLogger.d("localKey len=${localKey.length}", TAG)
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
            src = remoteId,
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
            user = user
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
        const val RESULT_SUCCESS = 2901
        const val RESULT_FAILURE = 2902

        private const val SEND_TIMEOUT = 5_000L
    }

    var onEvent: ((event: Int) -> Unit)? = null
    private var timer: CountDownTimer? = null

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            DAuthLogger.d("connect opened, send ...", TAG)
            trySend(signed)
        }

        /*override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            stopCloseTimer()
            DAuthLogger.d("received message: $text", TAG)
        }*/

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            super.onMessage(webSocket, bytes)
            stopCloseTimer()
            DAuthLogger.d("received binary data: $bytes", TAG)

            val (result, outBuffer) = user.signRound(bytes.toByteArray())
            DAuthLogger.d("signRound: $result", TAG)
            if (result) {
                closeWith(RESULT_SUCCESS)
            } else {
                trySend(outBuffer)
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            DAuthLogger.d("connection failed: ${t.message}", TAG)
            dispatchEvent(RESULT_FAILURE)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            DAuthLogger.d("connection closed: $reason", TAG)

            if (code == RESULT_SUCCESS) {
                dispatchEvent(RESULT_SUCCESS)
            } else {
                dispatchEvent(RESULT_FAILURE)
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

    private fun trySend(byteArray: ByteArray) {
        val r = ws.send(byteArray.toByteString())
        DAuthLogger.d("trySend $r", TAG)
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
        DAuthLogger.d("restart timer", TAG)
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