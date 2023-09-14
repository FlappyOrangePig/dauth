package com.infras.dauth.ui.fiat.transaction.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.infras.dauth.R
import com.infras.dauth.databinding.FragmentVerifySetProfileBinding
import com.infras.dauth.entity.KycProfileInfo
import com.infras.dauth.ext.isAreaCode
import com.infras.dauth.ext.isMail
import com.infras.dauth.ext.isPhone
import com.infras.dauth.ext.isVerifyCode
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauth.ui.fiat.transaction.viewmodel.KycSubmitViewModel
import com.infras.dauth.ui.fiat.transaction.viewmodel.VerifySetProfileEvent
import com.infras.dauth.ui.fiat.transaction.viewmodel.VerifySetProfileViewModel
import com.infras.dauth.util.ToastUtil
import com.infras.dauth.widget.LoadingDialogFragment
import com.infras.dauthsdk.wallet.base.BaseFragment
import kotlinx.coroutines.launch

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
    private val areaCodes = arrayOf("+86")
    private val viewModel: KycSubmitViewModel by activityViewModels()
    private val fVm: VerifySetProfileViewModel by viewModels()
    private val loadingDialog = LoadingDialogFragment.newInstance()

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
        tvSendEmail.setDebouncedOnClickListener {
            sendEmail()
        }
        tvSendSms.setDebouncedOnClickListener {
            sendSms()
        }
        tvContinue.setDebouncedOnClickListener {
            handleContinue()
        }
        spAreaCode.adapter =
            ArrayAdapter(requireContext(), R.layout.spinner_item_phone_area_code, areaCodes)
    }

    private fun initViewModel() {
        fVm.showLoading.observe(viewLifecycleOwner) {
            if (it) {
                loadingDialog.show(childFragmentManager, LoadingDialogFragment.TAG)
            } else {
                loadingDialog.dismissAllowingStateLoss()
            }
        }
        lifecycleScope.launch {
            fVm.toastEvent.collect { toast ->
                activity?.let { a -> ToastUtil.show(a, toast) }
            }
        }
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

    private fun handleContinue() {
        val context = requireActivity()
        if (showTab == 0) {
            val email = binding.etEmail.text?.toString().orEmpty()
            if (!email.isMail()) {
                ToastUtil.show(context, "mail format error")
                return
            }
            val verifyCode = binding.etEmailCode.text?.toString().orEmpty()
            if (!verifyCode.isVerifyCode()) {
                ToastUtil.show(context, "code format error")
                return
            }

            viewModel.profile = KycProfileInfo.Email(
                email = email,
                verifyCode = verifyCode
            )

            fVm.bindEmail(email = email, code = verifyCode)
        } else {
            val phone = binding.etPhone.text?.toString().orEmpty()
            if (!phone.isPhone()) {
                ToastUtil.show(context, "phone format error")
                return
            }
            val verifyCode = binding.etPhoneCode.text?.toString().orEmpty()
            if (!verifyCode.isVerifyCode()) {
                ToastUtil.show(context, "code format error")
                return
            }
            val pos = binding.spAreaCode.selectedItemPosition
            val areaCode = if (pos >= 0 && pos < areaCodes.size) {
                areaCodes[pos].removePrefix("+")
            } else {
                ""
            }
            if (!areaCode.isAreaCode()) {
                ToastUtil.show(context, "area code format error")
                return
            }

            viewModel.profile = KycProfileInfo.Phone(
                phone = phone,
                verifyCode = verifyCode,
                areaCode = areaCode
            )

            fVm.bindPhone(phone, areaCode, verifyCode)
        }
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
