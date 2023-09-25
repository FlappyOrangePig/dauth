package com.infras.dauth.ui.fiat.transaction.widget

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.MultiTypeAdapter
import com.infras.dauth.entity.FiatOrderDetailItemEntity
import com.infras.dauth.ext.dp
import com.infras.dauth.ui.fiat.transaction.adapter.OrderDetailGroupItemBinder
import com.infras.dauth.ui.fiat.transaction.adapter.OrderDetailImageItemBinder
import com.infras.dauth.ui.fiat.transaction.adapter.OrderDetailSplitItemBinder
import com.infras.dauth.ui.fiat.transaction.adapter.OrderDetailTextItemBinder
import com.infras.dauth.ui.fiat.transaction.adapter.OrderDetailTipsItemBinder
import com.infras.dauth.ui.fiat.transaction.adapter.OrderDetailTitleItemBinder

class OrderDetailListView(
    private val rv: RecyclerView,
    private val onClickProof: (() -> Unit)? = null,
) {
    private var lastData = mutableListOf<FiatOrderDetailItemEntity>()

    init {
        rv.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            addItemDecoration(VerticalDividerItemDecoration(0.dp()))
            adapter = MultiTypeAdapter().also { a ->
                a.register(FiatOrderDetailItemEntity.Text::class.java, OrderDetailTextItemBinder())
                a.register(
                    FiatOrderDetailItemEntity.Group::class.java,
                    OrderDetailGroupItemBinder()
                )
                a.register(
                    FiatOrderDetailItemEntity.Split::class.java,
                    OrderDetailSplitItemBinder()
                )
                a.register(FiatOrderDetailItemEntity.Tips::class.java, OrderDetailTipsItemBinder {
                    onClickProof?.invoke()
                })
                a.register(
                    FiatOrderDetailItemEntity.Title::class.java,
                    OrderDetailTitleItemBinder()
                )
                a.register(
                    FiatOrderDetailItemEntity.Image::class.java,
                    OrderDetailImageItemBinder()
                )
            }
        }
    }

    fun updateAll(new: List<FiatOrderDetailItemEntity>) {
        val a = (rv.adapter as MultiTypeAdapter)
        val old = a.items
        a.items = new
        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return old.size
            }

            override fun getNewListSize(): Int {
                return new.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldBean = old[oldItemPosition] as? FiatOrderDetailItemEntity ?: return false
                val newBean = new[newItemPosition]
                return oldBean.getKey() == newBean.getKey()
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldBean = old[oldItemPosition]
                val newBean = new[newItemPosition]
                return oldBean == newBean
            }

            override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
                return null
            }
        })
        result.dispatchUpdatesTo(a)
    }
}