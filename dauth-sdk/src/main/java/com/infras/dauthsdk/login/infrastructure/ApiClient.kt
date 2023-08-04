package com.infras.dauthsdk.login.infrastructure

import com.infras.dauthsdk.api.DAuthSDK
import com.infras.dauthsdk.login.impl.TokenManager
import com.infras.dauthsdk.login.model.IAccessTokenRequest
import com.infras.dauthsdk.login.model.IAuthorizationRequest
import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.login.utils.SignUtils
import com.infras.dauthsdk.mpc.util.MoshiUtil
import com.infras.dauthsdk.wallet.ext.runCatchingWithLogSuspend
import com.infras.dauthsdk.wallet.impl.ConfigurationManager
import com.infras.dauthsdk.wallet.impl.HttpClient
import com.infras.dauthsdk.wallet.impl.manager.Managers
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.*
import java.util.*
import java.util.regex.Pattern


//账号类型:10邮箱注册,20钱包注册,30谷歌,40facebook,50苹果,60手机号,70自定义帐号,80一键注册,100Discord,110Twitter
internal open class ApiClient {
    companion object {
        protected const val ContentType = "Content-Type"
        protected const val Accept = "Accept"
        protected const val JsonMediaType = "application/json"
        protected const val FormDataMediaType = "multipart/form-data"

        private val clientId get() = DAuthSDK.impl.config.clientId.orEmpty()
        private val clientSecret get() = DAuthSDK.impl.config.clientSecret.orEmpty()

        @JvmStatic
        var defaultHeaders: Map<String, String> by ApplicationDelegates.setOnce(
            mapOf(
                ContentType to FormDataMediaType,
                Accept to JsonMediaType,
                "client_id" to clientId,
                "client_secret" to clientSecret,
            )
        )
    }

    private val loginPrefs get() = Managers.loginPrefs
    protected val baseUrl get() = "https://${ConfigurationManager.stage().baseUrlHost}"
    protected val didToken get() = loginPrefs.getDidToken()
    protected val authId get() = loginPrefs.getAuthId()
    protected val accessToken get() = loginPrefs.getAccessToken()

    protected inline fun <reified T: Any> requestBody(
        content: T,
        mediaType: String = FormDataMediaType
    ): RequestBody {

        if (content is File) {
            return content
                .asRequestBody(mediaType.toMediaTypeOrNull())
        } else if (mediaType == FormDataMediaType) {
            val requestBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)

            // content's type *must* be Map<String, Any>
            @Suppress("UNCHECKED_CAST")
            val map = SignUtils.objToMap(content)

            val needAccessToken = hasAccessToken(content)
            if (needAccessToken) {
                if (authId.isNotEmpty()) {
                    map["authid"] = authId
                }
                if (accessToken.isNotEmpty()) {
                    map["access_token"] = accessToken
                }
            }

            map["sign"] = SignUtils.sign(map)
            map.forEach { (key, value) ->
                if (value::class == File::class) {
                    val file = value as File
                    requestBodyBuilder.addFormDataPart(
                        key, file.name,
                        file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                    )
                } else {
                    val stringValue = value as String
                    requestBodyBuilder.addFormDataPart(key, stringValue)
                }
            }

            return requestBodyBuilder.build()
        } else if (mediaType == JsonMediaType) {
            return MoshiUtil.toJson(content)
                .toRequestBody(mediaType.toMediaTypeOrNull())
        }
        return MultipartBody.Builder().setType(MultipartBody.FORM).build()
    }

    fun isJsonMime(mime: String?): Boolean {
        val jsonMime = "(?i)^(application/json|[^;/ \t]+/[^;/ \t]+[+]json)[ \t]*(;.*)?$"
        return mime != null && (mime.matches(jsonMime.toRegex()) || mime == "*/*")
    }

    protected inline fun <reified T : Any> responseBody(response: Response, accept: String): T? {
        if (response.body == null) return null
        val body = response.body
        val rb = body?.string()
        if (T::class.java == java.io.File::class.java) {
            return downloadFileFromResponse(response) as T
        } else if (T::class == Unit::class) {
            return Unit as T
        }

        var contentType = response.headers["Content-Type"]

        if(contentType == null) {
            contentType = JsonMediaType
        }
        val r = try {
            if (isJsonMime(contentType)) {
                //DAuthLogger.d("responseBody json=${rb.orEmpty()}")
                MoshiUtil.fromJson(rb.orEmpty())
            } else if (contentType.equals(String.Companion::class.java)) {
                response.body.toString() as T
            } else {
                DAuthLogger.e("Fill in more types!")
                null
            }
        } catch (e: Exception) {
            DAuthLogger.e("responseBody Serializer exception: ${e.stackTraceToString()}")
            null
        }
        DAuthLogger.d("responseBody result=$r")
        return r
    }

    internal suspend inline fun <reified T : Any> request(
        requestConfig: RequestConfig,
        body: Any
    ): T? = withContext(Dispatchers.IO) {
        runCatchingWithLogSuspend {
            if (hasAccessToken(body)) {
                TokenManager.instance.authenticatedRequest {
                    requestInner(requestConfig, body)
                }
            } else {
                requestInner(requestConfig, body)
            }
        }
    }

    protected inline fun <reified T : Any> requestInner(requestConfig: RequestConfig, body: Any): T? {
        var response: Response? = null
        var result: T? = null
        val url = requestConfig.reqUrl.getUrl(requestConfig, baseUrl)

        val publicHeaders = mutableMapOf<String, String>() + defaultHeaders + requestConfig.headers
        val headers = if (body is IAuthorizationRequest) {
            publicHeaders + mapOf("Authorization" to didToken)
        } else {
            publicHeaders
        }

        if ((headers[ContentType] ?: "") == "") {
            throw IllegalStateException("Missing Content-Type header. This is required.")
        }
        if ((headers[Accept] ?: "") == "") {
            throw IllegalStateException("Missing Accept header. This is required.")
        }
        val contentType = (headers[ContentType] as String).substringBefore(";")
            .lowercase(Locale.ROOT)
        val accept = (headers[Accept] as String).substringBefore(";")
            .lowercase(Locale.ROOT)
        val requestBuilder: Request.Builder = when (requestConfig.method) {
            RequestMethod.DELETE -> Request.Builder().url(url).delete()
            RequestMethod.GET -> Request.Builder().url(url)
            RequestMethod.HEAD -> Request.Builder().url(url).head()
            RequestMethod.PATCH -> Request.Builder().url(url)
                .patch(requestBody(body, contentType))
            RequestMethod.PUT -> Request.Builder().url(url).put(requestBody(body, contentType))
            RequestMethod.POST -> Request.Builder().url(url)
                .post(requestBody(body, contentType))
            RequestMethod.OPTIONS -> Request.Builder().url(url).method("OPTIONS", null)
        }
        headers.forEach { header -> requestBuilder.addHeader(header.key, header.value) }
        try {
            val realRequest = requestBuilder.build()
            response = HttpClient.client.newCall(realRequest).execute()
            if (response.isSuccessful) {
                result = responseBody(response, accept)
            }
        } catch (e: Exception) {
            DAuthLogger.e("request exception: ${e.stackTraceToString()}")
        } finally {
            response?.close()
        }

        return result
    }


    @Throws(IOException::class)
    fun downloadFileFromResponse(response: Response): File {
        val file = prepareDownloadFile(response)

        response.body?.byteStream().use{ input ->
            File(file.path).outputStream().use { input?.copyTo(it) }
        }

        return file
    }

    @Throws(IOException::class)
    fun prepareDownloadFile(response: Response): File {
        var filename: String? = null
        var contentDisposition = response.headers.get("Content-Disposition")

        if(contentDisposition != null && contentDisposition != ""){
            val pattern = Pattern.compile("filename=['\"]?([^'\"\\s]+)['\"]?")
            val matcher = pattern.matcher(contentDisposition)

            if (matcher.find())
                filename = matcher.group(1)
        }
        var prefix: String
        var suffix: String? = null

        if (filename == null) {
            prefix = "download-"
            suffix = ""
        } else {
            val pos = filename.lastIndexOf('.')

            if (pos == -1) {
            prefix = "$filename-";
            } else {
                prefix = filename.substring(0, pos) + "-"
                suffix = filename.substring(pos)
            }
            // File.createTempFile requires the prefix to be at least three characters long
        if (prefix.length < 3)
            prefix = "download-"
        }

        return File.createTempFile(prefix, suffix);
    }

    protected fun hasAccessToken(obj: Any?): Boolean {
        obj ?: return false
        if (obj is IAccessTokenRequest) {
            return true
        }
        val fields = obj.javaClass.declaredFields
        for (field in fields) {
            if (field.name == "access_token") {
                return true
            }
        }
        return false
    }
}

