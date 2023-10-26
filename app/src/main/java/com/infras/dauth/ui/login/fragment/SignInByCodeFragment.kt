package com.infras.dauth.ui.login.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.infras.dauth.R
import com.infras.dauth.app.BaseFragment
import com.infras.dauth.databinding.FragmentSignInByCodeBinding
import com.infras.dauth.ext.launchMainPage
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauth.repository.SignInResult
import com.infras.dauth.ui.login.viewmodel.SignInByCodeViewModel
import com.infras.dauth.util.DemoPrefs
import com.infras.dauth.util.ToastUtil
import com.infras.dauth.widget.LoadingDialogFragment
import kotlinx.coroutines.launch

class SignInByCodeFragment private constructor() : BaseFragment() {
    private var _binding: FragmentSignInByCodeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignInByCodeViewModel by viewModels()
    private val loadingDialog = LoadingDialogFragment.newInstance()

    companion object {
        fun newInstance(): SignInByCodeFragment {
            return SignInByCodeFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInByCodeBinding.inflate(inflater, container, false)
        binding.etAccount.addTextChangedListener {
            viewModel.updateAccount(it?.toString() ?: "")
        }
        binding.etPassword.addTextChangedListener {
            viewModel.updateVerifyCode(it?.toString() ?: "")
        }
        binding.btnSendCode.setDebouncedOnClickListener {
            lifecycleScope.launch {
                loadingDialog.show(childFragmentManager, LoadingDialogFragment.TAG)
                val result = viewModel.sendVerifyCode()
                loadingDialog.dismissAllowingStateLoss()
                ToastUtil.show(
                    it.context,
                    getString(if (result) R.string.success else (R.string.failure))
                )
            }
        }
        binding.btnSubmit.setDebouncedOnClickListener { v ->
            lifecycleScope.launch {
                loadingDialog.show(childFragmentManager, LoadingDialogFragment.TAG)
                val result = viewModel.sendSubmitRequest()
                loadingDialog.dismissAllowingStateLoss()
                requireActivity().apply {
                    ToastUtil.show(
                        this,
                        result.digest()
                    )
                    if (result is SignInResult.Success) {
                        this.launchMainPage()
                        this.finish()
                    }
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.etAccount.setText(DemoPrefs.getLastEmail())
    }
}