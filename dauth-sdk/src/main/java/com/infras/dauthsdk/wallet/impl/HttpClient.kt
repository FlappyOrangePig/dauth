package com.infras.dauthsdk.wallet.impl

import com.infras.dauthsdk.login.utils.DAuthLogger
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import okhttp3.internal.platform.Platform
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import okio.Buffer
import okio.GzipSource
import java.io.EOFException
import java.io.IOException
import java.lang.StringBuilder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.TreeSet
import java.util.concurrent.TimeUnit

object HttpClient {

    const val TAG = "HttpClient"

    val client by lazy {
        getOkhttpClient()
    }

    private val redactHeader
        get() = listOf<String>()

    private fun getOkhttpClient() = OkHttpClient().newBuilder().apply {
        connectTimeout(10, TimeUnit.SECONDS)
        readTimeout(10, TimeUnit.SECONDS)
        writeTimeout(10, TimeUnit.SECONDS)
        addInterceptor(getInterceptor())
    }.build()

    private fun getInterceptor(): Interceptor {
        return if (true) {
            HttpLoggingInterceptor2 { message ->
                DAuthLogger.d(message, TAG)
            }.apply {
                level = Level.BODY
                redactHeader.forEach {
                    redactHeader(it)
                }
            }
        } else {
            HttpLoggingInterceptor { message ->
                DAuthLogger.d(message, TAG)
            }.apply {
                level = Level.BODY
                redactHeader.forEach {
                    redactHeader(it)
                }
            }
        }
    }
}

class HttpLoggingInterceptor2 @JvmOverloads constructor(
    private val logger: Logger = Logger.DEFAULT
) : Interceptor {

    @Volatile private var headersToRedact = emptySet<String>()

    @set:JvmName("level")
    @Volatile var level = Level.NONE

    fun interface Logger {
        fun log(message: String)

        companion object {
            /** A [Logger] defaults output appropriate for the current platform. */
            @JvmField
            val DEFAULT: Logger = DefaultLogger()
            private class DefaultLogger : Logger {
                override fun log(message: String) {
                    Platform.get().log(message)
                }
            }
        }
    }

    fun redactHeader(name: String) {
        val newHeadersToRedact = TreeSet(String.CASE_INSENSITIVE_ORDER)
        newHeadersToRedact += headersToRedact
        newHeadersToRedact += name
        headersToRedact = newHeadersToRedact
    }

    /**
     * Sets the level and returns this.
     *
     * This was deprecated in OkHttp 4.0 in favor of the [level] val. In OkHttp 4.3 it is
     * un-deprecated because Java callers can't chain when assigning Kotlin vals. (The getter remains
     * deprecated).
     */
    fun setLevel(level: Level) = apply {
        this.level = level
    }

    @JvmName("-deprecated_level")
    @Deprecated(
        message = "moved to var",
        replaceWith = ReplaceWith(expression = "level"),
        level = DeprecationLevel.ERROR
    )
    fun getLevel(): Level = level

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val level = this.level

        val request = chain.request()
        if (level == Level.NONE) {
            return chain.proceed(request)
        }

        val logBody = level == Level.BODY
        val logHeaders = logBody || level == Level.HEADERS

        val requestBody = request.body

        val connection = chain.connection()
        var requestStartMessage =
            ("--> ${request.method} ${request.url}${if (connection != null) " " + connection.protocol() else ""}")
        if (!logHeaders && requestBody != null) {
            requestStartMessage += " (${requestBody.contentLength()}-byte body)"
        }
        logger.log(requestStartMessage)

        if (logHeaders) {
            if (!logBody || requestBody == null) {
                logger.log("--> END ${request.method}")
            } else if (bodyHasUnknownEncoding(request.headers)) {
                logger.log("--> END ${request.method} (encoded body omitted)")
            } else if (requestBody.isDuplex()) {
                logger.log("--> END ${request.method} (duplex request body omitted)")
            } else if (requestBody.isOneShot()) {
                logger.log("--> END ${request.method} (one-shot body omitted)")
            } else {
                /*if (requestBody is FormBody) {
                    val sb = StringBuilder()
                    sb.append("{")
                    for (i in 0 until requestBody.size) {
                        val k = requestBody.name(i)
                        val v = requestBody.value(i)
                        if (requestFieldBlackList.contains(k)){
                            continue
                        }
                        if (i > 0) {
                            sb.append(",")
                        }
                        sb.append("$k:$v")
                    }
                    sb.append("}")
                    logger.log(sb.toString())
                    //logger.log("--> END ${request.method} (${requestBody.contentLength()}-byte body)")
                } else {
                    val buffer = Buffer()
                    requestBody.writeTo(buffer)

                    val contentType = requestBody.contentType()
                    val charset: Charset =
                        contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8

                    if (buffer.isProbablyUtf8()) {
                        logger.log(buffer.readString(charset))
                        //logger.log("--> END ${request.method} (${requestBody.contentLength()}-byte body)")
                    } else {
                        //logger.log("--> END ${request.method} (binary ${requestBody.contentLength()}-byte body omitted)")
                    }
                }*/
            }
        }

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            logger.log("<-- HTTP FAILED: $e")
            throw e
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseBody = response.body!!
        val contentLength = responseBody.contentLength()
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
        logger.log(
            "<-- ${response.code}${if (response.message.isEmpty()) "" else ' ' + response.message} ${response.request.url} (${tookMs}ms${if (!logHeaders) ", $bodySize body" else ""})")

        if (logHeaders) {
            val headers = response.headers
            if (!logBody || !response.promisesBody()) {
                logger.log("<-- END HTTP")
            } else if (bodyHasUnknownEncoding(response.headers)) {
                logger.log("<-- END HTTP (encoded body omitted)")
            } else {
                val source = responseBody.source()
                source.request(Long.MAX_VALUE) // Buffer the entire body.
                var buffer = source.buffer

                var gzippedLength: Long? = null
                if ("gzip".equals(headers["Content-Encoding"], ignoreCase = true)) {
                    gzippedLength = buffer.size
                    GzipSource(buffer.clone()).use { gzippedResponseBody ->
                        buffer = Buffer()
                        buffer.writeAll(gzippedResponseBody)
                    }
                }

                val contentType = responseBody.contentType()
                val charset: Charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8

                if (!buffer.isProbablyUtf8()) {
                    //logger.log("")
                    //logger.log("<-- END HTTP (binary ${buffer.size}-byte body omitted)")
                    return response
                }

                if (contentLength != 0L) {
                    if (!request.url.toString().isInResponseBlackList()) {
                        logger.log(buffer.clone().readString(charset))
                    }
                }

                /*if (gzippedLength != null) {
                    logger.log("<-- END HTTP (${buffer.size}-byte, $gzippedLength-gzipped-byte body)")
                } else {
                    logger.log("<-- END HTTP (${buffer.size}-byte body)")
                }*/
            }
        }

        return response
    }

    private fun logHeader(headers: Headers, i: Int) {
        val value = if (headers.name(i) in headersToRedact) "██" else headers.value(i)
        logger.log(headers.name(i) + ": " + value)
    }

    private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"] ?: return false
        return !contentEncoding.equals("identity", ignoreCase = true) &&
                !contentEncoding.equals("gzip", ignoreCase = true)
    }

    private fun Buffer.isProbablyUtf8(): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = size.coerceAtMost(64)
            copyTo(prefix, 0, byteCount)
            for (i in 0 until 16) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            return true
        } catch (_: EOFException) {
            return false // Truncated UTF-8 sequence.
        }
    }

    private fun String.isInResponseBlackList() = when {
        this.endsWith("mpc/secret/get") -> true
        this.endsWith("mpc/genkey") -> true
        this.endsWith("secret/open/auth/get") -> true
        this.endsWith("account/v1/sociallogin/exchangedtoken") -> true
        else -> false
    }
}
