package com.infras.dauth.entity

data class KycOpenAccountMethod(
    val idCard: Boolean,
    val passport: Boolean,
    val driverSLicence: Boolean,
)