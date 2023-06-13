package com.cyberflow.dauthsdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauth.databinding.ActivityLoginLayoutBinding
import com.cyberflow.dauth.databinding.ActivityMobileLoginLayoutBinding
import com.cyberflow.dauthsdk.api.DAuthSDK
import kotlinx.coroutines.launch

class LoginByMobileActivity: AppCompatActivity() {
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

    private fun initView() {
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
                val code = DAuthSDK.instance.loginByMobileOrEmail(phone, verifyCode, 60)
                if(code == 0) {
                    MainActivity.launch(this@LoginByMobileActivity)
                }
            }
        }
    }
}