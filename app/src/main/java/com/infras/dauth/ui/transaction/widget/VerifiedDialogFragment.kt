package com.infras.dauth.ui.transaction.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.infras.dauth.databinding.DialogFragmentVerifiedBinding
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauth.widget.dialog.BottomDialogFragment

class VerifiedDialogFragment : BottomDialogFragment() {

    companion object {
        const val TAG = "VerifiedDialogFragment"
        private const val EXTRA_USER_ID = "EXTRA_USER_ID"

        fun newInstance(userId: String): VerifiedDialogFragment {
            return VerifiedDialogFragment().also {
                it.arguments = Bundle().apply { putString(EXTRA_USER_ID, userId) }
            }
        }
    }

    private var _binding: DialogFragmentVerifiedBinding? = null
    private val binding get() = _binding!!
    private val userId get() = requireArguments().getString(EXTRA_USER_ID)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentVerifiedBinding.inflate(inflater, container, false)
        binding.tvDauthId.text = userId.toString()
        binding.tvOk.setDebouncedOnClickListener {
            dismiss()
        }
        return binding.root
    }
}