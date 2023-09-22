package com.infras.dauth.ui.fiat.transaction.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.infras.dauth.databinding.ItemPayMethodBinding
import com.infras.dauth.entity.PayMethodChooseListEntity
import com.infras.dauth.ext.setDebouncedOnClickListener
import kotlin.math.pow

class PayMethodAdapter(
    private val onClickItem: (PayMethodChooseListEntity) -> Unit
) : Adapter<PayMethodVH>() {

    private var list = mutableListOf<PayMethodChooseListEntity>()

    fun setData(list: List<PayMethodChooseListEntity>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PayMethodVH {
        val bd = ItemPayMethodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PayMethodVH(bd, onClickItem)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: PayMethodVH, position: Int) {
        val item = list[position]
        holder.bind(item, position)
    }
}


class PayMethodVH(
    private val bd: ItemPayMethodBinding,
    private val onClickItem: (PayMethodChooseListEntity) -> Unit
) : ViewHolder(bd.root) {
    fun bind(item: PayMethodChooseListEntity, position: Int) {
        bd.root.setDebouncedOnClickListener {
            onClickItem.invoke(item)
        }
        bd.tvMethodName.text = item.payMethodInfo.payMethodName
        bd.tvMethodPrice.text = ""
        bd.ivCheck.isSelected = item.isSelected
        bd.vColorBlock.setBackgroundColor(convertIntegerToColor(position))
    }

    private fun convertIntegerToColor(number: Int): Int {
        val red = (2.toFloat().pow(number) + 85) % 256
        val green = (number * 83 + 170) % 256
        val blue = (number.toFloat().pow(3) + 255) % 256
        return Color.rgb(red.toInt(), green, blue.toInt())
    }
}