package com.cyberflow.dauthsdk.login.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauthsdk.login.callback.ThirdPartyCallback
import com.cyberflow.dauthsdk.login.google.GoogleLoginManager
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import kotlinx.coroutines.launch


class ThirdPartyResultActivity : AppCompatActivity() {
    companion object {
        private var callback: ThirdPartyCallback? = null
        fun launch(context: Context, callback: ThirdPartyCallback?) {
            val intent = Intent(context, ThirdPartyResultActivity::class.java)
            this.callback = callback
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DAuthLogger.d("ThirdPartyResultActivity onCreate")
        GoogleLoginManager.instance.googleSignInAuth(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        lifecycleScope.launch {
            val code = GoogleLoginManager.instance.googleAuthLogin(data)
            dispatchResult(code)
            finish()
        }
        DAuthLogger.d("ThirdPartyResultActivity onActivityResult")

    }

    override fun onDestroy() {
        super.onDestroy()
        DAuthLogger.d("ThirdPartyResultActivity onDestroy")
        dispatchResult(Integer.MAX_VALUE)
    }

    private fun dispatchResult(code: Int) {
        callback?.let {
            it.onResult(code)
            callback = null
        }
    }
}