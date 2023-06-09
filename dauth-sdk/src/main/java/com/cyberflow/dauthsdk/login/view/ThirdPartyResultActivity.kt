package com.cyberflow.dauthsdk.login.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauthsdk.login.DAuthSDK
import com.cyberflow.dauthsdk.login.callback.ThirdPartyCallback
import com.cyberflow.dauthsdk.login.google.GoogleLoginManager
import com.cyberflow.dauthsdk.login.impl.DAuthLogin
import com.cyberflow.dauthsdk.login.twitter.TwitterLoginManager
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ThirdPartyResultActivity : AppCompatActivity()   {

//    companion object {
//        var callback: ThirdPartyCallback? = null
//    }

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
                        DAuthLogger.e("twitter授权成功")
                    }

                    override fun failure(exception: TwitterException?) {
                        DAuthLogger.e("twitter授权失败:$exception")
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
        }
        DAuthLogger.d("ThirdPartyResultActivity onActivityResult")

    }


    override fun onDestroy() {
        super.onDestroy()
        DAuthLogger.d("ThirdPartyResultActivity onDestroy")
    }
}