package com.cyberflow.dauthsdk.wallet.util

import com.cyberflow.dauthsdk.login.DAuthSDK
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import java.io.File

/**
 * 凭据工具类
 */
object CredentialsUtil {

    private val context get() = (DAuthSDK.instance as DAuthSDK).context
    private const val PASSWORD = "123456"
    private const val MNEMONIC =
        "nominee video milk cake style decide blind sponsor rabbit mule dutch vanish"

    private fun getKeyDirectory(): File {
        val fileDir = context.applicationContext.filesDir
        return File(fileDir, "keys")
    }

    private fun safeGetKeyDirectory(): File {
        return getKeyDirectory().also {
            it.mkdirs()
        }
    }

    private fun generateWalletFile(): String {
        return createWalletFileFromMnemonic(MNEMONIC, PASSWORD)
    }

    private fun getWalletFile(): File {
        val pref = WalletPrefs(context)
        val walletFileName = pref.getWalletFileName()
        val finalFileName = walletFileName.ifEmpty {
            generateWalletFile().also {
                pref.setWalletFileName(it)
            }
        }
        return File(safeGetKeyDirectory(), finalFileName)
    }

    fun loadCredentials(): Credentials {
        val f = getWalletFile()
        return WalletUtils.loadCredentials(PASSWORD, f)
    }

    private fun createWalletFileFromMnemonic(mnemonic: String, password: String): String {
        val wallet = WalletUtils.generateBip39WalletFromMnemonic(
            password,
            mnemonic,
            safeGetKeyDirectory()
        )
        return wallet.filename
    }
}