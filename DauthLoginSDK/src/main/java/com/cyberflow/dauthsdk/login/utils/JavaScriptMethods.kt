package com.cyberflow.dauthsdk.login.utils

import android.webkit.JavascriptInterface
import androidx.appcompat.app.AppCompatActivity
import com.cyberflow.dauthsdk.login.model.DAuthUser
import com.cyberflow.dauthsdk.login.model.AuthorizeToken2Param
import com.cyberflow.dauthsdk.login.model.WalletRes
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.google.gson.Gson
import com.google.gson.JsonIOException


private const val TYPE_OF_WALLET_AUTH = "20"
private const val USER_TYPE = "user_type"
private const val USER_DATA = "user_data"
class JavaScriptMethods constructor( private val activity: AppCompatActivity){

    @JavascriptInterface
    fun postAddress(data:String?) {
        try {
            DAuthLogger.d("回调js返回的钱包地址:$data")
            val gson = Gson()
            val walletRes = gson.fromJson(data, WalletRes::class.java)
            val openId = walletRes.address
            val userData = DAuthUser()
            userData.openid = openId
            val userDataStr = gson.toJson(userData)
            val map = HashMap<String,String>()
            map[USER_TYPE] = TYPE_OF_WALLET_AUTH
            map[USER_DATA] = userDataStr
            val sign = SignUtils.sign(map)
            val body = AuthorizeToken2Param(
                user_type = TYPE_OF_WALLET_AUTH,
                sign = sign,
                user_data = userDataStr
            )
            RequestApi().authorizeExchangedToken(body)
            activity.finish()
        }catch (e: JsonIOException) {
            DAuthLogger.e("JavaScriptMethods JsonIOException:$e")
        }
    }

}