package com.cyberflow.dauthsdk.login.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauthsdk.login.api.DAuthSDK
import com.cyberflow.dauthsdk.login.google.GoogleLoginManager
import com.cyberflow.dauthsdk.login.twitter.TwitterLoginManager
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import kotlinx.coroutines.launch


class ThirdPartyResultActivity : AppCompatActivity()   {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DAuthLogger.d("ThirdPartyResultActivity onCreate")
        val type = intent.getIntExtra("type",0)
        if(type == 0) {
            GoogleLoginManager.instance.googleSignInAuth(this)
        } else {
            lifecycleScope.launch {
                TwitterLoginManager.instance.twitterLoginAuth(this@ThirdPartyResultActivity,
                    object : Callback<TwitterSession>() {
                    override fun success(result: Result<TwitterSession>?) {
                        val authToken = result?.data?.authToken
                        val userId = result?.data?.userId
                        val userName = result?.data?.userName
                        DAuthLogger.e("twitter授权成功")

                    }

                    override fun failure(exception: TwitterException?) {
                        DAuthLogger.e("twitter授权失败:$exception")
                        finish()
                    }

                })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        lifecycleScope.launch {
            val code = DAuthSDK.instance.thirdPartyCallback(requestCode, resultCode, data)
            DAuthSDK.callback?.onResult(code)
            // 返回结果给 LoginActivity
            val resultIntent = Intent()
            resultIntent.putExtra("code", code)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
            DAuthLogger.i("ThirdPartyResultActivity finish")
        }
        DAuthLogger.d("ThirdPartyResultActivity onActivityResult")

    }


    override fun onDestroy() {
        super.onDestroy()
        DAuthLogger.d("ThirdPartyResultActivity onDestroy")
    }
}