package com.infras.dauth.ui.transaction.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.INVALID_POSITION
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import com.infras.dauth.R
import com.infras.dauth.databinding.FragmentVerifySetIdCardBinding
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauth.widget.LoadingDialogFragment
import com.infras.dauthsdk.wallet.base.BaseFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class CountryInfo(
    val countryName: String,
    val useFullName: Boolean,
)

class VerifySetIdCardFragment : BaseFragment() {

    interface IdCardConfirmCallback {
        fun onConfirmIdCard()
    }

    companion object {
        const val TAG = "VerifySetIdCardFragment"
        fun newInstance(): VerifySetIdCardFragment {
            return VerifySetIdCardFragment()
        }
    }

    private var _binding: FragmentVerifySetIdCardBinding? = null
    private val binding get() = _binding!!
    private var countries: MutableList<CountryInfo> = mutableListOf()
    private val loadingDialog = LoadingDialogFragment.newInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifySetIdCardBinding.inflate(inflater, container, false)
        binding.initView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchData()
    }

    private fun FragmentVerifySetIdCardBinding.initView() {
        tvContinue.setDebouncedOnClickListener {
            (activity as? IdCardConfirmCallback)?.onConfirmIdCard()
        }
        spAreaCode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updatePage()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    private fun updatePage() {
        val position = binding.spAreaCode.selectedItemPosition

        if (INVALID_POSITION == position) {
            binding.svRoot.visibility = View.GONE
        } else {
            binding.svRoot.visibility = View.VISIBLE

            val item = countries[position]
            when (item.useFullName) {
                true -> {
                    binding.llNameFull.visibility = View.VISIBLE
                    binding.llNameParts.visibility = View.GONE
                }

                false -> {
                    binding.llNameFull.visibility = View.GONE
                    binding.llNameParts.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun fetchData() {
        lifecycleScope.launch {
            loadingDialog.show(childFragmentManager, LoadingDialogFragment.TAG)
            delay(1000)
            loadingDialog.dismiss()

            countries = mutableListOf(
                CountryInfo("Japan", true),
                CountryInfo("U.S.A.", false)
            )

            binding.spAreaCode.adapter = ArrayAdapter(
                requireContext(),
                R.layout.spinner_item_phone_area_code,
                countries.map { it.countryName })

            updatePage()
        }
    }
}