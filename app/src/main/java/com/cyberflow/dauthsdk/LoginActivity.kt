package com.cyberflow.dauthsdk

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauth.databinding.ActivityLoginLayoutBinding
import com.cyberflow.dauthsdk.login.callback.ThirdPartyCallback
import com.cyberflow.dauthsdk.login.const.LoginConst.ACCOUNT_TYPE_OF_EMAIL
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.login.view.WalletWebViewActivity
import kotlinx.coroutines.launch


private const val GOOGLE = "GOOGLE"
private const val TWITTER = "TWITTER"
private const val FACEBOOK = "FACEBOOK"

class LoginActivity : AppCompatActivity() {
    var loginBinding: ActivityLoginLayoutBinding?  = null
    private val binding: ActivityLoginLayoutBinding get() = loginBinding!!
    private var thirdPartyCallback : ThirdPartyCallback? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginLayoutBinding.inflate(LayoutInflater.from(this))

        setContentView(binding.root)
        binding.btnDauthLogin.setOnClickListener {
            val account = binding.edtAccount.text.toString()
            val password = binding.edtPassword.text.toString()
            lifecycleScope.launch {
                val code = DAuthSDK.instance.loginByMobileOrEmail(
                    account,
                    password,
                    ACCOUNT_TYPE_OF_EMAIL.toInt()
                )
                if (code == 0) {
                    MainActivity.launch(this@LoginActivity)
                } else if (code == 10001) {     //该邮箱没有钱包 调用创建钱包接口
                    val createWalletCode = DAuthSDK.instance.createWallet("Tt123456")
                    if(createWalletCode == 0) {
                        MainActivity.launch(this@LoginActivity)
                    }
                } else {
                    //1000032 验证码已过期
                    DAuthLogger.d("login return code == $code")
                }
            }

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
            thirdPartyCallback()
            lifecycleScope.launch {
                DAuthSDK.instance.loginWithType(GOOGLE, this@LoginActivity)
            }
        }

        binding.ivTwitter.setOnClickListener {
            thirdPartyCallback()
            lifecycleScope.launch {
                DAuthSDK.instance.loginWithType(TWITTER, this@LoginActivity)
            }
        }

        binding.ivWallet.setOnClickListener {
            WalletWebViewActivity.launch(this, false)
        }

        binding.tvSendCode.setOnClickListener {
            val account = binding.edtAccount.text.toString()
            lifecycleScope.launch {
                val isSend = DAuthSDK.instance.sendEmailVerifyCode(account)
                if (isSend) {
                    Toast.makeText(applicationContext, "验证码发送成功", Toast.LENGTH_SHORT).show()
                    DAuthLogger.d("验证码发送成功")
                } else {
                    Toast.makeText(applicationContext, "验证码发送失败", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }

    private fun thirdPartyCallback() {
        DAuthSDK.callback = object : ThirdPartyCallback {
            override fun onResult(code: Int?) {
                when (code) {
                    0 -> handleLoginSuccess()
                    10001 -> handleCreateWallet()
                    else -> handleLoginFailure(code)
                }
            }
        }
    }

    private fun handleLoginSuccess() {
        MainActivity.launch(this@LoginActivity)
    }

    private fun handleCreateWallet() {
        lifecycleScope.launch {
            val createWalletRes = DAuthSDK.instance.createWallet("Tt123456")
            if (createWalletRes == 0) {
                MainActivity.launch(this@LoginActivity)
            }
        }
    }

    private fun handleLoginFailure(errorCode: Int?) {
        val errorMessage = "登录失败 errorCode==$errorCode"
        Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
        DAuthLogger.e("login errorCode == $errorCode")
    }


}