package com.infras.dauthsdk.login.infrastructure

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Defines a config object for a given request.
 * NOTE: This object doesn't include 'body' because it
 *       allows for caching of the constructed object
 *       for many request definitions.
 * NOTE: Headers is a Map<String,String> because rfc2616 defines
 *       multi-valued headers as csv-only.
 */
data class RequestConfig(
    val reqUrl: ReqUrl,
    val method: RequestMethod = RequestMethod.POST,
    val headers: Map<String, String> = mapOf(),
    val query: Map<String, List<String>> = mapOf(),
)

sealed class ReqUrl {

    abstract fun getUrl(requestConfig: RequestConfig, baseUrl: String): HttpUrl

    class PathUrl(private val path: String) : ReqUrl() {

        override fun getUrl(requestConfig: RequestConfig, baseUrl: String): HttpUrl {
            val httpUrl =
                baseUrl.toHttpUrlOrNull() ?: throw IllegalStateException("baseUrl is invalid.")
            val urlBuilder = httpUrl.newBuilder()
                .addPathSegments(path.trimStart('/'))
            requestConfig.query.forEach { query ->
                query.value.forEach { queryValue ->
                    urlBuilder.addQueryParameter(query.key, queryValue)
                }
            }
            return urlBuilder.build()
        }
    }

    class WholePathUrl(private val wholePathUrl: String) : ReqUrl() {
        override fun getUrl(requestConfig: RequestConfig, baseUrl: String): HttpUrl {
            return wholePathUrl.toHttpUrl()
        }
    }
}