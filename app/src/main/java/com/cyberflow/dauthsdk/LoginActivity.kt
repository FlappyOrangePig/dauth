package com.cyberflow.dauthsdk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauth.databinding.ActivityLoginLayoutBinding
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import kotlinx.coroutines.launch


private const val GOOGLE = "GOOGLE"
private const val TWITTER = "TWITTER"
private const val FACEBOOK = "FACEBOOK"
private const val ACCOUNT_TYPE_OF_EMAIL = 10
private const val WALLET_IS_NOT_CREATE = 200001

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
                    200001 -> {
                        handleCreateWallet()
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
                val code = DAuthSDK.instance.loginWithType(GOOGLE, this@LoginActivity)
                when (code) {
                    0 -> handleLoginSuccess()
                    WALLET_IS_NOT_CREATE -> handleCreateWallet()
                    else -> handleLoginFailure(code)
                }
            }
        }

        binding.ivTwitter.setOnClickListener {
            lifecycleScope.launch {
                val code = DAuthSDK.instance.loginWithType(TWITTER, this@LoginActivity)
                when (code) {
                    0 -> handleLoginSuccess()
                    WALLET_IS_NOT_CREATE -> handleCreateWallet()
                    else -> handleLoginFailure(code)
                }
            }
        }

        binding.ivWallet.setOnClickListener {
            lifecycleScope.launch {
                val code = DAuthSDK.instance.link2EOAWallet(this@LoginActivity)
                DAuthLogger.d("EOA钱包登录返回code:$code")
                when(code) {
                    0 -> MainActivity.launch(this@LoginActivity)
                    WALLET_IS_NOT_CREATE -> handleCreateWallet()
                    else -> handleLoginFailure(code)
                }
            }
        }

        binding.tvSendCode.setOnClickListener {
            val account = binding.edtAccount.text.toString()
            lifecycleScope.launch {
                val isSend = DAuthSDK.instance.sendEmailVerifyCode(account)
                if (isSend) {
                    ToastUtil.show(applicationContext, "验证码发送成功")
                    DAuthLogger.d("验证码发送成功")
                } else {
                    ToastUtil.show(applicationContext, "验证码发送失败")
                }
            }
        }

        binding.tvSwitchPwd.setOnClickListener {
            binding.tvSendCode.visibility = View.GONE
        }

        binding.btnQueryAccount.setOnClickListener {
            val email = binding.edtAccount.text.toString()
            lifecycleScope.launch {
                val accountRes = DAuthSDK.instance.queryAccountByEmail(email)
                DAuthLogger.d("用户信息：${accountRes?.data?.address}")
            }
        }

    }

    private fun handleLoginSuccess() {
        MainActivity.launch(this@LoginActivity)
    }

    //创建aa钱包
    private fun handleCreateWallet() {
        lifecycleScope.launch {
            val createWalletRes = DAuthSDK.instance.createWallet("Tt123456")
            if (createWalletRes is DAuthResult.Success) {
                val address = createWalletRes.data.address
                DAuthLogger.d("创建的aa钱包地址：$address")
                MainActivity.launch(this@LoginActivity)
            }
        }
    }

    private fun handleLoginFailure(errorCode: Int?) {
        val errorMessage = "登录失败 errorCode==$errorCode"
        ToastUtil.show(this@LoginActivity, errorMessage)
        DAuthLogger.e("login errorCode == $errorCode")
    }


}