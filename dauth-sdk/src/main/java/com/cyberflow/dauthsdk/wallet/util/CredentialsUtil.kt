package com.cyberflow.dauthsdk.wallet.util

import com.cyberflow.dauthsdk.wallet.impl.DAuthWallet
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import java.io.File

object CredentialsUtil {

    private val context get() = (DAuthWallet.instance as DAuthWallet).context
    private const val PASSWORD = "123456"

    private fun getKeyDirectory(): File {
        val fileDir = context.applicationContext.filesDir
        return File(fileDir, "keys")
    }

    private fun getKeyDirectoryAndCreateDirectory(): File {
        return getKeyDirectory().also {
            it.mkdirs()
        }
    }

    private fun generateWalletFile(): String {
        val keysDir = getKeyDirectoryAndCreateDirectory()
        // 返回文件名
        return WalletUtils.generateFullNewWalletFile(PASSWORD, keysDir)
    }

    private fun getWalletFile(): File {
        val pref = WalletPrefs(context)
        val walletFileName = pref.getWalletFileName()
        val finalFileName = walletFileName.ifEmpty {
            generateWalletFile().also {
                pref.setWalletFileName(it)
            }
        }
        return File(getKeyDirectoryAndCreateDirectory(), finalFileName)
    }

    fun loadCredentials(): Credentials {
        val f = getWalletFile()
        return WalletUtils.loadCredentials(PASSWORD, f)
    }
}