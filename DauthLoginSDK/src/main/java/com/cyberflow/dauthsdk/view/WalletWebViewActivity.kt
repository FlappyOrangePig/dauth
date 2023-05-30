package com.cyberflow.dauthsdk.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.cyberflow.dauthsdk.databinding.ActivityWebviewBinding
import com.cyberflow.dauthsdk.utils.DAuthLogger
import com.cyberflow.dauthsdk.utils.JavaScriptMethods
import com.cyberflow.dauthsdk.viewmodel.WalletWebViewViewModel
import com.tencent.smtt.sdk.QbSdk
import com.tencent.smtt.sdk.WebView
import androidx.activity.viewModels

private const val TAG = "X5WebViewActivity"
private const val H5_URL = "https://wallet.51ailike.com/#/login"
private const val WEB_VIEW_ACTIVITY_EXTRA = "WEB_VIEW_ACTIVITY_EXTRA"

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
        webView.addJavascriptInterface(JavaScriptMethods(this), "android")
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