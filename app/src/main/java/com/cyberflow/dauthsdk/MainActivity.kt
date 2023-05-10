package com.cyberflow.dauthsdk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.cyberflow.dauth.databinding.ActivityMainBinding
import com.cyberflow.dauthsdk.google.GoogleLoginManager
import com.cyberflow.dauthsdk.twitter.TwitterLoginManager
//import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.identity.TwitterLoginButton

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    var mainBinding: ActivityMainBinding?  = null
    private val binding: ActivityMainBinding get() = mainBinding!!
    lateinit var  loginButton :TwitterLoginButton
//    lateinit var callbackManager : CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(LayoutInflater.from(this))


        setContentView(binding.root)
        binding.btnMyTwitter.setOnClickListener {
            binding.twitterLogin.performClick()
        }

        binding.btnLogin.setOnClickListener {
            GoogleLoginManager.instance.googleSignInAuth(this)
        }


        loginButton = binding.twitterLogin
        twitterCallback()
    }
    private fun twitterCallback() {
        val loginButton = binding.twitterLogin
        loginButton.callback = object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>?) {
                val twitterToken = result?.data?.authToken
                Log.d(TAG,"success twitterToken$twitterToken")
            }

            override fun failure(exception: TwitterException?) {
                Log.d(TAG,"failed")
            }

        };
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //twitter
        loginButton.onActivityResult(requestCode, resultCode, data)

        //google
//        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//        GoogleLoginManager.instance.handleSignInResult(task)
    }


}