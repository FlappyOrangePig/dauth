package com.infras.dauth.ui.transaction.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.infras.dauth.databinding.FragmentVerifySetIdCardBinding
import com.infras.dauth.databinding.FragmentVerifyUploadIdCardBinding
import com.infras.dauth.widget.LoadingDialogFragment
import com.infras.dauthsdk.wallet.base.BaseFragment

class VerifyUploadIdCardFragment : BaseFragment() {

    companion object {
        const val TAG = "VerifyUploadIdCardFragment"
        fun newInstance(): VerifyUploadIdCardFragment {
            return VerifyUploadIdCardFragment()
        }
    }

    private var _binding: FragmentVerifyUploadIdCardBinding? = null
    private val binding get() = _binding!!
    private var countries: MutableList<CountryInfo> = mutableListOf()
    private val loadingDialog = LoadingDialogFragment.newInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifyUploadIdCardBinding.inflate(inflater, container, false)
        binding.initView()
        return binding.root
    }

    private fun FragmentVerifyUploadIdCardBinding.initView() {

    }
}