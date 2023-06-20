package com.cyberflow.dauthsdk

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauth.databinding.ActivityLoginLayoutBinding
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.ResponseCode
import com.cyberflow.dauthsdk.api.entity.LoginResultData
import com.cyberflow.dauthsdk.api.entity.TokenType
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import kotlinx.coroutines.launch


private const val GOOGLE = "GOOGLE"
private const val TWITTER = "TWITTER"
private const val FACEBOOK = "FACEBOOK"
private const val ACCOUNT_TYPE_OF_EMAIL = 10

class LoginActivity : BaseActivity() {
    var loginBinding: ActivityLoginLayoutBinding? = null
    private val binding: ActivityLoginLayoutBinding get() = loginBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginLayoutBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        initView()
    }

    override fun initView() {
        // 邮箱登录
        binding.btnDauthLogin.setOnClickListener {
            val account = binding.edtAccount.text.toString()
            val password = binding.edtPassword.text.toString()
            lifecycleScope.launch {
                val loginResultData = DAuthSDK.instance.loginByMobileOrEmail(
                    account,
                    password,
                    ACCOUNT_TYPE_OF_EMAIL
                )
                when (loginResultData) {
                    is LoginResultData.Success -> {
                        val idToken = loginResultData.idToken
                        // 处理登录成功逻辑
                        MainActivity.launch(this@LoginActivity)
                        DAuthLogger.d("登录成功，返回的ID令牌：$idToken")
                    }
                    is LoginResultData.Failure -> {
                        val failureCode = loginResultData.code
                        // 处理登录失败逻辑
                        DAuthLogger.d("登录失败，返回的errorCode：$failureCode")
                        if(failureCode == ResponseCode.AA_WALLET_IS_NOT_CREATE) {
                            handleCreateWallet()
                        }
                    }
                    else -> {}
                }
            }

        }

        binding.tvForgetPwd.setOnClickListener {
            ResetPasswordActivity.launch(this)
        }

        binding.tvRegister.setOnClickListener {
            RegisterActivity.launch(this)
        }

        binding.ivGoogle.setOnClickListener {
            lifecycleScope.launch {
                val loginResultData = DAuthSDK.instance.loginWithType(GOOGLE, this@LoginActivity)
                handleLoginResult(loginResultData)
            }
        }

        binding.ivTwitter.setOnClickListener {
            lifecycleScope.launch {
                val loginResultData = DAuthSDK.instance.loginWithType(TWITTER, this@LoginActivity)
                handleLoginResult(loginResultData)
            }
        }

        binding.ivWallet.setOnClickListener {
            lifecycleScope.launch {
                val code = DAuthSDK.instance.link2EOAWallet(this@LoginActivity)
                DAuthLogger.d("EOA钱包登录返回code:$code")
                handleLoginResult(code)
            }
        }

        binding.tvSendCode.setOnClickListener {
            val account = binding.edtAccount.text.toString()
            lifecycleScope.launch {
                val response = DAuthSDK.instance.sendEmailVerifyCode(account)
                if (response?.iRet == 0) {
                    ToastUtil.show(applicationContext, "验证码发送成功")
                    DAuthLogger.d("验证码发送成功")
                } else {
                    ToastUtil.show(applicationContext, "验证码发送失败")
                }
            }
        }

        binding.btnMobileLogin.setOnClickListener {
            LoginByMobileActivity.launch(this)
        }

    }


    private fun handleLoginResult(loginResultData: LoginResultData?) {
        when (loginResultData) {
            is LoginResultData.Success -> {
                val idToken = loginResultData.idToken
                // 处理登录成功逻辑
                MainActivity.launch(this@LoginActivity)
                DAuthLogger.d("登录成功，返回的ID令牌：$idToken")
            }
            is LoginResultData.Failure -> {
                val failureCode = loginResultData.code
                // 处理登录失败逻辑
                DAuthLogger.d("登录失败，返回的errorCode：$failureCode")
                if(failureCode == ResponseCode.AA_WALLET_IS_NOT_CREATE) {
                    handleCreateWallet()
                }
            }
            else -> {}
        }
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


    override fun onResume() {
        super.onResume()
        testWeb3()
    }

    private fun testWeb3() {
        lifecycleScope.launch {
            // 查询明达的MIMO-NFT列表
            /*val tokenIds = DAuthSDK.instance.queryWalletBalance(
                MING_DA_S_ADDRESS, TokenType.ERC721(
                    MIMO_AVATAR_CONTRACT_ADDRESS
                )
            )
            ToastUtil.show(this@LoginActivity, tokenIds.toString())*/

            // 查询sepolia-test上面某人的BULL_TOKEN
            val balance = DAuthSDK.instance.queryWalletBalance(
                Web3Const.ANY_ONE_WHO_RECEIVE_BULL_TOKEN_ON_SEPOLIA_TEST_NETWORK,
                TokenType.ERC20(Web3Const.BULL_TOKEN_CONTRACT_ADDRESS)
            )
            ToastUtil.show(this@LoginActivity, balance.toString())
        }
    }
}