package com.cyberflow.dauthsdk

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauth.databinding.ActivityLoginLayoutBinding
import com.cyberflow.dauthsdk.api.entity.CreateWalletResult
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.login.callback.ThirdPartyCallback
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import kotlinx.coroutines.launch


private const val GOOGLE = "GOOGLE"
private const val TWITTER = "TWITTER"
private const val FACEBOOK = "FACEBOOK"
private const val ACCOUNT_TYPE_OF_EMAIL = 10

class LoginActivity : AppCompatActivity() {
    var loginBinding: ActivityLoginLayoutBinding?  = null
    private val binding: ActivityLoginLayoutBinding get() = loginBinding!!

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
                    ACCOUNT_TYPE_OF_EMAIL
                )
                when (code) {
                    0 -> MainActivity.launch(this@LoginActivity)
                    10001 -> {
                        val createWalletCode = DAuthSDK.instance.createWallet("Tt123456")
//                        if (createWalletCode == 0) {
//                            MainActivity.launch(this@LoginActivity)
//                        }
                    }
                    else -> DAuthLogger.d("login return code == $code")
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
            lifecycleScope.launch {
                DAuthSDK.instance.loginWithType(GOOGLE, this@LoginActivity, object: ThirdPartyCallback {
                    override fun onResult(code: Int?) {
                        DAuthLogger.e("谷歌授权登录返回code:$code")
                        when (code) {
                            0 -> handleLoginSuccess()
                            10001 -> handleCreateWallet()
                            else -> handleLoginFailure(code)
                        }
                    }

                })
            }
        }

        binding.ivTwitter.setOnClickListener {
            lifecycleScope.launch {
                val code = DAuthSDK.instance.loginWithType(TWITTER, this@LoginActivity)
                when (code) {
                    0 -> handleLoginSuccess()
                    10001 -> handleCreateWallet()
                    else -> handleLoginFailure(code)
                }
            }
        }

        binding.ivWallet.setOnClickListener {
            DAuthSDK.instance.link2EOAWallet(this)
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

    private fun handleLoginSuccess() {
        MainActivity.launch(this@LoginActivity)
    }

    private fun handleCreateWallet() {
        lifecycleScope.launch {
            val createWalletRes = DAuthSDK.instance.createWallet("Tt123456")
            if (createWalletRes is CreateWalletResult.Success) {
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