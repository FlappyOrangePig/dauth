package com.infras.dauth.ui.fiat.transaction.util

import com.infras.dauth.manager.AppManagers
import com.infras.dauth.manager.StorageDir
import com.infras.dauth.util.MoshiUtil
import com.infras.dauthsdk.login.model.DigitalCurrencyListRes
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode

object CurrencyCalcUtil {

    @Volatile
    private var data: DigitalCurrencyListRes.Data? = null

    fun setCurrencyList(data: DigitalCurrencyListRes.Data) {
        val dir = AppManagers.storageManager.getDir(StorageDir.JsonFile)
        if (!dir.exists()) {
            val mkResult = dir.mkdirs()
            if (!mkResult) {
                return
            }
        }
        val file = File(dir, StorageDir.JsonFile.FILE_NAME_CURRENCY_LIST)
        file.writeText(MoshiUtil.toJson(data))
        //this.data = data
    }

    private fun getCurrencyList(): DigitalCurrencyListRes.Data? {
        if (data == null) {
            synchronized(this) {
                if (data == null) {
                    kotlin.runCatching {
                        val dir = AppManagers.storageManager.getDir(StorageDir.JsonFile)
                        val file = File(dir, StorageDir.JsonFile.FILE_NAME_CURRENCY_LIST)
                        if (file.exists()) {
                            val json = file.readText()
                            data = MoshiUtil.fromJson<DigitalCurrencyListRes.Data>(json)
                        }
                    }
                }
            }
        }
        return data
    }

    fun getFiatInfo(fiatCode: String?): DigitalCurrencyListRes.FiatInfo? {
        val d = getCurrencyList()
        return kotlin.runCatching {
            d?.fiatList?.first {
                it.fiatCode == fiatCode
            }
        }.getOrNull()
    }

    fun getCryptoInfo(cryptoCode: String?): DigitalCurrencyListRes.CryptoInfo? {
        val d = getCurrencyList()
        return kotlin.runCatching {
            d?.cryptoList?.first {
                it.cryptoCode == cryptoCode
            }
        }.getOrNull()
    }

    fun String?.scale(
        precision: Int? = null,
        roundingMode: RoundingMode = RoundingMode.HALF_UP
    ): String {
        this ?: return ""
        return if (precision != null) {
            kotlin.runCatching {
                BigDecimal(this).setScale(precision, roundingMode).toString()
            }.getOrNull()
        } else {
            null
        } ?: this
    }
}