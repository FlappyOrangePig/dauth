package com.infras.dauth.ui.fiat.transaction.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.ItemViewBinder
import com.infras.dauth.databinding.ItemFiatOrderListBinding
import com.infras.dauth.entity.FiatOrderListItemEntity
import com.infras.dauth.ext.setDebouncedOnClickListener

class OrderListItemBinder(
    private val onItemClick: (FiatOrderListItemEntity) -> Unit
) : ItemViewBinder<FiatOrderListItemEntity, OrderListItemBinder.ViewHolder>() {

    inner class ViewHolder(private val bd: ItemFiatOrderListBinding) :
        RecyclerView.ViewHolder(bd.root) {
        fun bind(item: FiatOrderListItemEntity) {
            bd.tvTitle.text = item.title
            bd.tvState.text = item.state
            bd.tvQuantity.text = item.quantity
            bd.tvUnitPrice.text = item.unitPrice
            bd.tvTotalPrice.text = item.totalPrice
            bd.tvTime.text = item.time
            bd.root.setDebouncedOnClickListener {
                onItemClick.invoke(item)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, item: FiatOrderListItemEntity) {
        holder.bind(item)
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(ItemFiatOrderListBinding.inflate(inflater, parent, false))
    }
}

