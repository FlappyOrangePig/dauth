package com.infras.dauth.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class KycProfileInfo : Parcelable {
    @Parcelize
    class Email(
        val email: String,
        val verifyCode: String,
    ) : KycProfileInfo()

    @Parcelize
    class Phone(
        val areaCode: String,
        val phone: String,
        val verifyCode: String,
    ) : KycProfileInfo()
}

class KycDocumentInfo(
    val region: String,
    val kycName: KycName,
    val documentType: DocumentType,
)

sealed class KycName {
    class FullName(val name: String) : KycName()
    class PartsName(
        val first: String,
        val middle: String,
        val last: String
    ) : KycName()
}

sealed class DocumentType {
    class IDCard(val number: String) : DocumentType()
    class Passport(val number: String) : DocumentType()
    class DriverSLicence(val number: String) : DocumentType()
}