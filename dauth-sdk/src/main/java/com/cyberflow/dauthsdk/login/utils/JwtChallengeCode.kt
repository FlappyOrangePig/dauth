package com.cyberflow.dauthsdk.login.utils
import android.os.Build
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
class JwtChallengeCode {


    fun generateCodeVerifier(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        } else {
            Base64Utils.encode(bytes)
        }
    }

    fun generateCodeChallenge(codeVerifier: String): String {
        val bytes = codeVerifier.toByteArray(StandardCharsets.US_ASCII)
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val digest = messageDigest.digest(bytes)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
        } else {
            Base64Utils.encode(digest)
        }
    }


}