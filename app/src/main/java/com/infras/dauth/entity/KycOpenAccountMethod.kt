package com.infras.dauth.entity

import com.infras.dauthsdk.login.model.AccountDocumentationRequestRes

data class KycOpenAccountMethod(
    val idTypeList: List<AccountDocumentationRequestRes.IdTypeInfo>
)