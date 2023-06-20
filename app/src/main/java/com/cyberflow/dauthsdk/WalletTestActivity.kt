package com.cyberflow.dauthsdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauth.databinding.ActivityWalletTestBinding
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.mpc.websocket.WebsocketManager
import com.cyberflow.dauthsdk.wallet.impl.EoaWallet
import kotlinx.coroutines.launch

private const val TAG = "WalletTestActivity"

class WalletTestActivity : AppCompatActivity() {

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, WalletTestActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var _binding: ActivityWalletTestBinding? = null
    private val binding: ActivityWalletTestBinding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWalletTestBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.btnCreateWallet.setOnClickListener {
            lifecycleScope.launch {
                val result = DAuthSDK.instance.createWallet("")
            }
        }
        binding.btnSign.setOnClickListener {
            val session = WebsocketManager.instance.createDefaultSession()
            session?.onEvent = {
                Log.i(TAG, "onEvent $it")
            }
        }
    }
}