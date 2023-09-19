package com.infras.dauth.ui.fiat.transaction.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.infras.dauth.R
import com.infras.dauth.databinding.FragmentOrderDetailCompleteBinding
import com.infras.dauth.entity.FiatOrderDetailItemEntity
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauth.ui.fiat.transaction.util.TimeUtil
import com.infras.dauth.ui.fiat.transaction.widget.NeedHelpDialogFragment
import com.infras.dauthsdk.login.model.OrderDetailRes

open class OrderDetailCompleteFragment : BaseOrderDetailFragment() {

    companion object {
        const val TAG = "OrderDetailCompleteFragment"
        fun newInstance(data: OrderDetailRes.Data): OrderDetailCompleteFragment {
            return OrderDetailCompleteFragment().also {
                it.arguments = Bundle().apply { putParcelable(EXTRA_DATA, data) }
            }
        }
    }

    private var _binding: FragmentOrderDetailCompleteBinding? = null
    val binding get() = _binding!!
    private val dialog by lazy { NeedHelpDialogFragment.newInstance(getDialogStyle()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailCompleteBinding.inflate(inflater, container, false)
        binding.tvPaid.setDebouncedOnClickListener {
            dialog.show(childFragmentManager, NeedHelpDialogFragment.TAG)
        }
        return binding.root
    }

    override fun getTitleListData(): FiatOrderDetailItemEntity.Title? {
        return FiatOrderDetailItemEntity.Title(
            R.drawable.svg_ic_order_complete,
            "Pending for your payment",
            "Pay within 19:59s"
        )
    }

    open fun getDialogStyle() = 1
}