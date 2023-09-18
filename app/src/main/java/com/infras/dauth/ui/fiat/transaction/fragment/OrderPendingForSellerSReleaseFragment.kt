package com.infras.dauth.ui.fiat.transaction.fragment

import android.os.Bundle
import com.infras.dauth.R
import com.infras.dauth.entity.FiatOrderDetailItemEntity
import com.infras.dauthsdk.login.model.OrderDetailRes

class OrderPendingForSellerSReleaseFragment : OrderDetailCompleteFragment() {

    companion object {
        fun newInstance(data: OrderDetailRes.Data): OrderPendingForSellerSReleaseFragment {
            return OrderPendingForSellerSReleaseFragment().also {
                it.arguments = Bundle().apply { putParcelable(EXTRA_DATA, data) }
            }
        }
    }

    override fun getTitleListData(): FiatOrderDetailItemEntity.Title? {
        return FiatOrderDetailItemEntity.Title(
            R.drawable.svg_ic_order_pending_for_seller_s_release,
            "Pending for seller’s Release",
            "Expect to receive USDT within 04:59s"
        )
    }
}