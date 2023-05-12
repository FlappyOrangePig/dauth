package com.cyberflow.dauthsdk.network

import com.cyberflow.dauthsdk.CyberFlowApplication
import okhttp3.FormBody

class YXHttpManager private constructor() {
    companion object {
        private const val IS_TEST = true
        private const val REQUEST_TEST_URL = ""
        private const val REQUEST_PRODUCT_URL = ""
        val instance by lazy { YXHttpManager() }
    }

    private val context get() = CyberFlowApplication.instance

    private fun getUrl(): String {
        return if (IS_TEST) REQUEST_TEST_URL else REQUEST_PRODUCT_URL
    }

    fun <T> getService(service: Class<T>): T {
        return RetrofitHelper.getService(getUrl(), service)
    }

    fun getFormBody(map: HashMap<String, String>): FormBody {

        return map2FormBody(map)
    }

    private fun map2FormBody(map: Map<String, String>): FormBody {
        val formBody = FormBody.Builder()
        map.forEach {
            formBody.add(it.key, it.value)
        }
        return formBody.build()
    }
}