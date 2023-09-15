package com.infras.dauth.ui.fiat.transaction

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.infras.dauth.R
import com.infras.dauth.app.BaseActivity
import com.infras.dauth.databinding.ActivityOrderDetailBinding
import com.infras.dauth.ext.launch
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauth.util.SystemUIUtil

class OrderDetailActivity : BaseActivity() {

    companion object {
        private const val EXTRA_ORDER_ID = ""
        fun launch(context: Context, orderId: String) =
            context.launch(OrderDetailActivity::class.java) {
                it.putExtra(EXTRA_ORDER_ID, orderId)
            }
    }

    private var _binding: ActivityOrderDetailBinding? = null
    private val binding get() = _binding!!
    private val orderId get() = intent.getStringExtra(EXTRA_ORDER_ID).orEmpty()

    override fun showSystemUI() {
        SystemUIUtil.show(
            window,
            SystemUIUtil.ThemeDrawByDeveloper(statusBarColor = getColor(R.color.gray_f0))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityOrderDetailBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.initView()
    }

    private fun ActivityOrderDetailBinding.initView() {
        ivBack.setDebouncedOnClickListener {
            finish()
        }
    }
}