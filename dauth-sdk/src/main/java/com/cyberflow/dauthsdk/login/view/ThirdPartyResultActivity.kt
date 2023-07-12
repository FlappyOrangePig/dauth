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
private const val GOOGLE_OPEN_SERVICE_REQUEST_CODE = 9005
private const val TWITTER_REQUEST_CODE = 140

class ThirdPartyResultActivity : AppCompatActivity() {
    companion object {
        private var callback: ThirdPartyCallback? = null
        private const val LAUNCH_TYPE = "LAUNCH_TYPE"

        const val LAUNCH_TYPE_GOOGLE = 0
        const val LAUNCH_TYPE_TWITTER = 1
        fun launch(launchType: Int, context: Context, callback: ThirdPartyCallback) {
            val intent = Intent(context, ThirdPartyResultActivity::class.java)
            intent.putExtra(LAUNCH_TYPE, launchType)
            this.callback = callback
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DAuthLogger.d("ThirdPartyResultActivity onCreate")
        when (getLaunchType()) {
            LAUNCH_TYPE_GOOGLE -> {
                GoogleLoginManager.instance.googleSignInAuth(
                    this,
                    GOOGLE_REQUEST_CODE,
                    GOOGLE_OPEN_SERVICE_REQUEST_CODE
                )
            }

            LAUNCH_TYPE_TWITTER -> {
                TwitterLoginManager.instance.twitterLoginAuth(
                    this,
                    object : Callback<TwitterSession>() {
                        override fun success(result: Result<TwitterSession>?) {
                            DAuthLogger.e("twitter授权成功")
                        }

                        override fun failure(exception: TwitterException?) {
                            DAuthLogger.e("twitter授权失败：$exception")
                            finish()
                        }
                    })
            }

            else -> {
                finish()
            }
        }
    }

    private fun getLaunchType(): Int {
        return intent.getIntExtra(LAUNCH_TYPE, LAUNCH_TYPE_GOOGLE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        DAuthLogger.d("ThirdPartyResultActivity onActivityResult $requestCode")
        when (requestCode) {
            GOOGLE_REQUEST_CODE -> {
                lifecycleScope.launch {
                    val loginResultData = GoogleLoginManager.instance.googleAuthLogin(data)
                    dispatchResult(loginResultData)
                    finish()
                }
            }

            TWITTER_REQUEST_CODE -> {
                lifecycleScope.launch {
                    val loginResultData = TwitterLoginManager.instance.twitterAuthLogin(
                            requestCode,
                            resultCode,
                            data
                        )
                    dispatchResult(loginResultData)
                    finish()
                }
            }

            GOOGLE_OPEN_SERVICE_REQUEST_CODE -> {
                finish()
            }
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