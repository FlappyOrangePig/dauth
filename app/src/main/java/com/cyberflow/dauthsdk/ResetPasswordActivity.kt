package com.cyberflow.dauthsdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauth.databinding.ActivityResetPwdLayoutBinding
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.login.callback.ResetPwdCallback
import kotlinx.coroutines.launch

class ResetPasswordActivity: AppCompatActivity() {

    private var _binding: ActivityResetPwdLayoutBinding ?= null
    private val binding: ActivityResetPwdLayoutBinding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityResetPwdLayoutBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        initView()
    }

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, ResetPasswordActivity::class.java)
            context.startActivity(intent)
        }
    }

    private fun initView() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnResetPwd.setOnClickListener {
            DAuthSDK.instance.setRecoverPassword(object : ResetPwdCallback {
                override fun success() {
                    Toast.makeText(this@ResetPasswordActivity, "重置密码成功", Toast.LENGTH_SHORT).show()
                    finish()
                }

                override fun failed() {
                    Toast.makeText(this@ResetPasswordActivity, "重置密码失败", Toast.LENGTH_SHORT).show()
                }

            })
        }

        binding.tvSendCode.setOnClickListener {
            Toast.makeText(this, "验证码发送成功", Toast.LENGTH_SHORT).show()
            val account = binding.edtVerifyCode.text.toString()
            lifecycleScope.launch {
                DAuthSDK.instance.sendEmailVerifyCode(account)
            }
        }


    }
}