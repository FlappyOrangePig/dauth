package com.infras.dauth.ui.fiat.transaction.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.infras.dauth.databinding.FragmentOrderDetailPendingPayBinding
import com.infras.dauth.entity.FiatOrderDetailItemEntity
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauth.repository.FiatTxRepository
import com.infras.dauth.ui.fiat.transaction.test.OrderDetailMockData
import com.infras.dauth.ui.fiat.transaction.util.UriUtil
import com.infras.dauthsdk.login.model.OrderDetailRes
import com.infras.dauthsdk.login.model.OrderPaidParam
import kotlinx.coroutines.launch

class OrderDetailPendingPayFragment : BaseOrderDetailFragment() {

    companion object {
        const val TAG = "OrderDetailPendingFragment"
        fun newInstance(data: OrderDetailRes.Data): OrderDetailPendingPayFragment {
            return OrderDetailPendingPayFragment().also {
                it.arguments = Bundle().apply { putParcelable(EXTRA_DATA, data) }
            }
        }
    }

    private var _binding: FragmentOrderDetailPendingPayBinding? = null
    val binding get() = _binding!!

    private var _pickMedia: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private val pickMedia get() = _pickMedia!!
    private var mediaPath: String? = null
    private val repo = FiatTxRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                val path = UriUtil.uriTransform(activity, uri) ?: return@registerForActivityResult
                mediaPath = path
                updatePage()
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailPendingPayBinding.inflate(inflater, container, false)
        binding.tvPaid.setDebouncedOnClickListener {
            requestPaid()
        }
        return binding.root
    }

    override fun generatePageListData(): List<FiatOrderDetailItemEntity> {
        val r = OrderDetailMockData.getDetailUIData()
        r.forEach {
            if (it is FiatOrderDetailItemEntity.Tips) {
                it.imagePath = mediaPath.orEmpty()
            }
        }
        return r
    }

    override fun onClickProof() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun requestPaid() {
        lifecycleScope.launch {
            val r = repo.orderPaid(OrderPaidParam(""))
            if (r != null && r.isSuccess()) {
                val data = r.data
                if (data != null) {

                }
            }
        }
    }
}