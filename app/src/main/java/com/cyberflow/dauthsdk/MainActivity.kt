package com.cyberflow.dauthsdk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauth.databinding.ActivityMainBinding
import com.cyberflow.dauthsdk.constant.LoginType
import com.cyberflow.dauthsdk.google.GoogleLoginManager
import com.cyberflow.dauthsdk.login.DAuthUser
import com.cyberflow.dauthsdk.model.AuthorizeParam
import com.cyberflow.dauthsdk.model.AuthorizeToken2Param
import com.cyberflow.dauthsdk.model.CreateAccountParam
import com.cyberflow.dauthsdk.network.AccountApi
import com.cyberflow.dauthsdk.twitter.TwitterLoginManager
import com.cyberflow.dauthsdk.twitter.TwitterLoginUtils
import com.cyberflow.dauthsdk.utils.DAuthLogger
import com.cyberflow.dauthsdk.utils.SignUtils
import com.cyberflow.dauthsdk.view.WalletWebViewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"
private const val TWITTER_REQUEST_CODE = 140
private const val GOOGLE_REQUEST_CODE = 9001
private const val GOOGLE = "GOOGLE"
private const val TWITTER = "TWITTER"
private const val FACEBOOK = "FACEBOOK"

class MainActivity : AppCompatActivity() {
    var mainBinding: ActivityMainBinding?  = null
    private val binding: ActivityMainBinding get() = mainBinding!!
    private var googleUser: DAuthUser ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(LayoutInflater.from(this))

        setContentView(binding.root)

        binding.btnDauthLogin.setOnClickListener {
            val account = binding.edtAccount.text.toString()
            DAuthSDK.instance.login(account)
        }

        binding.tvForgetPwd.setOnClickListener {
            ResetPasswordActivity.launch(this)
        }

        binding.tvRegister.setOnClickListener {
            RegisterActivity.launch(this)
        }
        initView()
    }

    private fun initView() {
        binding.ivGoogle.setOnClickListener {
            DAuthSDK.instance.loginWithType(GOOGLE, this)
        }

        binding.ivTwitter.setOnClickListener {
            DAuthSDK.instance.loginWithType(TWITTER, this)
        }

        binding.ivWallet.setOnClickListener {
            WalletWebViewActivity.launch(this, false)
        }

        binding.tvSendCode.setOnClickListener {
            Toast.makeText(this, "验证码发送成功", Toast.LENGTH_SHORT).show()
            DAuthSDK.instance.sendVerifyCode()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            TWITTER_REQUEST_CODE -> {
                TwitterLoginManager.instance.onActivityResult(requestCode, resultCode, data)
            }
            GOOGLE_REQUEST_CODE -> {
                GoogleLoginManager.instance.onActivityResult(this, data)
            }
        }
    }


}