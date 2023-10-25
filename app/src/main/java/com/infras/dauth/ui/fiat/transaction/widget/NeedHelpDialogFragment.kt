package com.infras.dauth.ui.fiat.transaction.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.infras.dauth.databinding.DialogFragmentNeedHelpBinding
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauth.widget.dialog.BottomDialogFragment

class NeedHelpDialogFragment : BottomDialogFragment() {

    interface HelpDialogCallback {
        fun onHelpItemClick(index: Int)
    }

    companion object {
        const val TAG = "NeedHelpDialogFragment"
        private const val EXTRA_STYLE = "EXTRA_STYLE"
        fun newInstance(style: Int): NeedHelpDialogFragment {
            return NeedHelpDialogFragment().also {
                it.arguments = Bundle().apply {
                    putInt(EXTRA_STYLE, style)
                }
            }
        }
    }

    private var _binding: DialogFragmentNeedHelpBinding? = null
    private val binding get() = _binding!!
    private val style get() = arguments?.getInt(EXTRA_STYLE) ?: 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentNeedHelpBinding.inflate(inflater, container, false)
        when (style) {
            1 -> {
                binding.tvNotRelease.alpha = .5f
                binding.tvNotRelease.isEnabled = false
            }

            else -> {
                binding.tvNotReceive.alpha = .5f
                binding.tvNotReceive.isEnabled = false
            }
        }
        binding.tvNotRelease.setDebouncedOnClickListener {
            dispatchOnClick(0)
        }
        binding.tvNotReceive.setDebouncedOnClickListener {
            dispatchOnClick(1)
        }
        return binding.root
    }

    private fun dispatchOnClick(index: Int) {
        dismiss()
        (activity as? HelpDialogCallback)?.onHelpItemClick(index)
    }

    override fun onStart() {
        super.onStart()
        isCancelable = true
    }
}