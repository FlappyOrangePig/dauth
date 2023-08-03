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

object MpcServiceConst {
    const val NotAllowedError = 2000000
    const val RequestParamsError = 2000001
    const val DatabaseError = 2000002
    const val RedisError = 2000003
    const val RequestSignError = 2000004
    const val SessionExpiredError = 2000005
    const val ServiceInternalError = 2000006

    const val MpcSecretNotFoundError = 2000007
    const val MpcSecretInvalidError = 2000008
    const val MpcSecretWalletNotFoundError = 2000009
    const val MpcSecretSetError = 2000010
    const val MpcSecretAlreadyBoundError = 2000011
}

class RequestApiMpc internal constructor(): ApiClient() {

    suspend fun getParticipants(): GetParticipantsRes? {
        val localVariableConfig = RequestConfig(ReqUrl.PathUrl("/wallet/v1/participants/get"))
        return request<GetParticipantsRes>(localVariableConfig, GetParticipantsParam())
    }

    suspend fun setKey(url: String, key: String, mergeResult: String?): SetSecretKeyRes? {
        val localVariableConfig = RequestConfig(reqUrl = ReqUrl.WholePathUrl(url))
        val param = SetSecretKeyParam(
            keyshare = key,
            keyresult = mergeResult
        )
        return request<SetSecretKeyRes>(localVariableConfig, param)
    }

    suspend fun getKey(url: String, type: Int): GetSecretKeyRes? {
        val localVariableConfig = RequestConfig(reqUrl = ReqUrl.WholePathUrl(url))
        val param = GetSecretKeyParam(
            type
        )
        return request<GetSecretKeyRes>(localVariableConfig, param)
    }
}