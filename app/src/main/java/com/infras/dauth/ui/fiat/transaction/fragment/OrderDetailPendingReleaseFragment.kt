package com.infras.dauth.ui.fiat.transaction.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

    private val handle = Handler(Looper.getMainLooper())
    private val runnable = Runnable {
        updatePage()
        scheduleNextOnTimer()
    }

    override fun getTitleListData(): FiatOrderDetailItemEntity.Title? {
        val now = Date()
        val payTime = Date(data.payTime)
        val deltaMs = /*if (now.time - payTime.time < 5 * 60L * 1000L) {
            now.time - payTime.time
        } else {
            0L
        } / 1000L*/
            now.time - payTime.time

        val minutes = deltaMs / (1000 * 60)
        val seconds = (deltaMs / 1000) % 60
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

    override fun onDestroy() {
        super.onDestroy()
        handle.removeCallbacksAndMessages(null)
    }

    private fun scheduleNextOnTimer() {
        handle.removeCallbacks(runnable)
        handle.postDelayed(runnable, 1000L)
    }
}