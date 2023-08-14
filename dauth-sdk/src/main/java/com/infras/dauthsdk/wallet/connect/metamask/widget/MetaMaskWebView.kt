package com.infras.dauthsdk.wallet.connect.metamask.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.wallet.connect.metamask.JSHandler
import com.infras.dauthsdk.wallet.ext.runCatchingWithLog
import com.squareup.moshi.Moshi

private const val TAG = "MetaMaskWebView"
//private const val URL = "http://172.16.12.186:5500/index.html"
private val URL = "file:///android_asset/index.html"

internal class MetaMaskWebView(context: Context) : WebView(context) {

    companion object {
        private const val JS_OBJECT_NAME = "android"
    }

    private var currentEoaAddress: String? = null
    private val handlers = mutableListOf<JSHandler>()
    private val builtInOnAuthorizeHandlers = listOf(
        object : JSHandler("OnAuthorize") {
            override fun onHandle(arguments: Map<String, String>) {
                val account = arguments["message"]
                DAuthLogger.d("OnAuthorize $account", TAG)
                if (!account.isNullOrEmpty()) {
                    // 保管地址
                    currentEoaAddress = account
                }
            }
        },
        object : JSHandler("OnConnected") {
            override fun onHandle(arguments: Map<String, String>) {
                DAuthLogger.d("onConnected", TAG)
            }
        },
        object : JSHandler("OnDisconnect") {
            override fun onHandle(arguments: Map<String, String>) {
                DAuthLogger.d("OnDisconnected", TAG)
            }
        },
        object : JSHandler("OnMessage") {
            override fun onHandle(arguments: Map<String, String>) {
                DAuthLogger.d("OnMessage", TAG)
            }
        },
        object : JSHandler("OnChainChanged") {
            override fun onHandle(arguments: Map<String, String>) {
                DAuthLogger.d("OnChainChanged", TAG)
            }
        }
    )

    init {
        settings.apply {
            javaScriptEnabled = true
            setSupportZoom(false)
            databaseEnabled = false
            domStorageEnabled = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
        }
        webViewClient = MetaMaskWebViewClient
        webChromeClient = MetaMaskWebChromeClient
        addJavascriptInterface(MetaMaskJsObject(handlers), JS_OBJECT_NAME)
        refresh()
    }

    fun setOnPageFinished(listener: ((String) -> Unit)?) {
        MetaMaskWebViewClient.on1stPageFinished = listener
    }

    fun refresh() {
        loadUrl(URL)
    }

    fun setHandlers(h: MutableList<JSHandler>) {
        handlers.clear()
        handlers.addAll(builtInOnAuthorizeHandlers)
        handlers.addAll(h)
    }

    fun getAccount(): String? {
        return currentEoaAddress
    }
}


private object MetaMaskWebViewClient : WebViewClient() {

    @Volatile
    var on1stPageFinished: ((String) -> Unit)? = null

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        DAuthLogger.d("onPageStarted: url=$url", TAG)
    }

    override fun onPageFinished(view: WebView, url: String) {
        DAuthLogger.d("onPageFinished: url=$url", TAG)
        if (URL == url){
            on1stPageFinished?.invoke(url)
        }
    }

    @Deprecated("see super class")
    override fun onReceivedError(
        webView: WebView,
        errorCode: Int,
        description: String,
        failingUrl: String
    ) {
        DAuthLogger.e(
            "onReceivedError: errorCode=$errorCode, description=$description, failingUrl=$failingUrl",
            TAG
        )
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        DAuthLogger.e("onReceivedError: request=$request, error=$error", TAG)
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        val result = if (url.startsWith("https://metamask.app.link/")) {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            view.context.startActivity(intent)
            true
        } else {
            super.shouldOverrideUrlLoading(view, request)
        }
        DAuthLogger.d("shouldOverrideUrlLoading url=$url result=$result", TAG)
        return result
    }

    override fun onReceivedSslError(
        view: WebView,
        handler: SslErrorHandler,
        error: SslError
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(view.context)
        builder.setMessage("SSL ERROR!")
        builder.setPositiveButton(
            "continue"
        ) { _, _ -> handler.proceed() }
        builder.setNegativeButton(
            "cancel"
        ) { _, _ -> handler.cancel() }
        val dialog: AlertDialog = builder.create()
        dialog.show()
        DAuthLogger.i("onReceivedSslError:$error", TAG)
    }
}

private object MetaMaskWebChromeClient : WebChromeClient()

@VisibleForTesting
@Keep
private class MetaMaskJsObject(
    private val handlers: List<JSHandler>
) {
    companion object {
        private const val TAG = "MetaMaskJsObject"
        @VisibleForTesting
        fun toStringMap(jsonStr: String): Map<String, String>? {
            return try {
                val moshi = Moshi.Builder().build()
                val jsonAdapter = moshi.adapter<Map<String, String>>(
                    MutableMap::class.java
                )
                jsonAdapter.fromJson(jsonStr)
            } catch (t: Throwable) {
                DAuthLogger.e(t.stackTraceToString(), TAG)
                null
            }
        }
    }

    @JavascriptInterface
    fun invoke(message: String) {
        DAuthLogger.i("invoke $message", TAG)
        val map = toStringMap(message)
        if (map == null) {
            DAuthLogger.e("invoke json error", TAG)
            return
        }
        val type = map["type"]
        handlers.filter {
            it.type == type
        }.also {
            DAuthLogger.i("invoke count ${it.size}", TAG)
        }.forEach {
            runCatchingWithLog {
                it.onHandle(map)
            }
        }
    }
}