package com.infras.dauth.ui.fiat.transaction

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import coil.load
import com.infras.dauth.R
import com.infras.dauth.app.BaseActivity
import com.infras.dauth.databinding.ActivityOrderDetailBinding
import com.infras.dauth.entity.FiatOrderState
import com.infras.dauth.ext.launch
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauth.manager.AppManagers
import com.infras.dauth.repository.FiatTxRepository
import com.infras.dauth.ui.fiat.transaction.fragment.OnClickOrderDetailImage
import com.infras.dauth.ui.fiat.transaction.fragment.OrderDetailCancelFragment
import com.infras.dauth.ui.fiat.transaction.fragment.OrderDetailCompleteFragment
import com.infras.dauth.ui.fiat.transaction.fragment.OrderDetailDisputeFragment
import com.infras.dauth.ui.fiat.transaction.fragment.OrderDetailPendingChainFragment
import com.infras.dauth.ui.fiat.transaction.fragment.OrderDetailPendingPayFragment
import com.infras.dauth.ui.fiat.transaction.fragment.OrderDetailPendingReleaseFragment
import com.infras.dauth.ui.fiat.transaction.widget.NeedHelpDialogFragment
import com.infras.dauth.util.ToastUtil
import com.infras.dauth.widget.LoadingDialogFragment
import com.infras.dauthsdk.login.model.OrderAppealParam
import com.infras.dauthsdk.login.model.OrderCancelAppealParam
import com.infras.dauthsdk.login.model.OrderCancelParam
import com.infras.dauthsdk.login.model.OrderDetailParam
import com.infras.dauthsdk.login.model.OrderDetailRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderDetailActivity : BaseActivity(), NeedHelpDialogFragment.HelpDialogCallback,
    OnClickOrderDetailImage {

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
    private val resourceManager get() = AppManagers.resourceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityOrderDetailBinding.inflate(LayoutInflater.from(this))
        binding.initView()
        setContentView(binding.root)
        refresh(null)
    }

    private fun ActivityOrderDetailBinding.initView() {
        ivBack.setDebouncedOnClickListener {
            finish()
        }
        ivImage.setDebouncedOnClickListener {
            ivImage.setImageResource(0)
            ivImage.visibility = View.GONE
        }
    }

    private fun refresh(delay: Long? = 1000L) {
        if (false) {
            replaceFragment(OrderDetailRes.Data())
        } else {
            lifecycleScope.launch {
                delay?.let { d ->
                    withContext(Dispatchers.IO) {
                        delay(d)
                    }
                }
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
                } else {
                    ToastUtil.show(
                        this@OrderDetailActivity,
                        resourceManager.getResponseDigest(r)
                    )
                }
            }
        }
    }

    private fun replaceFragment(data: OrderDetailRes.Data) {
        val state = if (false) {
            FiatOrderState.PAID
        } else {
            data.state ?: return
        }
        binding.tvCancel.visibility = View.GONE
        val f = when (state) {
            FiatOrderState.APPEAL -> {
                binding.tvCancel.visibility = View.VISIBLE
                binding.tvCancel.text = "Cancel appeal"
                binding.tvCancel.setDebouncedOnClickListener(onClickCancelAppeal)
                OrderDetailDisputeFragment.newInstance(data)
            }

            FiatOrderState.UNPAID -> {
                binding.tvCancel.visibility = View.VISIBLE
                binding.tvCancel.setDebouncedOnClickListener(onClickCancel)
                OrderDetailPendingPayFragment.newInstance(data)
            }

            FiatOrderState.PAID -> {
                binding.tvCancel.visibility = View.GONE
                binding.tvCancel.setDebouncedOnClickListener(onClickCancel)
                OrderDetailPendingReleaseFragment.newInstance(data)
            }

            FiatOrderState.COMPLETED, FiatOrderState.WITHDRAW_FAIL -> {
                OrderDetailPendingChainFragment.newInstance(data)
            }

            FiatOrderState.WITHDRAW_SUCCESS -> {
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
        lifecycleScope.launch {
            loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
            val r = repo.orderAppeal(OrderAppealParam(orderId, index))
            loadingDialog.dismissAllowingStateLoss()
            ToastUtil.show(
                this@OrderDetailActivity,
                resourceManager.getResponseDigest(r)
            )
            if (r != null && r.isSuccess()) {
                refresh()
            }
        }
    }

    private val onClickCancel = View.OnClickListener { v ->
        lifecycleScope.launch {
            loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
            val r = repo.orderCancel(OrderCancelParam(orderId))
            loadingDialog.dismissAllowingStateLoss()
            ToastUtil.show(v.context, resourceManager.getResponseDigest(r))
            if (r != null && r.isSuccess()) {
                withContext(Dispatchers.IO) {
                    delay(2000L)
                }
                refresh()
            }
        }
    }

    private val onClickCancelAppeal = View.OnClickListener { v ->
        lifecycleScope.launch {
            loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
            val r = repo.orderCancelAppeal(OrderCancelAppealParam(orderId))
            loadingDialog.dismissAllowingStateLoss()
            ToastUtil.show(v.context, resourceManager.getResponseDigest(r))
            if (r != null && r.isSuccess()) {
                withContext(Dispatchers.IO) {
                    delay(2000L)
                }
                refresh()
            }
        }
    }

    override fun onClickImage(url: String) {
        binding.ivImage.load(url)
        binding.ivImage.visibility = View.VISIBLE
    }
}