package com.cyberflow.dauthsdk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauth.databinding.ActivityMainBinding
import com.cyberflow.dauthsdk.constant.LoginType
import com.cyberflow.dauthsdk.google.GoogleLoginManager
import com.cyberflow.dauthsdk.login.DAuthSDK
import com.cyberflow.dauthsdk.login.DAuthUser
import com.cyberflow.dauthsdk.model.AuthorizeParam
import com.cyberflow.dauthsdk.model.AuthorizeToken2Param
import com.cyberflow.dauthsdk.model.CreateAccountParam
import com.cyberflow.dauthsdk.network.AccountApi
import com.cyberflow.dauthsdk.twitter.TwitterLoginManager
import com.cyberflow.dauthsdk.twitter.TwitterLoginUtils
import com.cyberflow.dauthsdk.utils.DAuthLogger
import com.cyberflow.dauthsdk.utils.SignUtils
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterCore
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.identity.TwitterLoginButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    var mainBinding: ActivityMainBinding?  = null
    private val binding: ActivityMainBinding get() = mainBinding!!
    private var googleUser: DAuthUser ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(LayoutInflater.from(this))


        setContentView(binding.root)
        binding.btnMyTwitter.setOnClickListener {
            DAuthSDK.instance.loginWithType(LoginType.TWITTER,this@MainActivity)
        }

        binding.btnDauthLogin.setOnClickListener {
            DAuthSDK.instance.initSDK(this,"","")
        }

        binding.btnGoogleLogin.setOnClickListener {
            DAuthSDK.instance.loginWithType(LoginType.GOOGLE,this@MainActivity)

        }

        twitterCallback()
    }
    private fun twitterCallback() {
        val loginButton = binding.twitterLogin
        loginButton.callback = object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>?) {
                val twitterTokenAndSecret = result?.data?.authToken
                val token = twitterTokenAndSecret?.token
                val s =  TwitterCore.getInstance().sessionManager.activeSession
                DAuthLogger.d("twitter auth success twitterToken:$token")
            }

            override fun failure(exception: TwitterException?) {
                DAuthLogger.e("twitter auth failed:$exception")
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //twitter
        TwitterLoginManager.instance.onActivityResult(requestCode, resultCode, data)
        TwitterLoginUtils(this).onActivityResult(requestCode, resultCode, data)
        //google
        googleUser = GoogleLoginManager.instance.onActivityResult(this,data)
    }


}