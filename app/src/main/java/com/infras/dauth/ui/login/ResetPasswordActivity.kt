package com.infras.dauth.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.infras.dauth.app.BaseActivity
import com.infras.dauth.databinding.ActivityResetPwdLayoutBinding
import com.infras.dauth.ext.launch
import com.infras.dauth.manager.sdk
import com.infras.dauth.util.ToastUtil
import com.infras.dauthsdk.login.model.ResetByPasswordParam
import kotlinx.coroutines.launch

private const val USER_TYPE_OF_EMAIL = 10

class ResetPasswordActivity : BaseActivity() {

    private var _binding: ActivityResetPwdLayoutBinding? = null
    private val binding: ActivityResetPwdLayoutBinding get() = _binding!!
    private val sdk get() = sdk()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityResetPwdLayoutBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        initView()
    }

    companion object {
        fun launch(context: Context) {
            context.launch(ResetPasswordActivity::class.java)
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
                    val setPasswordData = sdk.setRecoverPassword(params)
                    if (setPasswordData.code == 0) {
                        ToastUtil.show(
                            this@ResetPasswordActivity, "重置密码成功"
                        )
                        this@ResetPasswordActivity.finish()
                    } else {
                        ToastUtil.show(
                            this@ResetPasswordActivity, "${setPasswordData.msg}"
                        )
                    }
                }
            }
        }

        binding.tvSendCode.setOnClickListener {
            val account = binding.edtAccount.text.toString()
            lifecycleScope.launch {
                val response = sdk.sendEmailVerifyCode(account)
                if(response?.ret == 0) {
                    ToastUtil.show(this@ResetPasswordActivity, "验证码发送成功")
                }
            }
        }
    }
}