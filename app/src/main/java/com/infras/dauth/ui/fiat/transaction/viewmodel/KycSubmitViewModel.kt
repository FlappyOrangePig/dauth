package com.infras.dauth.ui.fiat.transaction.viewmodel

import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.entity.KycDocumentInfo
import com.infras.dauth.entity.KycProfileInfo

class KycSubmitViewModel : BaseViewModel() {

    @Deprecated("can be submit alone")
    var profile: KycProfileInfo? = null

    var document: KycDocumentInfo? = null
}