package com.cyberflow.dauthsdk.network

import com.cyberflow.dauthsdk.utils.DAuthLogger
import com.cyberflow.dauthsdk.utils.StringPreference
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitHelper {
    private const val TAG = "RetrofitHelper"
    private const val CONTENT_PRE = "OkHttp: "
    private const val CONNECT_TIMEOUT = 10L
    private const val READ_TIMEOUT = 10L
    private const val SAVE_COOKIE = false
    private const val INTERCEPTOR_ENABLE = true

    val okHttpClient by lazy { createOkHttpClient() }
    private val gsonConverterFactory by lazy { GsonConverterFactory.create() }

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient().newBuilder().apply {
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            if (SAVE_COOKIE) {
                // get response cookie
                addInterceptor(GetResponseCookieInterceptor)
                // set request cookie
                addInterceptor(SetRequestCookieInterceptor)
            }
            // add log print
            if (INTERCEPTOR_ENABLE) {
                // loggingInterceptor
                addInterceptor(HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                    DAuthLogger.i( CONTENT_PRE + it)
                }).apply {
                    // log level
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
        }.build()
    }

    /**
     * create Retrofit
     */
    private fun create(url: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }

    /**
     * get ServiceApi
     */
    fun <T> getService(url: String, service: Class<T>): T = create(url).create(service)



    /**
     * save cookie string
     */
    private fun encodeCookie(cookies: List<String>): String {
        val sb = StringBuilder()
        val set = HashSet<String>()
        cookies
            .map { cookie ->
                cookie.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            }
            .forEach {
                it.filterNot { set.contains(it) }.forEach { set.add(it) }
            }

        val ite = set.iterator()
        while (ite.hasNext()) {
            val cookie = ite.next()
            sb.append(cookie).append(";")
        }

        val last = sb.lastIndexOf(";")
        if (sb.length - 1 == last) {
            sb.deleteCharAt(last)
        }

        return sb.toString()
    }

    private object GetResponseCookieInterceptor : Interceptor {
        // TODO: 修改接口名称
        private const val SAVE_USER_LOGIN_KEY = "user/login"
        private const val SAVE_USER_REGISTER_KEY = "user/register"
        private const val SET_COOKIE_KEY = "set-cookie"
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val response = chain.proceed(request)
            val requestUrl = request.url.toString()
            val domain = request.url.host
            // set-cookie maybe has multi, login to save cookie
            if ((requestUrl.contains(SAVE_USER_LOGIN_KEY) || requestUrl.contains(
                    SAVE_USER_REGISTER_KEY
                ))
                && !response.headers(SET_COOKIE_KEY).isEmpty()
            ) {
                val cookies = response.headers(SET_COOKIE_KEY)
                val cookie = encodeCookie(cookies)
                saveCookie(requestUrl, domain, cookie)
            }
            return response
        }
    }

    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    private fun saveCookie(url: String?, domain: String?, cookies: String) {
        url ?: return
        var spUrl: String by StringPreference(url, cookies)
        @Suppress("UNUSED_VALUE")
        spUrl = cookies
        domain ?: return
        var spDomain: String by StringPreference(domain, cookies)
        @Suppress("UNUSED_VALUE")
        spDomain = cookies
    }

    private object SetRequestCookieInterceptor : Interceptor {
        private const val COOKIE_NAME = "Cookie"
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val builder = request.newBuilder()
            val domain = request.url.host
            // get domain cookie
            if (domain.isNotEmpty()) {
                val spDomain: String by StringPreference(domain, "")
                val cookie: String = if (spDomain.isNotEmpty()) spDomain else ""
                if (cookie.isNotEmpty()) {
                    builder.addHeader(COOKIE_NAME, cookie)
                }
            }
            return chain.proceed(builder.build())
        }
    }


}

