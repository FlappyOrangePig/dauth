package com.infras.dauth.ui.fiat.transaction.fragment

import android.os.Bundle
import com.infras.dauth.R
import com.infras.dauth.entity.FiatOrderDetailItemEntity
import com.infras.dauthsdk.login.model.OrderDetailRes

class OrderDetailPendingChainFragment : OrderDetailCompleteFragment() {

    companion object {
        fun newInstance(data: OrderDetailRes.Data): OrderDetailPendingChainFragment {
            return OrderDetailPendingChainFragment().also {
                it.arguments = Bundle().apply { putParcelable(EXTRA_DATA, data) }
            }
        }
    }

    override fun getTitleListData(): FiatOrderDetailItemEntity.Title? {
        return FiatOrderDetailItemEntity.Title(
            R.drawable.svg_ic_order_pending_for_chain,
            "Pending for the Blockchain",
            "Expect to be confirmed by blockchain within 9:59s"
        )
    }
}