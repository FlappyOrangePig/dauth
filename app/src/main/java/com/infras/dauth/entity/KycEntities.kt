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

@Parcelize
class KycDocumentInfo(
    val countryCode: String,
    val kycName: KycName,
    val documentType: DocumentType,
) : Parcelable

@Parcelize
sealed class KycName : Parcelable {
    @Parcelize
    class FullName(val name: String) : KycName()

    @Parcelize
    class PartsName(
        val first: String,
        val middle: String,
        val last: String
    ) : KycName()
}

@Parcelize
sealed class DocumentType : Parcelable {
    abstract val picCount: Int

    @Parcelize
    class IDCard(override val picCount: Int, val number: String) : DocumentType()

    @Parcelize
    class Passport(override val picCount: Int) : DocumentType()

    @Parcelize
    class DriverSLicence(override val picCount: Int) : DocumentType()
}