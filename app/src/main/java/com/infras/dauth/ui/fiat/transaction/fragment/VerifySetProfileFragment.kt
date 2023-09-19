package com.infras.dauth.ui.fiat.transaction.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.infras.dauth.R
import com.infras.dauth.app.BaseFragment
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.databinding.FragmentVerifySetProfileBinding
import com.infras.dauth.ext.isMail
import com.infras.dauth.ext.isPhone
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauth.ui.fiat.transaction.viewmodel.VerifySetProfileEvent
import com.infras.dauth.ui.fiat.transaction.viewmodel.VerifySetProfileViewModel
import kotlinx.coroutines.launch

class VerifySetProfileFragment : BaseFragment() {

    interface ProfileConfirmCallback {
        fun onConfirmProfile()
    }

    companion object {
        const val TAG = "VerifySetProfileFragment"
        private const val DEBUG_JUMP_DIRECTLY = false
        fun newInstance(): VerifySetProfileFragment {
            return VerifySetProfileFragment()
        }
    }

    private var _binding: FragmentVerifySetProfileBinding? = null
    private val binding get() = _binding!!
    private val fVm: VerifySetProfileViewModel by viewModels()

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
        initViewModel()
        fVm.fetchCountry()
    }

    private fun FragmentVerifySetProfileBinding.initView() {
        tvSwitchToEmail.setDebouncedOnClickListener {
            fVm.showTab.value = 0
        }
        tvSwitchToSms.setDebouncedOnClickListener {
            fVm.showTab.value = 1
        }
        tvSendEmail.setDebouncedOnClickListener {
            sendEmail()
        }
        tvSendSms.setDebouncedOnClickListener {
            sendSms()
        }
        tvContinue.setDebouncedOnClickListener {
            if (DEBUG_JUMP_DIRECTLY) {
                launchNextPage()
            } else {
                fVm.handleContinue()
            }
        }
    }

    private fun initViewModel() {
        lifecycleScope.launch {
            fVm.commonEvent.collect { event ->
                when (event) {
                    VerifySetProfileEvent.BindEmailSuccess -> {
                        launchNextPage()
                    }

                    VerifySetProfileEvent.BindPhoneSuccess -> {
                        launchNextPage()
                    }
                }
            }
        }
        fVm.showTab.observe(viewLifecycleOwner) { tab ->
            when (tab) {
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
        binding.etEmail.addTextChangedListener {
            fVm.mailContent.value = it?.toString()?:""
        }
        binding.etEmailCode.addTextChangedListener {
            fVm.mailCodeContent.value = it?.toString()?:""
        }
        binding.etPhone.addTextChangedListener {
            fVm.phoneContent.value = it?.toString()?:""
        }
        binding.etPhoneCode.addTextChangedListener {
            fVm.phoneCodeContent.value = it?.toString()?:""
        }
        binding.spAreaCode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                fVm.selectAreaCode(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        fVm.areaCodes.observe(viewLifecycleOwner) { code ->
            val strings = code.map { "(+${it.phoneAreaCode}) ${it.countryName}" }
            binding.spAreaCode.adapter =
                ArrayAdapter(requireContext(), R.layout.spinner_item_phone_area_code, strings)
        }
    }

    override fun getDefaultViewModel(): BaseViewModel {
        return fVm
    }

    private fun sendEmail() {
        val mail = binding.etEmail.text?.toString().orEmpty()
        if (mail.isMail()) {
            fVm.sendEmailVerifyCode(mail)
        }
    }

    private fun sendSms() {
        val phone = binding.etPhone.text?.toString().orEmpty()
        if (phone.isPhone()) {
            fVm.sendSms(phone, "86")
        }
    }

    private fun launchNextPage() {
        (activity as? ProfileConfirmCallback)?.onConfirmProfile()
    }
}
