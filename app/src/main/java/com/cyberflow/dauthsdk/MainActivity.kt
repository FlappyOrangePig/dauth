package com.cyberflow.dauthsdk

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cyberflow.dauth.databinding.ActivityMainBinding
import com.cyberflow.dauthsdk.google.GoogleLoginManager
import com.cyberflow.dauthsdk.callback.BaseHttpCallback
import com.cyberflow.dauthsdk.model.LoginRes
import com.cyberflow.dauthsdk.twitter.TwitterLoginManager
import com.cyberflow.dauthsdk.utils.DAuthLogger
import com.cyberflow.dauthsdk.utils.JwtChallengeCode
import com.cyberflow.dauthsdk.view.WalletWebViewActivity

private const val TAG = "MainActivity"
private const val TWITTER_REQUEST_CODE = 140
private const val GOOGLE_REQUEST_CODE = 9001
private const val GOOGLE = "GOOGLE"
private const val TWITTER = "TWITTER"
private const val FACEBOOK = "FACEBOOK"

class MainActivity : AppCompatActivity() {
    var mainBinding: ActivityMainBinding?  = null
    private val binding: ActivityMainBinding get() = mainBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(LayoutInflater.from(this))

        setContentView(binding.root)

        binding.btnDauthLogin.setOnClickListener {
            val account = binding.edtAccount.text.toString()
            val password = binding.edtPassword.text.toString()
            DAuthSDK.instance.login(account,password, object : BaseHttpCallback<LoginRes> {

                override fun onResult(obj: String?) {
                    DAuthLogger.d("登录成功返回: $obj")
                }

                override fun onFailed(errorMsg: String) {
                    DAuthLogger.d("登录失败返回: $errorMsg")
                }

            })

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
//            DAuthSDK.instance.loginWithType(GOOGLE, this)

            //DAuth 授权接口测试
            val codeVerifier = JwtChallengeCode().generateCodeVerifier()
            val codeChallenge = JwtChallengeCode().generateCodeChallenge(codeVerifier)
            DAuthLogger.d("codeVerify == $codeVerifier")
            val code = DAuthSDK.instance.loginAuth(codeChallenge)
//
//            //DAuth 授权接口测试
            DAuthSDK.instance.getDAuthToken(codeVerifier,code)

        }

        binding.ivTwitter.setOnClickListener {
            DAuthSDK.instance.loginWithType(TWITTER, this)
        }

        binding.ivWallet.setOnClickListener {
            WalletWebViewActivity.launch(this, false)
        }

        binding.tvSendCode.setOnClickListener {
            val account = binding.edtAccount.text.toString()
            val isSend = DAuthSDK.instance.sendEmailVerifyCode(account)
            if(isSend) {
                Toast.makeText(this, "验证码发送成功", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            TWITTER_REQUEST_CODE -> {
                TwitterLoginManager.instance.onActivityResult(requestCode, resultCode, data)
            }
            GOOGLE_REQUEST_CODE -> {
                GoogleLoginManager.instance.onActivityResult(data)
            }
        }
    }


}