package com.cyberflow.dauthsdk.login.infrastructure

import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.login.utils.SignUtils
import com.google.gson.Gson
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.*
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
        var defaultHeaders: Map<String, String> by ApplicationDelegates.setOnce(
            mapOf(
                ContentType to FormDataMediaType,
                Accept to JsonMediaType
            )
        )

        @JvmStatic
        val jsonHeaders: Map<String, String> =
            mapOf(ContentType to JsonMediaType, Accept to JsonMediaType)
    }

    protected inline fun <reified T> requestBody(
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
            return Serializer.moshi.adapter(T::class.java).toJson(content)
                .toRequestBody(mediaType.toMediaTypeOrNull())
        }
        return MultipartBody.Builder().setType(MultipartBody.FORM).build()
    }

    protected inline fun <reified T: Any?> responseBody(response: Response, accept: String): T? {
        if(response.body == null) return null
        val body = response.body
        val rb = body?.string()
        DAuthLogger.d("Response body: $rb")
        val gson = Gson()
        return gson.fromJson(rb,T::class.java)
    }


//    protected inline fun <reified T: Any?> request(requestConfig: RequestConfig, body : Any? = null, baseHttpCallback: BaseHttpCallback<T>) {
//        val httpUrl = baseUrl.toHttpUrlOrNull() ?: throw IllegalStateException("baseUrl is invalid.")
//        var urlBuilder = httpUrl.newBuilder()
//            .addPathSegments(requestConfig.path.trimStart('/'))
//
//        requestConfig.query.forEach { query ->
//            query.value.forEach { queryValue ->
//                urlBuilder = urlBuilder.addQueryParameter(query.key, queryValue)
//            }
//        }
//
//        val url = urlBuilder.build()
//        val headers = defaultHeaders + requestConfig.headers
//
//        if ((headers[ContentType] ?: "") == "") {
//            throw IllegalStateException("Missing Content-Type header. This is required.")
//        }
//
//        if ((headers[Accept] ?: "") == "") {
//            throw IllegalStateException("Missing Accept header. This is required.")
//        }
//
//        // TODO: support multiple contentType,accept options here.
//        val contentType = (headers[ContentType] as String).substringBefore(";")
//            .lowercase(Locale.ROOT)
//        val accept = (headers[Accept] as String).substringBefore(";")
//            .lowercase(Locale.ROOT)
//
//        var request: Request.Builder = when (requestConfig.method) {
//            RequestMethod.DELETE -> Request.Builder().url(url).delete()
//            RequestMethod.GET -> Request.Builder().url(url)
//            RequestMethod.HEAD -> Request.Builder().url(url).head()
//            RequestMethod.PATCH -> Request.Builder().url(url).patch(requestBody(body, contentType))
//            RequestMethod.PUT -> Request.Builder().url(url).put(requestBody(body, contentType))
//            RequestMethod.POST -> Request.Builder().url(url).post(requestBody(body, contentType))
//            RequestMethod.OPTIONS -> Request.Builder().url(url).method("OPTIONS", null)
//        }
//
//        headers.forEach { header -> request = request.addHeader(header.key, header.value) }
//
//        val realRequest = request.build()
//        client.newCall(realRequest).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                baseHttpCallback.onFailed(e.toString())
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                val result = StringBuffer()
//                val ins = response.body?.byteStream()
//                val br = BufferedReader(InputStreamReader(ins, "UTF-8"))
//                var temp: String? = null
//                if (br.readLine().also { temp = it } != null) {
//                    result.append(temp)
//                }
//                baseHttpCallback.onResult(result.toString())
//
//            }
//
//        })
//
//    }

    protected inline fun <reified T : Any?> request(requestConfig: RequestConfig, body: Any?) :String?{
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

        if ((headers[ContentType] ?: "") == "") {
            throw IllegalStateException("Missing Content-Type header. This is required.")
        }

        if ((headers[Accept] ?: "") == "") {
            throw IllegalStateException("Missing Accept header. This is required.")
        }

        // TODO: support multiple contentType,accept options here.
        val contentType = (headers[ContentType] as String).substringBefore(";")
            .lowercase(Locale.ROOT)
        val accept = (headers[Accept] as String).substringBefore(";")
            .lowercase(Locale.ROOT)

        val request: Request.Builder = when (requestConfig.method) {
            RequestMethod.DELETE -> Request.Builder().url(url).delete()
            RequestMethod.GET -> Request.Builder().url(url)
            RequestMethod.HEAD -> Request.Builder().url(url).head()
            RequestMethod.PATCH -> Request.Builder().url(url).patch(requestBody(body, contentType))
            RequestMethod.PUT -> Request.Builder().url(url).put(requestBody(body, contentType))
            RequestMethod.POST -> Request.Builder().url(url).post(requestBody(body, contentType))
            RequestMethod.OPTIONS -> Request.Builder().url(url).method("OPTIONS", null)
        }

        headers.forEach { header -> request.addHeader(header.key, header.value) }

        val realRequest = request.build()
        val response: Response?
        try {
            response = client.newCall(realRequest).execute()

            if (response.isSuccessful) {
                return response.body?.string()
            } else {
                val errorMessage = response.message
            }
        } catch (e: Exception) {
            DAuthLogger.e("网络异常, 请检查网络连接")
        }

        return null
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
}