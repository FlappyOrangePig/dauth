package com.infras.dauth.ui.fiat.transaction.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.infras.dauth.R
import com.infras.dauth.databinding.FragmentOrderDetailDisputeBinding
import com.infras.dauth.entity.FiatOrderDetailItemEntity
import com.infras.dauth.ui.fiat.transaction.util.TimeUtil
import com.infras.dauthsdk.login.model.OrderDetailRes

class OrderDetailDisputeFragment : BaseOrderDetailFragment() {

    companion object {
        const val TAG = "OrderDetailDisputeFragment"
        fun newInstance(data: OrderDetailRes.Data): OrderDetailDisputeFragment {
            return OrderDetailDisputeFragment().also {
                it.arguments = Bundle().apply { putParcelable(EXTRA_DATA, data) }
            }
        }
    }

    private var _binding: FragmentOrderDetailDisputeBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailDisputeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getTitleListData(): FiatOrderDetailItemEntity.Title? {
        return FiatOrderDetailItemEntity.Title(
            R.drawable.svg_ic_order_dispute,
            "The order is in dispute",
            "A chat progress has already been initiated, please join the chat to figure out what’s going on..."
        )
    }
}