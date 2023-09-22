package com.infras.dauth.ui.fiat.transaction.fragment

import android.os.Bundle
import android.view.View
import com.infras.dauth.R
import com.infras.dauth.entity.FiatOrderDetailItemEntity
import com.infras.dauthsdk.login.model.OrderDetailRes

class OrderDetailCancelFragment : OrderDetailCompleteFragment() {

    companion object {
        const val TAG = "OrderDetailCancelFragment"
        fun newInstance(data: OrderDetailRes.Data): OrderDetailCancelFragment {
            return OrderDetailCancelFragment().also {
                it.arguments = Bundle().apply { putParcelable(EXTRA_DATA, data) }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvPaid.visibility = View.GONE
    }

    override fun getTitleListData(): FiatOrderDetailItemEntity.Title? {
        return FiatOrderDetailItemEntity.Title(
            R.drawable.svg_ic_order_canceled,
            "Order cancelled",
            "The Buyer cancelled the order."
        )
    }
}