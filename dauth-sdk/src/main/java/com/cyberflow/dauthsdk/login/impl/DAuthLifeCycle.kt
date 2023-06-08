package com.cyberflow.dauthsdk.login.impl

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import java.lang.reflect.Field

object DAuthLifeCycle : ActivityLifecycleCallbacks {

    // 授权时不输入或输入错误的密码时Twitter-sdk会报JavaScript不可用
    // Twitter本来就不想让我们进到另外的H5页面去登录，这是正常的流程
    // 如果hook了WebView，可以在输入错误密码时进入到另外的H5页面，但是这样可能会有安全隐患
    private const val HOOK = false
    private const val TWITTER_ACTIVITY = "com.twitter.sdk.android.core.identity.OAuthActivity"

    private inline fun Any.accessField(
        fieldName: String,
        crossinline block: (Field) -> Any?
    ): Any? {
        val field = this.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        val r = block.invoke(field)
        field.isAccessible = false
        return r
    }

    private fun Any.getFieldValue(fieldName: String): Any? {
        return accessField(fieldName) {
            it.get(this)
        }
    }

    private fun Any.setFieldValue(fieldName: String, value: Any?) {
        accessField(fieldName) {
            it.set(this, value)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun Activity.fixTwitterH5() {
        if (!HOOK) {
            DAuthLogger.d("do not hook")
            return
        }
        val isTwitter = this::class.java.name == TWITTER_ACTIVITY
        if (!isTwitter) {
            return
        }
        DAuthLogger.d("twitter page")
        try {
            val oAuthController = this.getFieldValue("oAuthController")!!
            val webView = oAuthController.getFieldValue("webView")
            DAuthLogger.d("webView=$webView")
            val wrapper = TwitterWebView(webView as WebView).also {
                it.visibility = View.GONE
            }
            oAuthController.setFieldValue("webView", wrapper)
            DAuthLogger.d("fix success")
        } catch (e: Exception) {
            DAuthLogger.e(e.stackTraceToString())
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        DAuthLogger.d("onCreate${activity.javaClass.simpleName}")
    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {
        activity.fixTwitterH5()
    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }
}