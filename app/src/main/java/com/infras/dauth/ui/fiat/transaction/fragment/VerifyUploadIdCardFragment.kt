package com.infras.dauth.ui.fiat.transaction.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import coil.load
import com.infras.dauth.app.BaseFragment
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.databinding.FragmentVerifyUploadIdCardBinding
import com.infras.dauth.entity.DocumentType
import com.infras.dauth.entity.KycDocumentInfo
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauth.ui.fiat.transaction.util.UriUtil
import com.infras.dauth.ui.fiat.transaction.viewmodel.VerifyUploadIdCardViewModel
import com.infras.dauthsdk.wallet.ext.getParcelableExtraCompat

class VerifyUploadIdCardFragment : BaseFragment() {

    companion object {
        const val EXTRA_DOC_INFO = "EXTRA_DOC_INFO"
        fun newInstance(info: KycDocumentInfo?): VerifyUploadIdCardFragment {
            return VerifyUploadIdCardFragment().also {
                it.arguments = Bundle().apply {
                    putParcelable(EXTRA_DOC_INFO, info)
                }
            }
        }
    }

    private var _binding: FragmentVerifyUploadIdCardBinding? = null
    private val binding get() = _binding!!
    private val fVm: VerifyUploadIdCardViewModel by viewModels()
    private var _pickMediaSideA: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private val pickMediaSideA get() = _pickMediaSideA!!
    private var _pickMediaSideB: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private val pickMediaSideB get() = _pickMediaSideB!!
    private val document get() = arguments?.getParcelableExtraCompat<KycDocumentInfo>(EXTRA_DOC_INFO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 参考教程：https://developer.android.com/training/data-storage/shared/photopicker?hl=zh-cn
        _pickMediaSideA =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                val path = UriUtil.uriTransform(activity, uri, "1") ?: return@registerForActivityResult
                fVm.pathA.value = path
            }
        _pickMediaSideB =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                val path = UriUtil.uriTransform(activity, uri, "2") ?: return@registerForActivityResult
                fVm.pathB.value = path
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifyUploadIdCardBinding.inflate(inflater, container, false)
        binding.initView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
    }

    private fun FragmentVerifyUploadIdCardBinding.initView() {
        flSideA.setDebouncedOnClickListener {
            pickMediaSideA.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        flSideB.setDebouncedOnClickListener {
            pickMediaSideB.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        tvContinue.setDebouncedOnClickListener {
            this@VerifyUploadIdCardFragment.document?.let {
                fVm.submit(it)
            }
        }
        document?.let {
            if (it.documentType.picCount == 1) {
                flSideB.visibility = View.GONE
            }

            when (it.documentType) {
                is DocumentType.IDCard -> {
                    tvSideATips.text = "+\nUpload the front of your ID card"
                    tvSideBTips.text = "+\nUpload the back of your ID card"
                }

                is DocumentType.Passport -> {
                    tvSideATips.text = "+\nUpload the index of your Passport"
                    tvSideBTips.text = "+\nUpload the deputy of your Passport"
                }

                is DocumentType.DriverSLicence -> {
                    tvSideATips.text = "+\nUpload the index of your Driver’s License"
                    tvSideBTips.text = "+\nUpload the deputy of your Driver’s License"
                }
            }
        }
    }

    private fun initViewModel() {
        fVm.pathA.observe(viewLifecycleOwner) {
            binding.ivSideA.load(it)
        }
        fVm.pathB.observe(viewLifecycleOwner) {
            binding.ivSideB.load(it)
        }
        fVm.createSuccessEvent.observe(viewLifecycleOwner) {
            activity?.finish()
        }
    }

    override fun getDefaultViewModel(): BaseViewModel {
        return fVm
    }
}