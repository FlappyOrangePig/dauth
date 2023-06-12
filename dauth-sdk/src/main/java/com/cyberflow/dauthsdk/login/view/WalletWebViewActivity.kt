package com.cyberflow.dauthsdk.login.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.cyberflow.dauthsdk.databinding.ActivityWebviewBinding
import com.cyberflow.dauthsdk.login.viewmodel.WalletWebViewViewModel
import com.tencent.smtt.sdk.QbSdk
import com.tencent.smtt.sdk.WebView
import androidx.activity.viewModels
import com.cyberflow.dauthsdk.login.callback.WalletCallback
import com.cyberflow.dauthsdk.login.constant.LoginConst.USER_TYPE
import com.cyberflow.dauthsdk.login.model.AuthorizeToken2Param
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.utils.*

private const val TAG = "X5WebViewActivity"
private const val H5_URL = "https://wallet.51ailike.com/#/login"
private const val WEB_VIEW_ACTIVITY_EXTRA = "WEB_VIEW_ACTIVITY_EXTRA"
private const val TYPE_OF_WALLET_AUTH = "20"
private const val USER_TYPE = "user_type"
private const val USER_DATA = "user_data"

class WalletWebViewActivity : AppCompatActivity() {
    private var _binding: ActivityWebviewBinding? = null
    private val binding: ActivityWebviewBinding get() = _binding!!
    private lateinit var webView: WebView
    private val viewModel: WalletWebViewViewModel by viewModels()

    companion object {
        fun launch(context: Context, isFinish: Boolean) {
            val intent = Intent(context, WalletWebViewActivity::class.java)
            intent.putExtra(WEB_VIEW_ACTIVITY_EXTRA, isFinish)
            context.startActivity(intent)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWebviewBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        initWebView()
    }

    private fun initWebView() {
        webView = WebView(this)
        val mContainer = binding.webViewContainer
        mContainer.addView(webView)
        viewModel.initWebViewClient(webView)
        webView.addJavascriptInterface(JavaScriptMethods(this, object : WalletCallback {
            override fun onResult(walletInfo: String) {
                val map = HashMap<String, String>()
                map[USER_TYPE] = TYPE_OF_WALLET_AUTH
                map[USER_DATA] = walletInfo
                val sign = SignUtils.sign(map)
                val body = AuthorizeToken2Param(
                    user_type = TYPE_OF_WALLET_AUTH,
                    sign = sign,
                    user_data = walletInfo
                )
                RequestApi().authorizeExchangedToken(body)
            }
        }), "android")
        webView.loadUrl(H5_URL)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val isFinishWebView = intent.getBooleanExtra(WEB_VIEW_ACTIVITY_EXTRA,false)
        DAuthLogger.d("isFinishWebView==$isFinishWebView")
        if(isFinishWebView) {
            finishAffinity()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        QbSdk.clearAllWebViewCache(this, true) // 清除缓存
        webView.stopLoading()
        webView.clearHistory()
        webView.clearCache(true)
        webView.onPause()
        webView.removeAllViews()
        webView.destroy()
    }

}