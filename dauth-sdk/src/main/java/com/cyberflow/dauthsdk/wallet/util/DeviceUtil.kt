package com.cyberflow.dauthsdk.wallet.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.text.TextUtils
import java.nio.charset.StandardCharsets
import java.util.UUID

object DeviceUtil {

    fun getDeviceId(context: Context): String {
        var result = ""
        try {
            val uuid: UUID = DeviceUuidHolder.getDeviceUuid(context)
            result = uuid.toString()
        } catch (e: Exception) {
            // ignore
        }
        return result
    }

    @SuppressLint("HardwareIds")
    private fun getAndroidId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun isDeviceIdValid(deviceId: String): Boolean {
        //android-8 的部分厂商所有设备使用的同一个id
        if (TextUtils.isEmpty(deviceId) || "9774d56d682e549c" == deviceId) {
            return false
        }
        // 全0设备号
        return !deviceId.contains("0000")
    }

    private fun getFromDevice(context: Context): String? {
        val str = getAndroidId(context)
        return if (isDeviceIdValid(str)) {
            str
        } else null
    }

    private object DeviceUuidHolder {
        @Volatile
        private var deviceUuid: UUID? = null

        fun getDeviceUuid(context: Context) = deviceUuid ?: synchronized(this) {
            deviceUuid ?: createUuid(context).also { deviceUuid = it }
        }

        private fun createUuid(context: Context): UUID {
            val id = DeviceIdPreference.getDeviceId(context)
            val r = if (id.isNotEmpty()) {
                UUID.fromString(id)
            } else {
                val str = getFromDevice(context)
                if (TextUtils.isEmpty(str)) UUID.randomUUID() else UUID.nameUUIDFromBytes(
                    str!!.toByteArray(StandardCharsets.UTF_8)
                ).also {
                    // 保存到SP
                    DeviceIdPreference.setDeviceId(context, deviceUuid.toString())
                }
            }
            return r
        }
    }
}

private object DeviceIdPreference {
    private const val PREFS_DEVICE_ID = "device_id"
    private const val FILE_NAME = "dauth_device_id.xml"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    }

    fun getDeviceId(context: Context): String {
        return prefs(context).getString(PREFS_DEVICE_ID, "").orEmpty()
    }

    @SuppressLint("ApplySharedPref")
    fun setDeviceId(context: Context, deviceId: String) {
        val prefs = prefs(context)
        val editor = prefs.edit()
        editor.putString(PREFS_DEVICE_ID, deviceId)
        editor.commit()
    }
}