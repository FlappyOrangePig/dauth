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
import com.cyberflow.dauthsdk.login.DAuthUser
import com.cyberflow.dauthsdk.model.AuthorizeParam
import com.cyberflow.dauthsdk.model.AuthorizeToken2Param
import com.cyberflow.dauthsdk.model.CreateAccountParam
import com.cyberflow.dauthsdk.network.AccountApi
import com.cyberflow.dauthsdk.twitter.TwitterLoginManager
import com.cyberflow.dauthsdk.twitter.TwitterLoginUtils
import com.cyberflow.dauthsdk.utils.DAuthLogger
import com.cyberflow.dauthsdk.utils.SignUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"
private const val TWITTER_REQUEST_CODE = 140
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

//        twitterCallback()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //twitter
        if(requestCode == TWITTER_REQUEST_CODE) {
            TwitterLoginManager.instance.onActivityResult(requestCode, resultCode, data)
        }
        //google
        googleUser = GoogleLoginManager.instance.onActivityResult(this,data)
    }


}