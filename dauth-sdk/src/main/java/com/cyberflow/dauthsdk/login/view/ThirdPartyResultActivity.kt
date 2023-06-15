package com.cyberflow.dauthsdk.login.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauthsdk.api.entity.LoginResultData
import com.cyberflow.dauthsdk.login.callback.ThirdPartyCallback
import com.cyberflow.dauthsdk.login.google.GoogleLoginManager
import com.cyberflow.dauthsdk.login.twitter.TwitterLoginManager
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import kotlinx.coroutines.launch


private const val GOOGLE_REQUEST_CODE = 9004
private const val TWITTER_REQUEST_CODE = 140

class ThirdPartyResultActivity : AppCompatActivity() {
    companion object {
        private var callback: ThirdPartyCallback? = null
        private const val EXTRA_REQUEST_CODE = "EXTRA_REQUEST_CODE"
        fun launch(requestCode: Int, context: Context, callback: ThirdPartyCallback) {
            val intent = Intent(context, ThirdPartyResultActivity::class.java)
            intent.putExtra(EXTRA_REQUEST_CODE, requestCode)
            this.callback = callback
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DAuthLogger.d("ThirdPartyResultActivity onCreate")
        when (getIntentExtra()) {
            GOOGLE_REQUEST_CODE -> {
                GoogleLoginManager.instance.googleSignInAuth(this)
            }
            TWITTER_REQUEST_CODE -> {
                TwitterLoginManager.instance.twitterLoginAuth(
                    this,
                    object : Callback<TwitterSession>() {
                        override fun success(result: Result<TwitterSession>?) {
                            DAuthLogger.e("twitter授权成功")
                        }

                        override fun failure(exception: TwitterException?) {
                            DAuthLogger.e("twitter授权失败：$exception")
                        }

                    })
            }
        }
    }

    private fun getIntentExtra() : Int {
       return intent.getIntExtra(EXTRA_REQUEST_CODE,0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var loginResultData : LoginResultData? = null
        lifecycleScope.launch {
            when (requestCode) {
                GOOGLE_REQUEST_CODE -> {
                    lifecycleScope.launch {
                        loginResultData = GoogleLoginManager.instance.googleAuthLogin(data)
                        dispatchResult(loginResultData)
                        finish()
                    }
                }

                TWITTER_REQUEST_CODE -> {
                    lifecycleScope.launch {
                        loginResultData = TwitterLoginManager.instance.twitterAuthLogin(
                                requestCode,
                                resultCode,
                                data
                            )
                        dispatchResult(loginResultData)
                        finish()
                    }
                }
            }

            DAuthLogger.d("ThirdPartyResultActivity onActivityResult")
        }
    }
    private fun dispatchResult(loginResultData: LoginResultData?) {
        callback?.let {
            it.onResult(loginResultData)
            callback = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DAuthLogger.d("ThirdPartyResultActivity onDestroy")
        dispatchResult(LoginResultData.Failure(Integer.MAX_VALUE))
    }
}