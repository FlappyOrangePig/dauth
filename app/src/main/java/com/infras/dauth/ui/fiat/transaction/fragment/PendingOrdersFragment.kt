package com.infras.dauth.ui.fiat.transaction.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.MultiTypeAdapter
import com.infras.dauth.databinding.FragmentPendingOrdersBinding
import com.infras.dauth.entity.FiatOrderListItemEntity
import com.infras.dauth.entity.FiatOrderState
import com.infras.dauth.entity.FiatOrderState.Companion.CREATE_FAIL
import com.infras.dauth.ext.dp
import com.infras.dauth.ui.fiat.transaction.OrderDetailActivity
import com.infras.dauth.ui.fiat.transaction.adapter.OrderListItemBinder
import com.infras.dauth.ui.fiat.transaction.viewmodel.PendingOrdersViewModel
import com.infras.dauth.ui.fiat.transaction.widget.VerticalDividerItemDecoration
import com.infras.dauthsdk.wallet.base.BaseFragment

open class PendingOrdersFragment : BaseFragment() {

    companion object {
        const val TAG = "PendingOrdersFragment"
        fun newInstance(): PendingOrdersFragment {
            return PendingOrdersFragment()
        }
    }

    private var _binding: FragmentPendingOrdersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PendingOrdersViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPendingOrdersBinding.inflate(inflater, container, false)
        binding.initView()
        return binding.root
    }

    private fun FragmentPendingOrdersBinding.initView() {
        llOrderStateTags.apply {
            setData(getStateTags().map { it.displayText })
            select.observe(viewLifecycleOwner) {
                requestOrders()
            }
        }
        rvOrders.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            addItemDecoration(VerticalDividerItemDecoration(12.dp()))
            adapter = MultiTypeAdapter().also { a ->
                a.register(FiatOrderListItemEntity::class.java, OrderListItemBinder { bd ->
                    OrderDetailActivity.launch(requireActivity(), bd.orderId)
                })
            }
        }
        srRefresh.setOnRefreshListener {
            requestOrders()
        }
    }

    private fun initViewModel() {
        viewModel.list.observe(viewLifecycleOwner) {
            val a = (binding.rvOrders.adapter as MultiTypeAdapter)
            a.items = it
            a.notifyDataSetChanged()
        }
        viewModel.refreshing.observe(viewLifecycleOwner) {
            binding.srRefresh.isRefreshing = it
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
    }

    override fun onResume() {
        super.onResume()
        requestOrders()
    }

    private fun requestOrders() {
        val index = currentTag()
        val tag = getStateTags()[index]
        val state = tag.wireState
        val filtered = state.filterNot { it == CREATE_FAIL }
        val states = filtered.joinToString(",")
        viewModel.requestOrders(states)
    }

    open fun getStateTags(): List<FiatOrderState> {
        return listOf(FiatOrderState.Pending.All, FiatOrderState.Pending.InProgress, FiatOrderState.Pending.InDispute)
    }

    private fun currentTag(): Int {
        return binding.llOrderStateTags.select.value!!
    }
}