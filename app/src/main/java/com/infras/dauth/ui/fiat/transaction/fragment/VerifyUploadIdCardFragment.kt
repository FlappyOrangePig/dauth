package com.infras.dauth.ui.fiat.transaction.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import coil.load
import com.infras.dauth.databinding.FragmentVerifyUploadIdCardBinding
import com.infras.dauth.entity.DocumentType
import com.infras.dauth.entity.KycName
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauth.ui.fiat.transaction.util.CacheFileUtil
import com.infras.dauth.ui.fiat.transaction.util.ImageBase64Util
import com.infras.dauth.ui.fiat.transaction.util.ImageScaleUtil
import com.infras.dauth.ui.fiat.transaction.util.StorageUtil
import com.infras.dauth.ui.fiat.transaction.viewmodel.KycSubmitViewModel
import com.infras.dauth.ui.fiat.transaction.viewmodel.VerifyUploadIdCardViewModel
import com.infras.dauth.util.LogUtil
import com.infras.dauth.util.ToastUtil
import com.infras.dauth.widget.LoadingDialogFragment
import com.infras.dauthsdk.login.model.AccountOpenParam
import com.infras.dauthsdk.wallet.base.BaseFragment
import java.io.File

class VerifyUploadIdCardFragment : BaseFragment() {

    companion object {
        const val TAG = "VerifyUploadIdCardFragment"
        fun newInstance(): VerifyUploadIdCardFragment {
            return VerifyUploadIdCardFragment()
        }
    }

    private var _binding: FragmentVerifyUploadIdCardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: KycSubmitViewModel by activityViewModels()
    private val fVm: VerifyUploadIdCardViewModel by viewModels()
    private val loadingDialog = LoadingDialogFragment.newInstance()
    private var _pickMediaSideA: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private val pickMediaSideA get() = _pickMediaSideA!!
    private var _pickMediaSideB: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private val pickMediaSideB get() = _pickMediaSideB!!
    private var pathA: String? = null
    private var pathB: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 参考教程：https://developer.android.com/training/data-storage/shared/photopicker?hl=zh-cn
        _pickMediaSideA =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                val path = uriTransform(uri) ?: return@registerForActivityResult
                pathA = path
                binding.ivSideA.load(path)
            }
        _pickMediaSideB =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                val path = uriTransform(uri) ?: return@registerForActivityResult
                pathB = path
                binding.ivSideB.load(path)
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
            onSubmit()
        }
    }

    private fun initViewModel() {
        fVm.showLoading.observe(viewLifecycleOwner) {
            if (it) {
                loadingDialog.show(childFragmentManager, LoadingDialogFragment.TAG)
            } else {
                loadingDialog.dismissAllowingStateLoss()
            }
        }
    }

    private fun onSubmit() {
        val document = viewModel.document ?: return

        val a = requireActivity()

        var full: String? = null
        var first: String? = null
        var middle: String? = null
        var last: String? = null
        when (document.kycName) {
            is KycName.FullName -> {
                full = document.kycName.name
            }

            is KycName.PartsName -> {
                first = document.kycName.first
                middle = document.kycName.middle
                last = document.kycName.last
            }
        }
        var idNum: String? = null
        val idType: Int = when (document.documentType) {
            is DocumentType.DriverSLicence -> 2
            is DocumentType.IDCard -> {
                idNum = document.documentType.number
                4
            }

            is DocumentType.Passport -> 1
        }

        val imageA = pathA.orEmpty()
        if (imageA.isEmpty()) {
            ToastUtil.show(a, "no image a")
            return
        }
        val imageB = pathB.orEmpty()
        if (imageB.isEmpty()) {
            ToastUtil.show(a, "no image b")
            return
        }
        val base64EncodedImageA = ImageBase64Util.getBase64EncodedImageFile(imageA)
        if (base64EncodedImageA == null) {
            ToastUtil.show(a, "image a base64 error")
            return
        }
        val base64EncodedImageB = ImageBase64Util.getBase64EncodedImageFile(imageB)
        if (base64EncodedImageB == null) {
            ToastUtil.show(a, "image b base64 error")
            return
        }

        val param = AccountOpenParam(
            first_name = first,
            middle_name = middle,
            last_name = last,
            full_name = full,
            id_type = idType,
            id_back_img = base64EncodedImageA,
            id_front_img = base64EncodedImageB,
            id_num = idNum,
            issuing_country = document.region
        )

        fVm.accountOpen(param)
    }

    private fun uriTransform(uri: Uri?): String? {
        uri ?: return null
        val a = activity ?: return null
        val dstDir = StorageUtil.getCacheImageDir(a)
        val file = CacheFileUtil.saveUriToCacheFile(a, uri) ?: return null
        val scaled = ImageScaleUtil.getScaledImage(dstDir, file.absolutePath)
        LogUtil.d(logTag, "${file.length()} -> ${File(scaled).length()}")
        return scaled
    }
}