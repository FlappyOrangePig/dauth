package com.cyberflow.dauthsdk.wallet.util

import com.cyberflow.dauthsdk.DAuthSDK
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Bip39Wallet
import org.web3j.crypto.Bip44WalletUtils
import org.web3j.crypto.Credentials
import org.web3j.crypto.MnemonicUtils
import org.web3j.crypto.WalletUtils
import java.io.File

/**
 * 凭据工具类
 */
object CredentialsUtil {

    private val context get() = (DAuthSDK.instance as DAuthSDK).context
    // 测试每次都创建，正式使用sp保存
    private const val TEST = true
    private const val PASSWORD = ""
    private const val MNEMONIC =
        "nominee video milk cake style decide blind sponsor rabbit mule dutch vanish"
    private const val WALLECT_ADDRESS = "0xd3Ca5938af1Cce97A4B45ea775E8a291eF53BA8C"
    //private const val WALLECT_ADDRESS =  0x4b4de226750e6f1569604912f2af42f53275258b

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
        return createWalletFileByMnemonic(MNEMONIC, PASSWORD).filename
    }

    private fun getWalletFile(): File {
        val finalFileName = if (TEST) {
            generateWalletFile()
        } else {
            val pref = WalletPrefs(context)
            val walletFileName = pref.getWalletFileName()
            walletFileName.ifEmpty {
                generateWalletFile().also {
                    pref.setWalletFileName(it)
                }
            }
        }
        DAuthLogger.d("getWalletFile finalFileName=$finalFileName")
        return File(safeGetKeyDirectory(), finalFileName)
    }

    fun loadCredentials(): Credentials {
        val f = getWalletFile()
        val r = WalletUtils.loadCredentials(PASSWORD, f)
        DAuthLogger.d("loadCredentials finalFileName=${r.address}")
        return r
    }

    private fun createWalletFileFromMnemonic(mnemonic: String, password: String): String {
        val wallet = WalletUtils.generateBip39WalletFromMnemonic(
            password,
            mnemonic,
            safeGetKeyDirectory()
        )

        DAuthLogger.d("createWalletFileFromMnemonic, mnemonic=$mnemonic")
        return wallet.filename
    }

    private fun createWalletFileFromMnemonic2(mnemonic: String, password: String): String {
        val wallet = Bip44WalletUtils.generateBip44Wallet(password, safeGetKeyDirectory())
        val credentials = Bip44WalletUtils.loadBip44Credentials(password, mnemonic)
        DAuthLogger.d("createWalletFileFromMnemonic2, credentials=${credentials.address}")
        return wallet.filename
    }

    private fun getCredentialByMnemonic(mnemonic: String, password: String){
        val seed = MnemonicUtils.generateSeed(mnemonic, password)
        val masterKeypair = Bip32ECKeyPair.generateKeyPair(seed)
        val childKeypair = Bip44WalletUtils.generateBip44KeyPair(masterKeypair, false)
        val credential = Credentials.create(childKeypair)
        DAuthLogger.d("getCredential" + credential.address)
    }

    private fun createWalletFileByMnemonic(
        mnemonic: String,
        password: String = "",
        testNet: Boolean = false
    ): Bip39Wallet {
        val seed = MnemonicUtils.generateSeed(mnemonic, password)
        val masterKeypair = Bip32ECKeyPair.generateKeyPair(seed)
        val bip44Keypair = Bip44WalletUtils.generateBip44KeyPair(masterKeypair, testNet)

        val walletFile =
            WalletUtils.generateWalletFile(password, bip44Keypair, safeGetKeyDirectory(), false)

        return Bip39Wallet(walletFile, mnemonic)
    }
}