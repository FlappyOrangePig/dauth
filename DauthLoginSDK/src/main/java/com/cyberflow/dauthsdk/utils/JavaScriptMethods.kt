package com.cyberflow.dauthsdk.utils

import android.webkit.JavascriptInterface
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonIOException

class JavaScriptMethods constructor( private val activity: AppCompatActivity){

    @JavascriptInterface
    fun postAddress(data:String?) {
        try {
            DAuthLogger.d("回调js返回的钱包地址:$data")
            activity.finish()
        }catch (e: JsonIOException) {
            DAuthLogger.e("JavaScriptMethods JsonIOException:$e")
        }
    }

}