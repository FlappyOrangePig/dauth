package com.infras.dauth.ui.fiat.transaction.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.infras.dauth.R
import com.infras.dauth.databinding.FragmentVerifySetIdCardBinding
import com.infras.dauth.entity.DocumentType
import com.infras.dauth.entity.KycDocumentInfo
import com.infras.dauth.entity.KycName
import com.infras.dauth.entity.KycOpenAccountMethod
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauth.ui.fiat.transaction.viewmodel.KycSubmitViewModel
import com.infras.dauth.ui.fiat.transaction.viewmodel.VerifySetIdCardViewModel
import com.infras.dauth.util.ToastUtil
import com.infras.dauth.widget.LoadingDialogFragment
import com.infras.dauthsdk.login.model.CountryListRes.CountryInfo
import com.infras.dauthsdk.wallet.base.BaseFragment
import kotlinx.coroutines.launch

class VerifySetIdCardFragment : BaseFragment() {

    interface IdCardConfirmCallback {
        fun onConfirmIdCard()
    }

    companion object {
        const val TAG = "VerifySetIdCardFragment"
        private const val DEBUG_JUMP_DIRECTLY = false
        fun newInstance(): VerifySetIdCardFragment {
            return VerifySetIdCardFragment()
        }
    }

    private var _binding: FragmentVerifySetIdCardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: KycSubmitViewModel by activityViewModels()
    private val fVm: VerifySetIdCardViewModel by viewModels()
    private val loadingDialog = LoadingDialogFragment.newInstance()
    private val checkViews by lazy {
        listOf(binding.ivCheckIdCard, binding.ivCheckPassport, binding.ivCheckDrive)
    }

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
        initViewModel()
    }

    override fun onResume() {
        super.onResume()
        fVm.fetchCountry()
    }

    private fun FragmentVerifySetIdCardBinding.initView() {
        tvContinue.setDebouncedOnClickListener {
            if (DEBUG_JUMP_DIRECTLY) {
                (activity as? IdCardConfirmCallback)?.onConfirmIdCard()
            } else {
                handleContinue()
            }
        }
        spAreaCode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                fVm.selectCountry(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        checkViews.forEachIndexed { index, imageView ->
            imageView.setDebouncedOnClickListener {
                fVm.selectOpenAccountType(index)
            }
        }
    }

    private fun initViewModel() {
        fVm.countries.observe(viewLifecycleOwner) { countries ->
            binding.spAreaCode.adapter = ArrayAdapter(
                requireContext(),
                R.layout.spinner_item_phone_area_code,
                countries.map { it.countryName })

            binding.flRoot.visibility = if (countries.isEmpty()) View.GONE else View.VISIBLE
        }
        fVm.openAccountMethod.observe(viewLifecycleOwner) {
            updateOpenAccountMethod(it)
        }
        fVm.fullName.observe(viewLifecycleOwner) {
            if (it) {
                binding.llNameFull.visibility = View.VISIBLE
                binding.llNameParts.visibility = View.GONE
            } else {
                binding.llNameFull.visibility = View.GONE
                binding.llNameParts.visibility = View.VISIBLE
            }
        }
        fVm.currentDocumentType.observe(viewLifecycleOwner) {
            updateCheckView(it)
        }
        lifecycleScope.launch {
            fVm.showLoading.observe(viewLifecycleOwner) {
                if (it) {
                    loadingDialog.show(childFragmentManager, LoadingDialogFragment.TAG)
                } else {
                    loadingDialog.dismissAllowingStateLoss()
                }
            }
        }
    }

    private fun getCurrentCountryInfo(): CountryInfo? {
        val position = binding.spAreaCode.selectedItemPosition
        val countries = fVm.countries.value.orEmpty()
        return if (position >= 0 && position < countries.size) {
            countries[position]
        } else {
            null
        }
    }

    private fun updateCheckView(currentDocumentType: Int?) {
        checkViews.partition {
            currentDocumentType == checkViews.indexOf(it)
        }.let { partition ->
            partition.first.forEach { it.isSelected = true }
            partition.second.forEach { it.isSelected = false }
        }
    }

    private fun updateOpenAccountMethod(kycOpenAccountMethod: KycOpenAccountMethod) {
        binding.clIdCard.visibility = if (kycOpenAccountMethod.idCard) View.VISIBLE else View.GONE
        binding.clPassport.visibility =
            if (kycOpenAccountMethod.passport) View.VISIBLE else View.GONE
        binding.clDriver.visibility =
            if (kycOpenAccountMethod.driverSLicence) View.VISIBLE else View.GONE
    }

    private fun handleContinue() {
        val context = requireActivity()
        val countryInfo = getCurrentCountryInfo()
        if (countryInfo == null) {
            ToastUtil.show(context, "countryInfo is null")
            return
        }

        val kycName = when (countryInfo.useFullName) {
            false -> {
                val first = binding.etFirstName.text?.toString().orEmpty()
                val middle = binding.etMiddleName.text?.toString().orEmpty()
                val last = binding.etLastName.text?.toString().orEmpty()

                if (first.isEmpty()) {
                    ToastUtil.show(context, "first name is empty")
                    return
                }
                if (middle.isEmpty()) {
                    ToastUtil.show(context, "middle name is empty")
                    return
                }
                if (last.isEmpty()) {
                    ToastUtil.show(context, "last name is empty")
                    return
                }

                KycName.PartsName(
                    first = first,
                    middle = middle,
                    last = last
                )
            }

            true -> {
                val full = binding.etFullName.text?.toString().orEmpty()
                if (full.isEmpty()) {
                    ToastUtil.show(context, "full name is empty")
                    return
                }

                KycName.FullName(
                    full
                )
            }
        }

        val documentType = when (fVm.currentDocumentType.value) {
            0 -> {
                val id = binding.etIdCard.text?.toString().orEmpty()
                if (id.isEmpty()) {
                    ToastUtil.show(context, "id card id is empty")
                    return
                }
                DocumentType.IDCard(id)
            }

            1 -> {
                ToastUtil.show(context, "passport id is empty")
                return
            }

            2 -> {
                ToastUtil.show(context, "driver's licence id is empty")
                return
            }

            else -> {
                ToastUtil.show(context, "select document type")
                return
            }
        }

        val documentInfo = KycDocumentInfo(
            region = countryInfo.countryName,
            kycName = kycName,
            documentType = documentType
        )
        viewModel.document = documentInfo

        (activity as? IdCardConfirmCallback)?.onConfirmIdCard()
    }
}