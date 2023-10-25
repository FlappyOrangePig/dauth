package com.infras.dauth.ui.fiat.transaction.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.infras.dauth.app.BaseFragment
import com.infras.dauth.databinding.FragmentVerifyDeniedBinding
import com.infras.dauth.ui.fiat.transaction.viewmodel.VerifyDeniedViewModel

class VerifyDeniedFragment : BaseFragment() {

    companion object {
        const val TAG = "VerifyDeniedFragment"
        fun newInstance(): VerifyDeniedFragment {
            return VerifyDeniedFragment()
        }
    }

    private var _binding: FragmentVerifyDeniedBinding? = null
    private val binding get() = _binding!!
    private val fVm: VerifyDeniedViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifyDeniedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestData()
    }

    private fun requestData() {
        fVm.requestPageData()
    }

    private fun updatePageUi(
        region: String,
        name: String,
        docType: String,
        docNo: String,
    ) {
        binding.apply {
            tvRegion.text = region
            tvFullName.text = name
            tvDocumentType.text = docType
            tvDocumentNumber.text = docNo
        }
    }
}