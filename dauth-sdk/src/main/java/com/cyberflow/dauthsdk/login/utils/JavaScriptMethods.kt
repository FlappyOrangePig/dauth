package com.cyberflow.dauthsdk.login.utils

import android.webkit.JavascriptInterface
import androidx.appcompat.app.AppCompatActivity
import com.cyberflow.dauthsdk.login.callback.WalletCallback
import com.cyberflow.dauthsdk.login.model.DAuthUser
import com.cyberflow.dauthsdk.login.model.WalletRes
import com.google.gson.Gson
import com.google.gson.JsonIOException


class JavaScriptMethods constructor( private val activity: AppCompatActivity, private val callback: WalletCallback?){
    @JavascriptInterface
    fun postAddress(data: String?) {
        try {
            DAuthLogger.d("回调js返回的钱包地址:$data")
            val gson = Gson()
            val walletRes = gson.fromJson(data, WalletRes::class.java)
            val openId = walletRes.address
            val userData = DAuthUser()
            userData.openid = openId
            val userDataStr = gson.toJson(userData)
            callback?.onResult(userDataStr)
            activity.finish()
        } catch (e: JsonIOException) {
            DAuthLogger.e("JavaScriptMethods JsonIOException:$e")
        }
    }

}