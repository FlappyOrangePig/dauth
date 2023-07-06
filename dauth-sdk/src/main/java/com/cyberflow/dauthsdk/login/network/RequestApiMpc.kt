package com.cyberflow.dauthsdk.login.network

import com.cyberflow.dauthsdk.login.infrastructure.ApiClient
import com.cyberflow.dauthsdk.login.infrastructure.ReqUrl
import com.cyberflow.dauthsdk.login.infrastructure.RequestConfig
import com.cyberflow.dauthsdk.login.model.GetParticipantsParam
import com.cyberflow.dauthsdk.login.model.GetParticipantsRes
import com.cyberflow.dauthsdk.login.model.GetSecretKeyParam
import com.cyberflow.dauthsdk.login.model.GetSecretKeyRes
import com.cyberflow.dauthsdk.login.model.SetSecretKeyParam
import com.cyberflow.dauthsdk.login.model.SetSecretKeyRes
import com.cyberflow.dauthsdk.login.utils.LoginPrefs

class RequestApiMpc(
    private val loginPrefs: LoginPrefs
) : ApiClient() {

    private val accessToken get() = loginPrefs.getAccessToken()
    private val authId get() = loginPrefs.getAuthId()

    suspend fun getParticipants(): GetParticipantsRes? {
        val localVariableConfig = RequestConfig(ReqUrl.PathUrl("/wallet/v1/participants/get"))
        val param = GetParticipantsParam(
            access_token = accessToken,
            authid = authId
        )
        return request<GetParticipantsRes>(localVariableConfig, param, true)
    }

    suspend fun setKey(url: String, key: String, mergeResult: String?): SetSecretKeyRes? {
        val localVariableConfig = RequestConfig(reqUrl = ReqUrl.WholePathUrl(url))
        val param = SetSecretKeyParam(
            access_token = accessToken,
            authid = authId,
            keyshare = key,
            keyresult = mergeResult
        )
        return request<SetSecretKeyRes>(localVariableConfig, param, true)
    }

    suspend fun getKey(url: String, type: Int): GetSecretKeyRes? {
        val localVariableConfig = RequestConfig(reqUrl = ReqUrl.WholePathUrl(url))
        val param = GetSecretKeyParam(
            access_token = accessToken,
            authid = authId,
            type
        )
        return request<GetSecretKeyRes>(localVariableConfig, param, true)
    }
}