package com.infras.dauth.ui.transaction.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.infras.dauth.R
import com.infras.dauth.databinding.FragmentVerifySetProfileBinding
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauthsdk.wallet.base.BaseFragment

class VerifySetProfileFragment : BaseFragment() {

    interface ProfileConfirmCallback {
        fun onConfirmProfile()
    }

    companion object {
        const val TAG = "VerifySetProfileFragment"
        fun newInstance(): VerifySetProfileFragment {
            return VerifySetProfileFragment()
        }
    }

    private var _binding: FragmentVerifySetProfileBinding? = null
    private val binding get() = _binding!!
    private var showTab = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifySetProfileBinding.inflate(inflater, container, false)
        binding.initView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateTab()
    }

    private fun FragmentVerifySetProfileBinding.initView() {
        tvSwitchToEmail.setDebouncedOnClickListener {
            showTab = 0
            updateTab()
        }
        tvSwitchToSms.setDebouncedOnClickListener {
            showTab = 1
            updateTab()
        }
        tvContinue.setDebouncedOnClickListener {
            (activity as? ProfileConfirmCallback)?.onConfirmProfile()
        }
        val list = arrayOf("+86")
        spAreaCode.adapter =
            ArrayAdapter(requireContext(), R.layout.spinner_item_phone_area_code, list)
    }

    private fun updateTab() {
        when (showTab) {
            0 -> {
                binding.clEmail.visibility = View.VISIBLE
                binding.clPhone.visibility = View.GONE
            }

            else -> {
                binding.clEmail.visibility = View.GONE
                binding.clPhone.visibility = View.VISIBLE
            }
        }
    }
}
