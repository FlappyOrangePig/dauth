package com.infras.dauth.ui.fiat.transaction.fragment

import com.infras.dauth.entity.FiatOrderState

class CompletedOrdersFragment : PendingOrdersFragment() {

    companion object {
        const val TAG = "CompletedOrdersFragment"
        fun newInstance(): CompletedOrdersFragment {
            return CompletedOrdersFragment()
        }
    }

    override fun getStateTags(): List<FiatOrderState> {
        return listOf(FiatOrderState.Completed.All, FiatOrderState.Completed.Fulfilled, FiatOrderState.Completed.Canceled)
    }
}