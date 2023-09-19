package com.infras.dauth.ui.fiat.transaction.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.entity.DocumentType
import com.infras.dauth.entity.KycDocumentInfo
import com.infras.dauth.entity.KycName
import com.infras.dauth.manager.AppManagers
import com.infras.dauth.repository.FiatTxRepository
import com.infras.dauth.ui.fiat.transaction.util.ImageBase64Util
import com.infras.dauthsdk.login.model.AccountDocumentationRequestRes
import com.infras.dauthsdk.login.model.AccountOpenParam
import kotlinx.coroutines.launch

class VerifyUploadIdCardViewModel : BaseViewModel() {

    private val repo = FiatTxRepository()

    val pathA = MutableLiveData<String?>()
    val pathB = MutableLiveData<String?>()
    val createSuccessEvent = MutableLiveData<Boolean>()

    private suspend fun accountOpen(param: AccountOpenParam) {
        val r = showLoading {
            repo.accountOpen(param)
        }
        toast(AppManagers.resourceManager.getResponseDigest(r))
        if (r != null && r.isSuccess()) {
            createSuccessEvent.value = true
        }
    }

    fun submit(document: KycDocumentInfo) = viewModelScope.launch {
        var full: String? = null
        var first: String? = null
        var middle: String? = null
        var last: String? = null
        when (document.kycName) {
            is KycName.FullName -> {
                full = document.kycName.name
            }

            is KycName.PartsName -> {
                first = document.kycName.first
                middle = document.kycName.middle
                last = document.kycName.last
            }
        }
        var idNum: String? = null
        val idType: String = when (document.documentType) {
            is DocumentType.DriverSLicence -> AccountDocumentationRequestRes.IdTypeInfo.DRIVERS
            is DocumentType.IDCard -> {
                idNum = document.documentType.number
                AccountDocumentationRequestRes.IdTypeInfo.ID_CARD
            }

            is DocumentType.Passport -> AccountDocumentationRequestRes.IdTypeInfo.PASSPORT
        }

        val imageA = pathA.value.orEmpty()
        if (imageA.isEmpty()) {
            toast("no image a")
            return@launch
        }
        val base64EncodedImageA = ImageBase64Util.getBase64EncodedImageFile(imageA)
        if (base64EncodedImageA == null) {
            toast("image a base64 error")
            return@launch
        }

        val base64EncodedImageB = if (document.documentType.picCount == 2) {
            val imageB = pathB.value.orEmpty()
            if (imageB.isEmpty()) {
                toast("no image b")
                return@launch
            }

            ImageBase64Util.getBase64EncodedImageFile(imageB).also {
                if (it == null) {
                    toast("image b base64 error")
                    return@launch
                }
            }
        } else {
            null
        }

        val param = AccountOpenParam(
            first_name = first,
            middle_name = middle,
            last_name = last,
            full_name = full,
            id_type = idType,
            id_back_img = base64EncodedImageB,
            id_front_img = base64EncodedImageA,
            id_num = idNum,
            issuing_country = document.countryCode
        )

        accountOpen(param)
    }
}