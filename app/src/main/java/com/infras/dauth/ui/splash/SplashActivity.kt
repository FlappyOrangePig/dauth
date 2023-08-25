package com.infras.dauth.ui.splash

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.infras.dauth.R
import com.infras.dauth.app.BaseActivity
import com.infras.dauth.ext.launchMainPage
import com.infras.dauth.manager.AccountManager
import com.infras.dauth.manager.sdk
import com.infras.dauth.ui.login.LoginActivity
import com.infras.dauth.util.ToastUtil
import com.infras.dauthsdk.api.entity.ResponseCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity() {

    private val sdk get() = sdk()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = FrameLayout(this).also { fl ->
            TextView(fl.context).also { tv ->
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40F)
                tv.text = getString(R.string.app_name)
                tv.setTextColor(Color.BLACK)
                tv.typeface = Typeface.DEFAULT_BOLD
                val lp = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).also { it.gravity = Gravity.CENTER }
                fl.addView(tv, lp)
            }
        }
        setContentView(root)
        performAutoLogin()
    }

    private fun performAutoLogin() {
        lifecycleScope.launch {
            val walletExists = AccountManager.isWalletExists()
            val a = this@SplashActivity
            var isLoggedIn = false
            if (walletExists) {
                val accountResult = sdk.queryAccountByAuthid()
                if (accountResult == null) {
                    ToastUtil.show(a, "网络错误")
                } else {
                    val ret = accountResult.ret
                    when {
                        accountResult.isSuccess() -> {
                            isLoggedIn = true
                        }

                        ResponseCode.isLoggedOut(ret) -> {
                            ToastUtil.show(a, "登录状态已失效 $ret")
                            sdk.logout()
                        }

                        else -> {
                            ToastUtil.show(a, "登录失败 $ret")
                        }
                    }
                }
            } else {
                delay(100L)
            }
            if (isLoggedIn) {
                a.launchMainPage()
            } else {
                LoginActivity.launch(a)
            }
            finish()
        }
    }
}