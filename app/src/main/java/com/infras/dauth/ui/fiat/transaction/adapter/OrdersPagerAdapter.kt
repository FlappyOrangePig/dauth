package com.infras.dauth.ui.fiat.transaction.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.infras.dauth.ui.fiat.transaction.fragment.OrdersCompletedFragment
import com.infras.dauth.ui.fiat.transaction.fragment.OrdersPendingFragment

class OrdersPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            OrdersPendingFragment.newInstance()
        } else {
            OrdersCompletedFragment.newInstance()
        }
    }
}