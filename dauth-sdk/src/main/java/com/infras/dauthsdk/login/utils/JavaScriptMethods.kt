package com.infras.dauthsdk.login.utils

import android.webkit.JavascriptInterface
import androidx.appcompat.app.AppCompatActivity
import com.infras.dauthsdk.login.callback.WalletCallback
import com.infras.dauthsdk.login.infrastructure.Serializer
import com.infras.dauthsdk.login.model.DAuthUser
import com.infras.dauthsdk.login.model.WalletRes
import com.google.gson.Gson
import com.google.gson.JsonIOException


class JavaScriptMethods constructor( private val activity: AppCompatActivity, private val callback: WalletCallback?){
    @JavascriptInterface
    fun postAddress(data: String?) {
        try {
            DAuthLogger.d("回调js返回的钱包地址:$data")
            val adapter = Serializer.moshi.adapter(WalletRes::class.java)
            val walletRes = adapter.fromJson(data.toString())
            val openId = walletRes?.address
            val userData = DAuthUser()
            userData.openid = openId
            val userAdapter = Serializer.moshi.adapter(DAuthUser::class.java)
            callback?.onResult(userAdapter.toJson(userData))
            activity.finish()
        } catch (e: JsonIOException) {
            DAuthLogger.e("JavaScriptMethods JsonIOException:$e")
        }
    }

}