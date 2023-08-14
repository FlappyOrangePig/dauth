package com.infras.dauthsdk.wallet.impl.manager

import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.mpc.DAuthJniInvoker
import com.infras.dauthsdk.mpc.MpcKeyIds
import com.infras.dauthsdk.mpc.ext.runSpending
import com.infras.dauthsdk.wallet.ext.SafeFile.Companion.safe
import com.infras.dauthsdk.wallet.ext.digest
import com.infras.dauthsdk.wallet.ext.runCatchingWithLog
import com.infras.dauthsdk.wallet.util.ThreadUtil
import kotlinx.coroutines.delay
import java.io.File

internal class PreGenerateKeyManager(
    private val fileManager: FileManager,
    private val globalPrefsManager: GlobalPrefsManager,
) {
    companion object {
        private const val TAG = "PreGenerateKeyManager"
    }

    @Volatile
    private var generating = false

    private val initCaller by lazy {
        InitCaller()
    }

    fun initialize() {
        initCaller
    }

    private fun initInner() {
        DAuthLogger.d("init inner", TAG)
        ThreadUtil.runOnWorkerThread {
            val generated = globalPrefsManager.getKeyPreGenerated()
            if (generated) {
                DAuthLogger.d("init: already generated", TAG)
                return@runOnWorkerThread
            }
            startGenerate()
        }
    }

    private fun getFolder(): File {
        val f = fileManager.getFolder(FileManager.FOLDER_NAME_PRE_GENERATE_KEY, true)
        f.mkdirs()
        return f
    }

    private fun getKeyFile(index: String): File {
        return File(getFolder(), "key$index.txt")
    }

    private fun startGenerate() {
        if (generating) {
            DAuthLogger.d("startGenerate: generating, quit", TAG)
            return
        }
        generating = true

        runSpending(TAG, "start generate") {
            val keys = DAuthJniInvoker.generateSignKeys()
            keys.forEachIndexed { index, e ->
                runCatchingWithLog {
                    val f = getKeyFile(index.toString())
                    f.safe().safeWriteText(e)
                }
            }
        }
        globalPrefsManager.setKeyPreGenerated(true)
        DAuthLogger.d("gen finished, set to sp", TAG)

        generating = false
    }

    private fun ready(): Boolean {
        var result = true
        for (i in MpcKeyIds.getKeyIds()) {
            val f = getKeyFile(i)
            if (!f.safe().safeExists()) {
                result = false
                break
            }
        }
        DAuthLogger.d("ready:$result", TAG)
        return result
    }

    /**
     * 返回并删除预生成的key，返回的key必然不是[isNullOrEmpty]，空key将导致结果size小于3

     */
    suspend fun popKeys(): List<String> {
        val ready = ready()
        DAuthLogger.d("pop keys >> ready:$ready", TAG)
        if (!ready) {
            while (generating) {
                DAuthLogger.v("..", TAG)
                delay(10L)
            }
        }

        val r = MpcKeyIds.getKeyIds().mapNotNull {
            val f = getKeyFile(it)
            val k = f.safe().safeReadText()
            f.delete()
            if (k.isNullOrEmpty()) {
                return@mapNotNull null
            }
            k
        }.filter {
            it.isNotEmpty()
        }

        DAuthLogger.d("pop keys << ${r.map { it.digest() }}", TAG)
        return r
    }

    private inner class InitCaller {
        init {
            initInner()
        }
    }
}

