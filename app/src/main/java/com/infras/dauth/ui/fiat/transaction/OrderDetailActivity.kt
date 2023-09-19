package com.infras.dauth.ui.fiat.transaction

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.infras.dauth.R
import com.infras.dauth.app.BaseActivity
import com.infras.dauth.databinding.ActivityOrderDetailBinding
import com.infras.dauth.entity.FiatOrderState
import com.infras.dauth.ext.launch
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauth.repository.FiatTxRepository
import com.infras.dauth.ui.fiat.transaction.fragment.OrderDetailCancelFragment
import com.infras.dauth.ui.fiat.transaction.fragment.OrderDetailCompleteFragment
import com.infras.dauth.ui.fiat.transaction.fragment.OrderDetailDisputeFragment
import com.infras.dauth.ui.fiat.transaction.fragment.OrderDetailPendingPayFragment
import com.infras.dauth.ui.fiat.transaction.fragment.OrderPendingForChain
import com.infras.dauth.ui.fiat.transaction.fragment.OrderPendingForSellerSReleaseFragment
import com.infras.dauth.ui.fiat.transaction.widget.NeedHelpDialogFragment
import com.infras.dauth.widget.LoadingDialogFragment
import com.infras.dauthsdk.login.model.OrderDetailParam
import com.infras.dauthsdk.login.model.OrderDetailRes
import kotlinx.coroutines.launch

class OrderDetailActivity : BaseActivity(), NeedHelpDialogFragment.HelpDialogCallback {

    companion object {
        private const val EXTRA_ORDER_ID = "EXTRA_ORDER_ID"
        fun launch(context: Context, orderId: String) =
            context.launch(OrderDetailActivity::class.java) {
                it.putExtra(EXTRA_ORDER_ID, orderId)
            }
    }

    private var _binding: ActivityOrderDetailBinding? = null
    private val binding get() = _binding!!
    private val orderId get() = intent.getStringExtra(EXTRA_ORDER_ID).orEmpty()
    private val repo = FiatTxRepository()
    private val loadingDialog = LoadingDialogFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityOrderDetailBinding.inflate(LayoutInflater.from(this))
        binding.initView()
        setContentView(binding.root)
        refresh()
    }

    private fun ActivityOrderDetailBinding.initView() {
        ivBack.setDebouncedOnClickListener {
            finish()
        }
    }

    private fun refresh() {
        if (false) {
            replaceFragment(OrderDetailRes.Data())
        } else {
            lifecycleScope.launch {
                loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
                val o = if (false) {
                    "1703754270844653568"
                } else orderId
                val r = repo.orderDetail(OrderDetailParam(o))
                loadingDialog.dismissAllowingStateLoss()
                if (r != null && r.isSuccess()) {
                    val data = r.data
                    if (data != null) {
                        replaceFragment(data)
                    }
                }
            }
        }
    }

    private fun replaceFragment(data: OrderDetailRes.Data) {
        val state = if (true) {
            FiatOrderState.PAID
        } else {
            data.state ?: return
        }
        val f = when (state) {
            FiatOrderState.APPEAL -> {
                OrderDetailDisputeFragment.newInstance(data)
            }

            FiatOrderState.UNPAID -> {
                OrderDetailPendingPayFragment.newInstance(data)
            }

            FiatOrderState.PAID -> {
                OrderPendingForSellerSReleaseFragment.newInstance(data)
            }

            FiatOrderState.COMPLETED -> {
                OrderPendingForChain.newInstance(data)
            }

            FiatOrderState.WITHDRAW_FAIL, FiatOrderState.WITHDRAW_SUCCESS -> {
                OrderDetailCompleteFragment.newInstance(data)
            }

            FiatOrderState.CANCEL -> {
                OrderDetailCancelFragment.newInstance(data)
            }

            FiatOrderState.CREATE_FAIL -> {
                OrderDetailCompleteFragment.newInstance(data)
            }

            else -> {
                OrderDetailCompleteFragment.newInstance(data)
            }
        }

        supportFragmentManager.beginTransaction().apply {
            replace(
                R.id.cl_fragment_container,
                f
            )
        }.commitAllowingStateLoss()
    }

    override fun onHelpItemClick(index: Int) {
        refresh()
    }
}