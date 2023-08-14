package com.infras.dauthsdk.wallet.impl.manager

import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.mpc.MpcKeyIds
import com.infras.dauthsdk.wallet.ext.SafeFile.Companion.safe
import com.infras.dauthsdk.wallet.ext.runCatchingWithLog
import com.infras.dauthsdk.wallet.impl.manager.FileManager.Companion.FOLDER_NAME_KEYSTORE
import com.infras.dauthsdk.wallet.impl.manager.api.IKeyStore
import java.io.File

internal class KeyStoreManager private constructor(
    private val fileManager: FileManager,
    private val userId: String,
) : IKeyStore {
    companion object {
        private const val TAG = "KeyStoreManager"
        private const val DEBUG = true
        fun getInstance(userId: String): KeyStoreManager {
            return KeyStoreManager(fileManager = Managers.fileManager, userId = userId)
        }
    }

    private fun log(log: String) {
        if (DEBUG) {
            DAuthLogger.v("[$userId]$log", TAG)
        }
    }

    private fun getFolder(): File? {
        return runCatchingWithLog {
            val folder = fileManager.getFolder(FOLDER_NAME_KEYSTORE, true)
            File(folder, userId).also { file ->
                file.mkdirs()
            }
        }
    }

    private fun getKeyFile(index: String): File {
        return File(getFolder(), "key$index.txt")
    }

    private fun getMergeResultFile(): File {
        return File(getFolder(), "merge_result.txt")
    }

    private fun execTransaction(block: () -> Unit): Boolean = try {
        block.invoke()
        true
    } catch (t: Throwable) {
        DAuthLogger.e(t.stackTraceToString(), TAG)
        false
    }

    override fun getAllKeys(): List<String> {
        val r = runCatchingWithLog {
            MpcKeyIds.getKeyIds().mapNotNull {
                val keyFile = getKeyFile(it)
                keyFile.safe().safeReadText()
            }
        } ?: emptyList()
        log("getAllKeys ${r.map { it.length }}")
        return r
    }

    override fun setAllKeys(keys: List<String>) {
        val r = execTransaction {
            keys.mapIndexed { index, key ->
                val k = getKeyFile(index.toString())
                k.safe().safeWriteText(key)
            }
        }
        log("setAllKeys ${keys.map { it.length }} $r")
    }

    override fun setLocalKey(key: String) {
        val r = execTransaction {
            val f = getKeyFile(MpcKeyIds.getLocalId())
            f.safe().safeWriteText(key)
        }
        log("setLocalKey ${key.length} $r")
    }

    override fun getLocalKey(): String {
        return runCatchingWithLog {
            val keyFile = getKeyFile(MpcKeyIds.getLocalId())
            keyFile.safe().safeReadText()
        }.orEmpty().also {
            log("getLocalKey ${it.length}")
        }
    }

    override fun clear() {
        val r = runCatchingWithLog {
            getFolder()!!.deleteRecursively()
        } != null
        log("clear $r")
    }

    override fun releaseTempKeys() {
        val r = runCatchingWithLog {
            listOf(
                getKeyFile(MpcKeyIds.getDAuthId()),
                getKeyFile(MpcKeyIds.getAppId()),
                getMergeResultFile()
            ).forEach {
                it.delete()
            }
        } != null
        log("releaseTempKeys $r")
    }
}

