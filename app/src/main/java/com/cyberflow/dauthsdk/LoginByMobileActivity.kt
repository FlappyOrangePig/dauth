package com.cyberflow.dauthsdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauth.databinding.ActivityMobileLoginLayoutBinding
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.ResponseCode
import com.cyberflow.dauthsdk.api.entity.LoginResultData
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import kotlinx.coroutines.launch

private const val USER_TYPE_OF_MOBILE = 60  // 手机号登录
class LoginByMobileActivity: BaseActivity() {
    private var _binding: ActivityMobileLoginLayoutBinding?  = null
    private val binding: ActivityMobileLoginLayoutBinding get() = _binding!!

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, LoginByMobileActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMobileLoginLayoutBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        initView()
    }

    fun initView() {
        binding.tvSendCode.setOnClickListener {
            val phone = binding.edtAccount.text.toString()
            lifecycleScope.launch {
                DAuthSDK.instance.sendPhoneVerifyCode(phone,"86")
            }
        }

        binding.btnMobileLogin.setOnClickListener {
            val phone = binding.edtAccount.text.toString()
            val verifyCode = binding.edtVerifyCode.text.toString()
            lifecycleScope.launch {
                val result = DAuthSDK.instance.loginByMobileOrEmail(phone, verifyCode, USER_TYPE_OF_MOBILE)
                handleLoginResult(result)
            }
        }
    }

    private fun handleLoginResult(loginResultData: LoginResultData?) {
        when (loginResultData) {
            is LoginResultData.Success -> {
                val idToken = loginResultData.accessToken
                // 处理登录成功逻辑
                MainActivity.launch(this@LoginByMobileActivity)
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
                MainActivity.launch(this@LoginByMobileActivity)
            }
        }
    }
}