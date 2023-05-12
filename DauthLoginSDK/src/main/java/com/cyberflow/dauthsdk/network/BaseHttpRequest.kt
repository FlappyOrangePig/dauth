package com.cyberflow.dauthsdk.network

import android.util.Log
import com.cyberflow.dauthsdk.utils.DAuthLogger
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.lang.reflect.ParameterizedType

abstract class BaseHttpRequest<RESPONSE : BaseResponse> {

    companion object {
        private const val BASE_REQUEST_URL = ""
        private val http by lazy { YXHttpManager.instance }
        private val gson by lazy { Gson() }
    }

    private fun getUrl(): String {
        return ""
    }

    private fun requestInner(callback: YXHttpCallback<RESPONSE>) {
        val client = RetrofitHelper.okHttpClient
        val input = HashMap<String, String>()
        fillInputParams(input)
        val formBody = http.getFormBody(input)
        val request = Request.Builder().url(getUrl()).post(formBody).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                DAuthLogger.e("requestInner onFailure ${Log.getStackTraceString(e)}")
                callback.onResult(HttpResult.Failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                DAuthLogger.d("requestInner onResponse $response")
                val code = response.code
                val message = response.message
                val responseBean = if (response.isSuccessful) {
                    val responseBody = response.body
                    if (responseBody != null) {
                        try {
                            val rb = responseBody.string()
                            val data = JSONObject(rb)
                            val ret = data.getInt("iRet")
                            DAuthLogger.d("intercept biz code=$ret")
                            val clazz = getResponseClazz()
                            val bean = gson.fromJson(rb, /*getResponseClass()*/clazz)
                            if (bean == null) {
                                HttpResult.Exception(java.lang.RuntimeException("no data"))
                            } else {
                                HttpResult.Success(bean)
                            }

                        } catch (e: Exception) {
                            DAuthLogger.e(javaClass.simpleName + Log.getStackTraceString(e))
                            HttpResult.Exception(e)
                        }
                    } else {
                        HttpResult.Exception(NullPointerException("body is null"))
                    }
                } else {
                    HttpResult.HttpError(code, message)
                }
                callback.onResult(responseBean)
            }
        })
    }

    protected abstract fun fillInputParams(map: MutableMap<String, String>)

    fun <T : BaseResponse> BaseHttpRequest<T>.getResponseClazz(): Class<T> {
        when (val superClass = this.javaClass.genericSuperclass) {
            is ParameterizedType -> {
                when (val arg0 = superClass.actualTypeArguments[0]) {
                    is Class<*> -> {
                        return arg0 as Class<T>
                    }
                }
            }
        }
        throw java.lang.IllegalStateException("get response clazz exception")
    }
}