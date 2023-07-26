package com.cyberflow.dauthsdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauth.databinding.ActivityRegisterLayoutBinding
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.widget.LoadingDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RegisterActivity : BaseActivity() {
    private var _binding: ActivityRegisterLayoutBinding? = null
    private val binding: ActivityRegisterLayoutBinding get() = _binding!!
    private val loadingDialog = LoadingDialogFragment.newInstance()

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, RegisterActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterLayoutBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.initView()
    }

    private fun ActivityRegisterLayoutBinding.initView() {
        ivBack.setOnClickListener {
            finish()
        }

        btnDauthRegister.setOnClickListener {
            val context = it.context
            val account = edtAccount.text.toString()
            val password = edtPassword.text.toString()
            val ensurePassword = edtEnsurePassword.text.toString()
            if (account.isNotEmpty() && password.isNotEmpty() && ensurePassword.isNotEmpty()) {
                lifecycleScope.launch {
                    loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
                    val code = DAuthSDK.instance.createDAuthAccount(
                        account,
                        password,
                        ensurePassword
                    )
                    loadingDialog.dismiss()
                    if (code != null) {
                        if (code == 0) {
                            MainActivity.launch(context)
                        } else {
                            ToastUtil.show(context, "创建自有账号失败 errorCode: $code")
                        }
                    }
                }
            } else {
                ToastUtil.show(context, "请输入账号或密码")
            }
        }
    }
}