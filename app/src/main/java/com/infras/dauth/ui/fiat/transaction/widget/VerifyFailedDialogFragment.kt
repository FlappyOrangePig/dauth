package com.infras.dauth.ui.fiat.transaction.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.infras.dauth.databinding.DialogFragmentVerifyFailedBinding
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauth.ui.fiat.transaction.KycSubmitActivity
import com.infras.dauth.widget.dialog.BottomDialogFragment

class VerifyFailedDialogFragment : BottomDialogFragment() {

    companion object {
        const val TAG = "VerifyFailedDialogFragment"
        private const val EXTRA_USER_ID = "EXTRA_USER_ID"
        private const val EXTRA_IS_BOUND = "EXTRA_IS_BOUND"

        fun newInstance(userId: String, isBound: Boolean): VerifyFailedDialogFragment {
            return VerifyFailedDialogFragment().also {
                it.arguments = Bundle().apply {
                    putString(EXTRA_USER_ID, userId)
                    putBoolean(EXTRA_IS_BOUND, isBound)
                }
            }
        }
    }

    private var _binding: DialogFragmentVerifyFailedBinding? = null
    private val binding get() = _binding!!
    private val userId get() = requireArguments().getString(EXTRA_USER_ID)
    private val isBound get() = requireArguments().getBoolean(EXTRA_IS_BOUND)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentVerifyFailedBinding.inflate(inflater, container, false)
        binding.tvDauthId.text = userId.toString()
        binding.tvLatter.setDebouncedOnClickListener {
            dismiss()
        }
        binding.tvGetVerified.setDebouncedOnClickListener {
            dismiss()
            KycSubmitActivity.launch(it.context, isBound = isBound)
        }
        return binding.root
    }
}