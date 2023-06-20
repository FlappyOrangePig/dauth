package com.cyberflow.dauthsdk.mpc.websocket

import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit

private const val TAG = "WebSocket"

class WebsocketManager private constructor() {
    companion object {
        private const val ECHO_SERVER_URL = "wss://ws.postman-echo.com/raw"
        private const val SERVER_URL = "ws://api.infras.online/mpc/sign"
        val instance by lazy { WebsocketManager() }
    }

    fun createDefaultSession(): DAuthWsSession {
        return createSession(ECHO_SERVER_URL)
    }

    private fun createSession(url: String): DAuthWsSession {
        DAuthLogger.d("create socket $url", TAG)
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder()
            .url(url)
            .build()
        return DAuthWsSession(client = client, request = request)
    }
}

class DAuthWsSession(
    client: OkHttpClient,
    request: Request,
) : WebSocket, WebSocketListener() {

    private val ws = client.newWebSocket(request, this)

    override fun cancel() {
        ws.cancel()
    }

    override fun close(code: Int, reason: String?): Boolean {
        return ws.close(code, reason)
    }

    override fun queueSize(): Long {
        return ws.queueSize()
    }

    override fun request(): Request {
        return ws.request()
    }

    override fun send(text: String): Boolean {
        return ws.send(text)
    }

    override fun send(bytes: ByteString): Boolean {
        return ws.send(bytes)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        DAuthLogger.d("connect opened.", TAG)
        webSocket.send("Hello, server!")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        DAuthLogger.d("received message: $text", TAG)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        super.onMessage(webSocket, bytes)
        DAuthLogger.d("received binary data: $bytes", TAG)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        DAuthLogger.d("connection failed: ${t.message}", TAG)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        DAuthLogger.d("connection closed: $reason", TAG)
    }
}