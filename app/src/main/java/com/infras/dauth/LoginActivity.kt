package com.infras.dauth

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.infras.dauth.databinding.ActivityLoginLayoutBinding
import com.infras.dauth.manager.AccountManager
import com.infras.dauth.manager.sdk
import com.infras.dauth.util.HideApiUtil
import com.infras.dauth.util.LogUtil
import com.infras.dauth.widget.LoadingDialogFragment
import com.infras.dauthsdk.api.entity.DAuthResult
import com.infras.dauthsdk.api.entity.LoginResultData
import com.infras.dauthsdk.api.entity.ResponseCode
import kotlinx.coroutines.launch


private const val GOOGLE = "GOOGLE"
private const val TWITTER = "TWITTER"
private const val FACEBOOK = "FACEBOOK"
private const val ACCOUNT_TYPE_OF_EMAIL = 10

class LoginActivity : BaseActivity() {
    private var loginBinding: ActivityLoginLayoutBinding? = null
    private val binding: ActivityLoginLayoutBinding get() = loginBinding!!
    private val loadingDialog = LoadingDialogFragment.newInstance()
    private val sdk get() = sdk()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginLayoutBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        initView()
        initData()
    }

    private fun initView() {
        // 邮箱登录
        binding.btnDauthLogin.setOnClickListener {
            val account = binding.edtAccount.text.toString()
            val password = binding.edtPassword.text.toString()
            lifecycleScope.launch {
                val loginResultData = sdk.loginByMobileOrEmail(
                    account,
                    password,
                    ACCOUNT_TYPE_OF_EMAIL
                )
                when (loginResultData) {
                    is LoginResultData.Success -> {
                        if (loginResultData.needCreateWallet) {
                            handleCreateWallet()
                        } else {
                            val idToken = loginResultData.accessToken
                            // 处理登录成功逻辑
                            MainActivity.launch(this@LoginActivity)
                            LogUtil.d(logTag, "登录成功，返回的ID令牌：$idToken")
                        }
                    }

                    is LoginResultData.Failure -> {
                        val failureCode = loginResultData.code
                        // 处理登录失败逻辑
                        LogUtil.d(logTag, "登录失败，返回的errorCode：$failureCode")
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
                val loginResultData = sdk.loginWithType(GOOGLE, this@LoginActivity)
                handleLoginResult(loginResultData)
            }
        }

        binding.ivTwitter.setOnClickListener {
            lifecycleScope.launch {
                val loginResultData = sdk.loginWithType(TWITTER, this@LoginActivity)
                handleLoginResult(loginResultData)
            }
        }

        binding.ivWalletConnect.setOnClickListener {
            lifecycleScope.launch {
                kotlin.runCatching {
                    val connectResult =  HideApiUtil.getEoaApi().connectWallet()
                    ToastUtil.show(it.context, "$connectResult")
                }
            }
        }

        binding.tvSendCode.setOnClickListener {
            val account = binding.edtAccount.text.toString()
            lifecycleScope.launch {
                val response = sdk.sendEmailVerifyCode(account)
                if (response?.ret == 0) {
                    ToastUtil.show(applicationContext, "验证码发送成功")
                    LogUtil.d(logTag, "验证码发送成功")
                } else {
                    ToastUtil.show(applicationContext, "验证码发送失败")
                }
            }
        }

        binding.btnMobileLogin.setOnClickListener {
            LoginByMobileActivity.launch(this)
        }

        binding.tvDauth.setOnClickListener {
            WalletTestActivity.launch(it.context)
        }

        binding.ivMetamask.setOnClickListener {
            lifecycleScope.launch {
                // 待实现
            }
        }
    }


    private fun handleLoginResult(loginResultData: LoginResultData?) {
        when (loginResultData) {
            is LoginResultData.Success -> {
                if (loginResultData.needCreateWallet) {
                    handleCreateWallet()
                } else {
                    val idToken = loginResultData.accessToken
                    // 处理登录成功逻辑
                    MainActivity.launch(this@LoginActivity)
                    LogUtil.d(logTag, "登录成功，返回的ID令牌：$idToken")
                }
            }

            is LoginResultData.Failure -> {
                val failureCode = loginResultData.code
                // 处理登录失败逻辑
                LogUtil.d(logTag, "登录失败，返回的errorCode：$failureCode")
            }

            else -> {
                LogUtil.e(logTag, "用户取消授权")
            }
        }
    }

    //创建aa钱包
    private fun handleCreateWallet() {
        lifecycleScope.launch {
            loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
            val createWalletRes = sdk.createWallet(false)
            loadingDialog.dismiss()
            if (createWalletRes is DAuthResult.Success) {
                val address = createWalletRes.data.address
                LogUtil.d(logTag, "创建的aa钱包地址：$address")
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
            /*val tokenIds = sdk.queryWalletBalance(
                MING_DA_S_ADDRESS, TokenType.ERC721(
                    MIMO_AVATAR_CONTRACT_ADDRESS
                )
            )
            ToastUtil.show(this@LoginActivity, tokenIds.toString())*/

            // 查询sepolia-test上面某人的BULL_TOKEN
            /*val balance = sdk.queryWalletBalance(
                Web3Const.ANY_ONE_WHO_RECEIVE_BULL_TOKEN_ON_SEPOLIA_TEST_NETWORK,
                TokenType.ERC20(Web3Const.BULL_TOKEN_CONTRACT_ADDRESS)
            )
            ToastUtil.show(this@LoginActivity, balance.toString())*/
        }
    }

    private fun initData() {
        performAutoLogin()
    }

    private fun performAutoLogin() {
        lifecycleScope.launch {
            val walletExists = AccountManager.isWalletExists()
            if (!walletExists) {
                return@launch
            }

            loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
            val accountResult = sdk.queryAccountByAuthid()
            loadingDialog.dismiss()
            if (accountResult == null) {
                ToastUtil.show(this@LoginActivity, "网络错误")
                return@launch
            }

            val ret = accountResult.ret
            when {
                ret == ResponseCode.RESPONSE_CORRECT_CODE -> {
                    finish()
                    MainActivity.launch(this@LoginActivity)
                }

                ResponseCode.isLoggedOut(ret) -> {
                    ToastUtil.show(this@LoginActivity, "登录状态已失效 $ret")
                    sdk.logout()
                }

                else -> {
                    ToastUtil.show(this@LoginActivity, "登录失败 $ret")
                }
            }
        }
    }
}