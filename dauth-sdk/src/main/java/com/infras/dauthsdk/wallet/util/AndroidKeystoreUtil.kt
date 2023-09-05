package com.infras.dauthsdk.wallet.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.infras.dauthsdk.login.utils.Base64Utils
import com.infras.dauthsdk.login.utils.DAuthLogger
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object AndroidKeystoreUtil {
    private const val TAG = "AndroidKeystoreUtil"
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val ALIAS = "key_store_manager"
    private const val AUTHENTICATION_TAG_LENGTH = 128
    private var iv = byteArrayOf(5, 63, 17, 56, -119, -116, -125, -41, -77, -92, 127, -105)

    private fun spec() = GCMParameterSpec(AUTHENTICATION_TAG_LENGTH, iv)

    @Throws(Exception::class)
    private fun genKey() {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            ALIAS,
            KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(false)
            .build()
        val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        kg.init(keyGenParameterSpec)
        val secretKey = kg.generateKey()
        DAuthLogger.d("key generated: ${secretKey.hashCode()}", TAG)
    }

    @Throws(Exception::class)
    private fun getKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        val entry = keyStore.getEntry(ALIAS, null)
            ?: genKey().let { keyStore.getEntry(ALIAS, null) }
        val secretKeyEntry = entry as KeyStore.SecretKeyEntry
        return secretKeyEntry.secretKey
    }

    @Throws(Exception::class)
    fun encode(src: String): String {
        val secretKey = getKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec())
        val result = cipher.doFinal(src.toByteArray(StandardCharsets.UTF_8))
        return Base64Utils.encode(result)
    }

    @Throws(Exception::class)
    fun decode(src: String): String {
        val secretKey = getKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec())
        val result = cipher.doFinal(Base64Utils.decode(src))
        return String(result, StandardCharsets.UTF_8)
    }
}