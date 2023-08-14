package com.infras.dauthsdk.wallet.connect.metamask

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import androidx.lifecycle.lifecycleScope
import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.wallet.base.BaseFragment
import com.infras.dauthsdk.wallet.connect.metamask.util.JsConvertUtil
import com.infras.dauthsdk.wallet.connect.metamask.util.JsInvoker
import com.infras.dauthsdk.wallet.connect.metamask.util.MetaMaskH5Holder
import com.infras.dauthsdk.wallet.connect.metamask.widget.MetaMaskWebView
import kotlinx.coroutines.launch

/**
 * 对MetaMask的WebView进行包装，封装基本逻辑。
 * 而使用不同UI组件的封装参见[MetaMaskActivity]或[MetaMaskDialog]
 */
class MetaMaskFragment : BaseFragment() {

    companion object {
        private const val TAG = "MetaMaskFragment"
        fun newInstance(): MetaMaskFragment {
            return MetaMaskFragment()
        }
    }

    private val jsInvoker = JsInvoker { webView!! }
    private var webView: MetaMaskWebView? = null
    private val builtInOnAuthorizeHandler = listOf(
        object : JSHandler("OnAuthorize") {
            override fun onHandle(arguments: Map<String, String>) {
                val account = arguments["message"]
                DAuthLogger.d("OnAuthorize $account", TAG)
                if (!account.isNullOrEmpty()) {
                    refreshH5Ui()
                }
            }
        }
    )

    private var metaMaskInput: MetaMaskInput? = null
    @Volatile
    private var jsToBeExecutedOnPageRefresh: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        FrameLayout(requireContext()).apply {
            addView(MetaMaskH5Holder.getGlobalWebView().also {wv ->
                wv.setOnPageFinished {
                    // 刷新页面后更新按钮状态
                    refreshH5Ui()
                }
                webView = wv
            }, -1, -2)
            layoutParams = MarginLayoutParams(-1, -2)
            return this
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //loadUrl()
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
        webView?.refresh()
    }

    internal fun setMetaMaskInput(input: MetaMaskInput) {
        metaMaskInput = input
        when (input) {
            MetaMaskInput.Connect -> {
            }

            is MetaMaskInput.PersonalSign -> {
                lifecycleScope.launch {
                    setJsToBeExecutedOnPageRefresh(JsConvertUtil.personalSignMessage(input.message))
                }
            }

            is MetaMaskInput.SendTransaction -> {
                lifecycleScope.launch {
                    setJsToBeExecutedOnPageRefresh(JsConvertUtil.sendTransaction(input.transactionJson))
                }
            }
        }
    }

    internal fun setJsHandlers(handler: List<JSHandler>) {
        DAuthLogger.d("setJsHandlers $handler", TAG)
        webView?.let {
            it.setHandlers(
                mutableListOf<JSHandler>().apply {
                    addAll(handler)
                    addAll(builtInOnAuthorizeHandler)
                }
            )
        }
    }

    private suspend fun setJsToBeExecutedOnPageRefresh(js: String) {
        this.jsToBeExecutedOnPageRefresh = js
        this.jsInvoker.evaluateJavascript(js)
    }

    suspend fun isConnected() = kotlin.runCatching { jsInvoker.isConnected() }.getOrNull()

    fun getCurrentEoaAddress(): String? {
        return webView?.getAccount()
    }
}

internal abstract class JSHandler(val type: String) {
    abstract fun onHandle(arguments: Map<String, String>)
}

