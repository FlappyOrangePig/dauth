package com.infras.dauth.ui.fiat.transaction.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.drakeet.multitype.ItemViewBinder
import com.infras.dauth.databinding.ItemFiatOrderDetailGroupBinding
import com.infras.dauth.databinding.ItemFiatOrderDetailSplitBinding
import com.infras.dauth.databinding.ItemFiatOrderDetailTextBinding
import com.infras.dauth.databinding.ItemFiatOrderDetailTipsBinding
import com.infras.dauth.databinding.ItemFiatOrderDetailTitleBinding
import com.infras.dauth.entity.FiatOrderDetailItemEntity
import com.infras.dauth.ext.dp
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauth.util.ClipBoardUtil

class OrderDetailGroupItemBinder(
) : ItemViewBinder<FiatOrderDetailItemEntity.Group, OrderDetailGroupItemBinder.ViewHolder>() {

    inner class ViewHolder(private val bd: ItemFiatOrderDetailGroupBinding) :
        RecyclerView.ViewHolder(bd.root) {
        fun bind(item: FiatOrderDetailItemEntity.Group) {
            bd.tvGroupName.text = item.name
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, item: FiatOrderDetailItemEntity.Group) {
        holder.bind(item)
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(ItemFiatOrderDetailGroupBinding.inflate(inflater, parent, false))
    }
}

class OrderDetailSplitItemBinder(
) : ItemViewBinder<FiatOrderDetailItemEntity.Split, OrderDetailSplitItemBinder.ViewHolder>() {

    inner class ViewHolder(private val bd: ItemFiatOrderDetailSplitBinding) :
        RecyclerView.ViewHolder(bd.root) {
    }

    override fun onBindViewHolder(holder: ViewHolder, item: FiatOrderDetailItemEntity.Split) {
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(ItemFiatOrderDetailSplitBinding.inflate(inflater, parent, false))
    }
}

class OrderDetailTipsItemBinder(
    private val onClickProof: () -> Unit
) : ItemViewBinder<FiatOrderDetailItemEntity.Tips, OrderDetailTipsItemBinder.ViewHolder>() {

    inner class ViewHolder(private val bd: ItemFiatOrderDetailTipsBinding) :
        RecyclerView.ViewHolder(bd.root) {
        fun bind(item: FiatOrderDetailItemEntity.Tips) {
            bd.tvCost.text = item.cost
            bd.tvAccountNameContent.text = item.accountName
            bd.tvAccountNumberContent.text = item.accountNumber
            val r = 5.dp().toFloat()
            bd.ivProof.load(item.imagePath) {
                transformations(
                    RoundedCornersTransformation(
                        r,
                        r,
                        r,
                        r,
                    )
                )
            }
            bd.ivProof.setDebouncedOnClickListener {
                onClickProof.invoke()
            }
            bd.llAccountName.setDebouncedOnClickListener {
                ClipBoardUtil.copyToClipboard(it.context, item.accountName)
            }
            bd.llAccountNumber.setDebouncedOnClickListener {
                ClipBoardUtil.copyToClipboard(it.context, item.accountNumber)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, item: FiatOrderDetailItemEntity.Tips) {
        holder.bind(item)
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(ItemFiatOrderDetailTipsBinding.inflate(inflater, parent, false))
    }
}

class OrderDetailTextItemBinder :
    ItemViewBinder<FiatOrderDetailItemEntity.Text, OrderDetailTextItemBinder.ViewHolder>() {

    inner class ViewHolder(private val bd: ItemFiatOrderDetailTextBinding) :
        RecyclerView.ViewHolder(bd.root) {
        fun bind(item: FiatOrderDetailItemEntity.Text) {
            bd.tvContent.text = item.displayContent
            bd.tvTitle.text = item.title
            bd.tvContent.setTypeface(null, if (item.boldContent) Typeface.BOLD else Typeface.NORMAL)
            if (item.canCopy) {
                bd.ivCopy.visibility = View.VISIBLE
                bd.root.setDebouncedOnClickListener {
                    ClipBoardUtil.copyToClipboard(it.context, item.content.toString())
                }
            } else {
                bd.ivCopy.visibility = View.GONE
                bd.root.setDebouncedOnClickListener {}
            }

            bd.ivCopy.visibility = if (item.canCopy) View.VISIBLE else View.GONE
            bd.root.setDebouncedOnClickListener {
                if (item.canCopy) {
                    ClipBoardUtil.copyToClipboard(it.context, item.content.toString())
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, item: FiatOrderDetailItemEntity.Text) {
        holder.bind(item)
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(ItemFiatOrderDetailTextBinding.inflate(inflater, parent, false))
    }
}

class OrderDetailTitleItemBinder :
    ItemViewBinder<FiatOrderDetailItemEntity.Title, OrderDetailTitleItemBinder.ViewHolder>() {

    inner class ViewHolder(private val bd: ItemFiatOrderDetailTitleBinding) :
        RecyclerView.ViewHolder(bd.root) {
        fun bind(item: FiatOrderDetailItemEntity.Title) {
            bd.ivIcon.setImageResource(item.icon)
            bd.tvTitle.text = item.title
            bd.tvTitleDesc.text = item.desc
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, item: FiatOrderDetailItemEntity.Title) {
        holder.bind(item)
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(ItemFiatOrderDetailTitleBinding.inflate(inflater, parent, false))
    }
}
