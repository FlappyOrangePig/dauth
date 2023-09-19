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

class OrderPendingForChain : OrderDetailCompleteFragment() {

    companion object {
        fun newInstance(data: OrderDetailRes.Data): OrderPendingForChain {
            return OrderPendingForChain().also {
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