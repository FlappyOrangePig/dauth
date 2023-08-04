package com.infras.dauthsdk.wallet.connect.metamask

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.lifecycleScope
import com.infras.dauthsdk.api.DAuthSDK
import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.wallet.base.BaseFragment
import com.infras.dauthsdk.wallet.connect.metamask.util.JsInvoker
import com.infras.dauthsdk.wallet.connect.metamask.util.MetaMaskWebChromeClient
import com.infras.dauthsdk.wallet.connect.metamask.util.MetaMaskWebViewClient
import com.infras.dauthsdk.wallet.ext.dp
import com.infras.dauthsdk.wallet.ext.runCatchingWithLog
import com.squareup.moshi.Moshi
import kotlinx.coroutines.launch

private const val JS_OBJECT_NAME = "android"
private const val TAG = "MetaMaskFragment"
//private const val URL = "http://172.16.12.186:5500/index.html"
private const val URL = "file:///android_asset/index.html"

internal class MetaMaskFragment : BaseFragment() {

    private val jsInvoker = JsInvoker { webView }
    private val eoaWallet get() = DAuthSDK.impl.eoaWalletApi
    private lateinit var webView: WebView
    private val jsObjectHandlers = mutableListOf<JSHandler>()
    private val builtInOnAuthorizeHandler = listOf(
        object : JSHandler("OnAuthorize") {
            override fun onHandle(arguments: Map<String, String>) {
                val account = arguments["message"]
                DAuthLogger.d("OnAuthorize $account", TAG)
                if (!account.isNullOrEmpty()) {
                    // 保管地址
                    currentEoaAddress = account
                    refreshH5Ui()
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

    private var metaMaskInput: MetaMaskInput? = null
    @Volatile
    private var jsToBeExecutedOnPageRefresh: String? = null
    @Volatile
    private var currentEoaAddress: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                setSupportZoom(false)
                databaseEnabled = false
                domStorageEnabled = true
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true
            }
            webViewClient = MetaMaskWebViewClient {
                if (it == URL) {
                    // 刷新页面后更新按钮状态
                    refreshH5Ui()
                }
            }
            webChromeClient = MetaMaskWebChromeClient

            val jso = MetaMaskJsObject(jsObjectHandlers)
            addJavascriptInterface(jso, JS_OBJECT_NAME)
            layoutParams = MarginLayoutParams(-1, 100.dp())
            webView = this
            return webView
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUrl()
    }

    override fun onResume() {
        super.onResume()
        refreshH5Ui()
    }

    private fun refreshH5Ui() = lifecycleScope.launch {
        val isConnected = jsInvoker.isConnected() == true
        val input = metaMaskInput
        DAuthLogger.d("refreshH5Ui connected=$isConnected input=$input")
        val buttonIndex = when (input) {
            null -> -1
            MetaMaskInput.Connect -> {
                if (isConnected) {
                    -1
                } else {
                    0
                }
            }

            is MetaMaskInput.SendTransaction -> {
                if (isConnected) {
                    1
                } else {
                    0
                }
            }

            is MetaMaskInput.PersonalSign -> {
                if (isConnected) {
                    2
                } else {
                    0
                }
            }
        }
        DAuthLogger.v("showButton input=${input} index=${buttonIndex}", TAG)
        jsInvoker.showButton(buttonIndex)

        // 页面刷新时也要更新请求数据到js
        jsToBeExecutedOnPageRefresh?.let {
            jsInvoker.evaluateJavascript(it)
        }
    }

    fun loadUrl() {
        webView.loadUrl(URL)
    }

    fun setMetaMaskInput(input: MetaMaskInput) {
        metaMaskInput = input
    }

    fun setJsHandlers(handler: List<JSHandler>) {
        DAuthLogger.d("setJsHandlers $handler", TAG)
        jsObjectHandlers.apply {
            clear()
            addAll(builtInOnAuthorizeHandler)
            addAll(handler)
        }
    }

    suspend fun setJsToBeExecutedOnPageRefresh(js: String) {
        this.jsToBeExecutedOnPageRefresh = js
        this.jsInvoker.evaluateJavascript(js)
    }

    suspend fun isConnected() = jsInvoker.isConnected()

    fun getCurrentEoaAddress(): String? {
        return currentEoaAddress
    }
}

@VisibleForTesting
@Keep
class MetaMaskJsObject internal constructor(
    private val handlers: List<JSHandler>
) {
    companion object {
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

internal abstract class JSHandler(val type: String) {
    abstract fun onHandle(arguments: Map<String, String>)
}

