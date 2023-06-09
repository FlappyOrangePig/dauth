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
    private val config get() = DAuthSDK.instance.config
    private val useTestNetwork get() = config.useTestNetwork
    private val useInnerAccount get() = config.useInnerTestAccount
    // 测试每次都创建，正式版本使用sp保存
    private const val ALWAYS_CREATE_WALLET = false
    private const val PASSWORD = ""
    private const val MNEMONIC = "nominee video milk cake style decide blind sponsor rabbit mule dutch vanish"
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

    private fun createWallet(): Bip39Wallet {
        val r = if (useInnerAccount) {
            createWalletFileByMnemonic(MNEMONIC, PASSWORD)
        } else {
            createInitialWallet()
        }
        if (DebugUtil.isAppDebuggable(context)) {
            DAuthLogger.e("\n********************************************************************************\n(only shown in debug mode)\ncreate EOA wallet, mnemonic:\n${r.mnemonic}\nyou can send eth to this account by MetaMask before AA account is finished\n********************************************************************************")
        }
        return r
    }

    private fun getWalletFile(): File {
        val finalFileName = if (ALWAYS_CREATE_WALLET) {
            createWallet().filename
        } else {
            val pref = WalletPrefs(context)
            val walletFileName = pref.getWalletFileName()
            walletFileName.ifEmpty {
                createWallet().filename.also {
                    pref.setWallet(it)
                }
            }
        }
        DAuthLogger.d("getWalletFile finalFileName=$finalFileName")
        return File(safeGetKeyDirectory(), finalFileName)
    }

    /**
     * 加载凭据
     */
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

    /**
     * 随机创建一个初始钱包
     */
    private fun createInitialWallet(): Bip39Wallet {
        return Bip44WalletUtils.generateBip44Wallet(
            PASSWORD,
            safeGetKeyDirectory(),
            useTestNetwork
        )
    }

    /**
     * 通过助记词获取凭据，不生成钱包文件，暂时没有使用场景
     */
    private fun getCredentialByMnemonic(mnemonic: String, password: String): Credentials {
        val seed = MnemonicUtils.generateSeed(mnemonic, password)
        val masterKeypair = Bip32ECKeyPair.generateKeyPair(seed)
        val childKeypair = Bip44WalletUtils.generateBip44KeyPair(masterKeypair, useTestNetwork)
        val credential = Credentials.create(childKeypair)
        DAuthLogger.d("getCredential" + credential.address)
        return credential
    }

    /**
     * 用助记词创建钱包
     * 根据bip44，根据主秘钥按路径管理一批秘钥
     * @see <a href=https://github.com/bitcoin/bips/blob/master/bip-0039.mediawiki>haha</a>
     */
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