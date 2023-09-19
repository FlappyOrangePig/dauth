package com.infras.dauth.ui.fiat.transaction.fragment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.infras.dauth.R
import com.infras.dauth.app.BaseFragment
import com.infras.dauth.entity.FiatOrderDetailItemEntity
import com.infras.dauth.ui.fiat.transaction.util.OrderDetailListComposeUtil
import com.infras.dauth.ui.fiat.transaction.widget.OrderDetailListView
import com.infras.dauthsdk.login.model.OrderDetailRes
import com.infras.dauthsdk.wallet.ext.getParcelableExtraCompat

abstract class BaseOrderDetailFragment : BaseFragment() {
    companion object {
        const val EXTRA_DATA = "EXTRA_DATA"
    }

    val data get() = arguments?.getParcelableExtraCompat<OrderDetailRes.Data>(EXTRA_DATA)!!
    private lateinit var listView: OrderDetailListView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.rv_detail)
        listView = OrderDetailListView(rv) {
            onClickProof()
        }
        updatePage()
    }

    open fun onClickProof() {}

    open fun generatePageListData(): List<FiatOrderDetailItemEntity> {
        val title = getTitleListData()
        if (title != null) {
            return OrderDetailListComposeUtil.all(title, data)
        }
        return listOf()
    }

    open fun getTitleListData(): FiatOrderDetailItemEntity.Title? = null

    fun updatePage() {
        listView.updateAll(generatePageListData())
    }
}