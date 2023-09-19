package com.infras.dauth.ui.fiat.transaction.fragment

import com.infras.dauth.entity.FiatOrderState

class OrdersCompletedFragment : OrdersPendingFragment() {

    companion object {
        fun newInstance(): OrdersCompletedFragment {
            return OrdersCompletedFragment()
        }
    }

    override fun getStateTags(): List<FiatOrderState> {
        return listOf(FiatOrderState.Completed.All, FiatOrderState.Completed.Fulfilled, FiatOrderState.Completed.Canceled)
    }
}