package com.infras.dauth.ui.fiat.transaction.fragment

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.infras.dauth.R
import com.infras.dauth.databinding.FragmentOrderDetailPendingPayBinding
import com.infras.dauth.entity.FiatOrderDetailItemEntity
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauth.manager.AppManagers
import com.infras.dauth.repository.FiatTxRepository
import com.infras.dauth.ui.fiat.transaction.BuyAndSellActivity
import com.infras.dauth.ui.fiat.transaction.test.OrderDetailMockData
import com.infras.dauth.ui.fiat.transaction.util.CurrencyCalcUtil
import com.infras.dauth.ui.fiat.transaction.util.CurrencyCalcUtil.scale
import com.infras.dauth.ui.fiat.transaction.util.OrderDetailListComposeUtil
import com.infras.dauth.ui.fiat.transaction.util.UriUtil
import com.infras.dauth.util.ToastUtil
import com.infras.dauth.widget.LoadingDialogFragment
import com.infras.dauthsdk.login.model.OrderDetailRes
import com.infras.dauthsdk.login.model.OrderPaidParam
import kotlinx.coroutines.launch
import java.util.Date

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
    private var offTickInS: Long = 0
    private val loadingDialog = LoadingDialogFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                val path = UriUtil.uriTransform(activity, uri) ?: return@registerForActivityResult
                mediaPath = path
                updatePage()
            }
        // 创建时使用刚拉取到的剩余时间确认到期时间
        offTickInS = System.currentTimeMillis() / 1000L + data.payTimeoutTime
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
        val r = if (false) {
            OrderDetailMockData.getDetailUIData()
        } else {
            val nowInS = System.currentTimeMillis() / 1000L
            val deltaS = (offTickInS - nowInS).takeIf { it > 0 } ?: 0
            val minutes = deltaS / 60
            val seconds = deltaS % 60
            val formattedTime = String.format("%02d:%02d", minutes, seconds)

            mutableListOf<FiatOrderDetailItemEntity>().apply {
                add(
                    FiatOrderDetailItemEntity.Title(
                        R.drawable.svg_ic_order_complete,
                        "Pending for your payment",
                        "Pay within $formattedTime"
                    )
                )
                add(FiatOrderDetailItemEntity.Group("Buy ${data.cryptoCode}"))
                addAll(OrderDetailListComposeUtil.priceInfo(data))
                add(FiatOrderDetailItemEntity.Split)

                val fiatInfo = CurrencyCalcUtil.getFiatInfo(data.fiatCode)
                val fiatPrecision: Int? = fiatInfo?.fiatPrecision?.toInt()
                add(FiatOrderDetailItemEntity.Tips(
                    cost = "Enter the payment amount of ${fiatInfo?.fiatSymbol}${data.amount.scale(fiatPrecision)}",
                    list = data.payMethodInfo?.payMethodValueInfo.orEmpty(),
                    imagePath = mediaPath.orEmpty(),
                    payMethodName = data.payMethodInfo?.payMethodName.orEmpty()
                ))
            }
        }
        return r
    }

    override fun onClickProof() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun requestPaid() {
        lifecycleScope.launch {
            loadingDialog.show(childFragmentManager, LoadingDialogFragment.TAG)
            val r = repo.orderPaid(OrderPaidParam(data.orderId.orEmpty()))
            loadingDialog.dismissAllowingStateLoss()

            activity?.let { a ->
                ToastUtil.show(a, AppManagers.resourceManager.getResponseDigest(r))
                if (r != null && r.isSuccess()) {
                    BuyAndSellActivity.launch(a)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scheduleNextOnTimer()
    }
}