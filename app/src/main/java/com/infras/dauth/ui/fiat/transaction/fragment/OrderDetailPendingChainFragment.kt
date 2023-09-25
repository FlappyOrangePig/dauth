package com.infras.dauth.ui.fiat.transaction.fragment

import android.os.Bundle
import android.view.View
import com.infras.dauth.R
import com.infras.dauth.entity.FiatOrderDetailItemEntity
import com.infras.dauthsdk.login.model.OrderDetailRes
import java.util.Date

class OrderDetailPendingChainFragment : OrderDetailCompleteFragment() {

    companion object {
        fun newInstance(data: OrderDetailRes.Data): OrderDetailPendingChainFragment {
            return OrderDetailPendingChainFragment().also {
                it.arguments = Bundle().apply { putParcelable(EXTRA_DATA, data) }
            }
        }
    }

    override fun getTitleListData(): FiatOrderDetailItemEntity.Title? {
        val offTickInS = data.releaseCryptoTime / 1000L + 60L * 5L
        val nowInS = Date().time / 1000L
        val deltaS = (offTickInS - nowInS).takeIf { it > 0 } ?: 0
        val minutes = deltaS / 60
        val seconds = deltaS % 60
        val formattedTime = String.format("%02d:%02d", minutes, seconds)

        return FiatOrderDetailItemEntity.Title(
            R.drawable.svg_ic_order_pending_for_chain,
            "Pending for the Blockchain",
            "Expect to be confirmed by blockchain within $formattedTime"
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scheduleNextOnTimer()
    }
}