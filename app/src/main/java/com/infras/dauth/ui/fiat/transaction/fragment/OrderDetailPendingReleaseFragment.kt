package com.infras.dauth.ui.fiat.transaction.fragment

import android.os.Bundle
import android.view.View
import com.infras.dauth.R
import com.infras.dauth.entity.FiatOrderDetailItemEntity
import com.infras.dauthsdk.login.model.OrderDetailRes
import java.util.Date

class OrderDetailPendingReleaseFragment : OrderDetailCompleteFragment() {

    companion object {
        fun newInstance(data: OrderDetailRes.Data): OrderDetailPendingReleaseFragment {
            return OrderDetailPendingReleaseFragment().also {
                it.arguments = Bundle().apply { putParcelable(EXTRA_DATA, data) }
            }
        }
    }

    override fun getTitleListData(): FiatOrderDetailItemEntity.Title? {
        val offTickInS = data.payTime / 1000L + 60L * 5L
        val nowInS = Date().time / 1000L
        val deltaS = (offTickInS - nowInS).takeIf { it > 0 } ?: 0
        val minutes = deltaS / 60
        val seconds = deltaS % 60
        val formattedTime = String.format("%02d:%02d", minutes, seconds)

        return FiatOrderDetailItemEntity.Title(
            R.drawable.svg_ic_order_pending_for_seller_s_release,
            "Pending for seller’s Release",
            "Expect to receive USDT within $formattedTime"
        )
    }

    override fun getDialogStyle() = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scheduleNextOnTimer()
    }
}