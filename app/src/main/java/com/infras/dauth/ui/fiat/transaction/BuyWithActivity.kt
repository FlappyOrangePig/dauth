package com.infras.dauth.ui.fiat.transaction

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.infras.dauth.BuildConfig
import com.infras.dauth.app.BaseActivity
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.databinding.ActivityBuyWithBinding
import com.infras.dauth.entity.BuyWithPageInputEntity
import com.infras.dauth.ext.dp
import com.infras.dauth.ext.launch
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauth.ui.fiat.transaction.adapter.PayMethodAdapter
import com.infras.dauth.ui.fiat.transaction.viewmodel.BuyWithViewModel
import com.infras.dauth.ui.fiat.transaction.widget.VerticalDividerItemDecoration
import com.infras.dauth.util.ConvertUtil
import com.infras.dauth.util.DialogHelper
import com.infras.dauthsdk.wallet.ext.getParcelableExtraCompat


class BuyWithActivity : BaseActivity() {

    companion object {
        private const val EXTRA_INPUT = "EXTRA_INPUT"
        fun launch(context: Context, input: BuyWithPageInputEntity) {
            context.launch(BuyWithActivity::class.java) {
                it.putExtra(EXTRA_INPUT, input)
            }
        }
    }

    private var _binding: ActivityBuyWithBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BuyWithViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.attachInput(intent.getParcelableExtraCompat(EXTRA_INPUT)!!)
        _binding = ActivityBuyWithBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.initView()
        initViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateUI()
    }

    private fun initViewModel() {
        viewModel.address.observe(this) {
            binding.tvAddress.text = it
        }
        viewModel.payMethods.observe(this) {
            (binding.rvPayMethod.adapter as PayMethodAdapter).setData(it)
        }
        viewModel.createdOrderIdState.observe(this) {
            OrderDetailActivity.launch(this, it)
        }
        viewModel.quoteState.observe(this) {
            binding.tvWillPay.text = it
        }
    }

    private fun ActivityBuyWithBinding.initView() {
        ivBack.setDebouncedOnClickListener {
            finish()
        }
        tvBuy.setDebouncedOnClickListener {
            if (BuildConfig.IS_LIVE) {
                viewModel.fetchWithdrawConf("ARBITRUMONE")
            } else {
                //viewModel.fetchWithdrawConf("ETH")

                DialogHelper.showInputDialogMayHaveLeak(
                    this@BuyWithActivity,
                    "Enter ChainId"
                ) { chainId ->
                    viewModel.buy(chainId)
                }
            }
        }

        val amount = ConvertUtil.addCommasToNumber(viewModel.input.buyCount)
        val token = viewModel.getTokenCode()
        val text = "$amount $token"
        tvTokenAmount.text = text

        rvPayMethod.apply {
            val orientation = RecyclerView.VERTICAL
            layoutManager = LinearLayoutManager(context, orientation, false)
            adapter = PayMethodAdapter(onClickItem = { item -> viewModel.selectItem(item) })
            addItemDecoration(VerticalDividerItemDecoration(12.dp()))
        }
    }

    override fun getDefaultViewModel(): BaseViewModel {
        return viewModel
    }
}