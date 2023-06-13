package com.cyberflow.dauthsdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauth.databinding.ActivityResetPwdLayoutBinding
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.login.model.ResetByPasswordParam
import kotlinx.coroutines.launch

private const val USER_TYPE_OF_EMAIL = 10
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
            val account = binding.edtAccount.text.toString()
            val password = binding.edtNewPwd.text.toString()
            val verifyCode = binding.edtVerifyCode.text.toString()
            if(password.length < 8) {
                ToastUtil.show(this, "请输入8-16位包括大小写英文和数字的密码")
            } else {
                lifecycleScope.launch {
                    val params = ResetByPasswordParam(
                        USER_TYPE_OF_EMAIL, account = account,
                        verify_code = verifyCode, password = password
                    )
                    val isSuccess = DAuthSDK.instance.setRecoverPassword(params)
                    if (isSuccess) {
                        ToastUtil.show(
                            this@ResetPasswordActivity, "重置密码成功")
                        this@ResetPasswordActivity.finish()
                    }
                }
            }
        }

        binding.tvSendCode.setOnClickListener {
            val account = binding.edtAccount.text.toString()
            lifecycleScope.launch {
                val isSend = DAuthSDK.instance.sendEmailVerifyCode(account)
                if(isSend) {
                    ToastUtil.show(this@ResetPasswordActivity, "验证码发送成功")
                }
            }
        }


    }
}