package com.infras.dauth.ui.fiat.transaction

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.infras.dauth.R
import com.infras.dauth.app.BaseActivity
import com.infras.dauth.databinding.ActivityOrdersBinding
import com.infras.dauth.databinding.ItemTabLayoutOrderStateBinding
import com.infras.dauth.ext.launch
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauth.ui.fiat.transaction.adapter.OrdersPagerAdapter
import com.infras.dauth.util.LogUtil
import com.infras.dauth.util.SystemUIUtil

class OrdersActivity : BaseActivity() {

    companion object {
        fun launch(context: Context) = context.launch(OrdersActivity::class.java)
    }

    private var _binding: ActivityOrdersBinding? = null
    private val binding get() = _binding!!
    private val pageListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            LogUtil.d(logTag, "onPageSelected $position")
        }
    }
    private val onTabSelectListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            LogUtil.d(logTag, "onTabSelected ${tab.position}")
            updateTabLayout()
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
        }
    }

    override fun showSystemUI() {
        SystemUIUtil.show(
            window,
            SystemUIUtil.ThemeDrawByDeveloper(statusBarColor = getColor(R.color.gray_f0))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityOrdersBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.initView()
    }

    private fun ActivityOrdersBinding.initView() {
        ivBack.setDebouncedOnClickListener {
            finish()
        }
        vpOrders.adapter = OrdersPagerAdapter(supportFragmentManager, lifecycle)
        vpOrders.isUserInputEnabled = false
        vpOrders.registerOnPageChangeCallback(pageListener)

        val tabLayout = tlIndicatorState
        tabLayout.addOnTabSelectedListener(onTabSelectListener)
        TabLayoutMediator(tabLayout, vpOrders) { tab, position ->
            tab.customView as? TextView ?: ItemTabLayoutOrderStateBinding.inflate(
                LayoutInflater.from(this@OrdersActivity),
                tabLayout,
                false
            ).also {
                tab.customView = it.root
            }
            tab.text = when (position) {
                0 -> "Pending"
                1 -> "Completed"
                else -> throw RuntimeException()
            }
        }.attach()
    }

    private fun updateTabLayout() {
        val tabLayout = binding.tlIndicatorState
        val sel = tabLayout.selectedTabPosition
        updateTabInTabLayout(0, 0 == sel)
        updateTabInTabLayout(1, 1 == sel)
    }

    private fun updateTabInTabLayout(position: Int, selected: Boolean) {
        val tabLayout = binding.tlIndicatorState
        val tv = tabLayout.getTabAt(position)?.customView as? TextView ?: return
        when (selected) {
            true -> {
                // change text size ?
            }

            false -> {
                // change text size ?
            }
        }
    }
}