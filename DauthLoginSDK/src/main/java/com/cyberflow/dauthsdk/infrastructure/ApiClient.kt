package com.cyberflow.dauthsdk.infrastructure

import com.cyberflow.dauthsdk.model.BaseErrorResponse
import com.cyberflow.dauthsdk.utils.DAuthLogger
import com.cyberflow.dauthsdk.utils.SignUtils
import com.google.gson.Gson
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*
import java.util.regex.Pattern


//账号类型:10邮箱注册,20钱包注册,30谷歌,40facebook,50苹果,60手机号,70自定义帐号,80一键注册,100Discord,110Twitter
open class ApiClient(val baseUrl: String) {
    companion object {
        protected const val ContentType = "Content-Type"
        protected const val Accept = "Accept"
        protected const val JsonMediaType = "application/json"
        protected const val FormDataMediaType = "multipart/form-data"
        protected const val XmlMediaType = "application/xml"

        @JvmStatic
        val client by lazy {
            builder.build()
        }

        @JvmStatic
        val builder: OkHttpClient.Builder = OkHttpClient.Builder()

        @JvmStatic
        var defaultHeaders: Map<String, String> by ApplicationDelegates.setOnce(mapOf(ContentType to FormDataMediaType, Accept to JsonMediaType))

        @JvmStatic
        val jsonHeaders: Map<String, String> = mapOf(ContentType to JsonMediaType, Accept to JsonMediaType)
    }

    protected inline fun <reified T> requestBody(content: T, mediaType: String = FormDataMediaType): RequestBody {

        if(content is File) {
            return content
                .asRequestBody(mediaType.toMediaTypeOrNull())
        } else if(mediaType == FormDataMediaType) {
            val requestBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)

            // content's type *must* be Map<String, Any>
            @Suppress("UNCHECKED_CAST")
            val map = SignUtils.objToMap(content)
            map.forEach { (key, value) ->
                if(value::class == File::class) {
                    val file = value as File
                    requestBodyBuilder.addFormDataPart(key, file.name,
                        file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                    )
                } else {
                    val stringValue = value as String
                    requestBodyBuilder.addFormDataPart(key, stringValue)
                }
            }

            return requestBodyBuilder.build()
        }  else if(mediaType == JsonMediaType) {
            return Serializer.moshi.adapter(T::class.java).toJson(content)
                .toRequestBody(mediaType.toMediaTypeOrNull())
        }
        return MultipartBody.Builder().setType(MultipartBody.FORM).build()
    }

    protected inline fun <reified T: Any?> responseBody(response: Response, accept: String): T? {
        if(response.body == null) return null
        val body = response.body
        val rb = body?.string()
        val data = JSONObject(rb)
        val ret = data.getInt("iRet")
        val objStr = data.getString("data")
        val msg = data.getString("sMsg")
        DAuthLogger.e("接口返回：$msg")
        if (T::class.java == java.io.File::class.java) {
            return downloadFileFromResponse(response) as T
        } else if (T::class == Unit::class) {
            return Unit as T
        }
        
        var contentType = response.headers["Content-Type"]
        
        if(contentType == null) {
            contentType = JsonMediaType
        }
        if(ret == 0) {
            try {
                return if (isJsonMime(contentType)) {
                    val gson = Gson()
                    gson.fromJson(objStr, T::class.java)
                } else if (contentType.equals(String.Companion::class.java)) {
                    response.body.toString() as T
                } else {
                    DAuthLogger.e("Fill in more types!")
                    return null
                }
            } catch (e: java.lang.Exception) {
                DAuthLogger.e("responseBody Serializer exception: $e")
            }
        } else {
            DAuthLogger.e("http request response errorCode:$ret,errorMsg:$msg")
        }
        return null
    }
    
    fun isJsonMime(mime: String?): Boolean {
        val jsonMime = "(?i)^(application/json|[^;/ \t]+/[^;/ \t]+[+]json)[ \t]*(;.*)?$"
        return mime != null && (mime.matches(jsonMime.toRegex()) || mime == "*/*")
    }

    protected inline fun <reified T: Any?> request(requestConfig: RequestConfig, body : Any? = null): ApiInfrastructureResponse<T?> {
        val httpUrl = baseUrl.toHttpUrlOrNull() ?: throw IllegalStateException("baseUrl is invalid.")

        var urlBuilder = httpUrl.newBuilder()
                .addPathSegments(requestConfig.path.trimStart('/'))

        requestConfig.query.forEach { query ->
            query.value.forEach { queryValue ->
                urlBuilder = urlBuilder.addQueryParameter(query.key, queryValue)
            }
        }

        val url = urlBuilder.build()
        val headers = defaultHeaders + requestConfig.headers

        if((headers[ContentType] ?: "") == "") {
            throw  IllegalStateException("Missing Content-Type header. This is required.")
        }

        if((headers[Accept] ?: "") == "") {
            throw  IllegalStateException("Missing Accept header. This is required.")
        }

        // TODO: support multiple contentType,accept options here.
        val contentType = (headers[ContentType] as String).substringBefore(";")
            .lowercase(Locale.ROOT)
        val accept = (headers[Accept] as String).substringBefore(";")
            .lowercase(Locale.ROOT)

        var request : Request.Builder =  when (requestConfig.method) {
            RequestMethod.DELETE -> Request.Builder().url(url).delete()
            RequestMethod.GET -> Request.Builder().url(url)
            RequestMethod.HEAD -> Request.Builder().url(url).head()
            RequestMethod.PATCH -> Request.Builder().url(url).patch(requestBody(body, contentType))
            RequestMethod.PUT -> Request.Builder().url(url).put(requestBody(body, contentType))
            RequestMethod.POST -> Request.Builder().url(url).post(requestBody(body, contentType))
            RequestMethod.OPTIONS -> Request.Builder().url(url).method("OPTIONS", null)
        }

        headers.forEach { header -> request = request.addHeader(header.key, header.value) }

        val realRequest = request.build()
        val response = client.newCall(realRequest).execute()

        // TODO: handle specific mapping types. e.g. Map<int, Class<?>>
        when {
            response.isRedirect -> return Redirection(
                response.code,
                    response.headers.toMultimap()
            )
            response.isInformational -> return Informational(
                    response.message,
                    response.code,
                    response.headers.toMultimap()
            )
            response.isSuccessful -> return Success(
                    responseBody(response, accept),
                    response.code,
                    response.headers.toMultimap()
            )
            response.isClientError -> return ClientError(
                    response.body?.string(),
                    response.code,
                    response.headers.toMultimap()
            )
            else -> return ServerError(
                    null,
                    response.body?.string(),
                    response.code,
                    response.headers.toMultimap()
            )
        }
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
            prefix = filename + "-";
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
}