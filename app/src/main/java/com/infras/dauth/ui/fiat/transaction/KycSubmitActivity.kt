package com.infras.dauth.ui.fiat.transaction

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.infras.dauth.R
import com.infras.dauth.app.BaseActivity
import com.infras.dauth.databinding.ActivityKycSubmitBinding
import com.infras.dauth.ext.launch
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauth.manager.AccountManager
import com.infras.dauth.ui.fiat.transaction.fragment.VerifySetIdCardFragment
import com.infras.dauth.ui.fiat.transaction.fragment.VerifySetProfileFragment
import com.infras.dauth.ui.fiat.transaction.fragment.VerifyUploadIdCardFragment
import com.infras.dauth.ui.fiat.transaction.viewmodel.KycSubmitViewModel
import com.infras.dauth.util.ToastUtil
import com.infras.dauth.widget.LoadingDialogFragment
import kotlinx.coroutines.launch

class KycSubmitActivity : BaseActivity(), VerifySetProfileFragment.ProfileConfirmCallback,
    VerifySetIdCardFragment.IdCardConfirmCallback {

    companion object {
        fun launch(context: Context) {
            context.launch(KycSubmitActivity::class.java)
        }
    }

    private var _binding: ActivityKycSubmitBinding? = null
    private val binding get() = _binding!!
    private val viewModel: KycSubmitViewModel by viewModels()
    private val loadingDialog = LoadingDialogFragment.newInstance()
    private val createFragments: List<() -> Fragment> = listOf(
        { VerifySetProfileFragment.newInstance() },
        { VerifySetIdCardFragment.newInstance() },
        { VerifyUploadIdCardFragment.newInstance() },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityKycSubmitBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.ivBack.setDebouncedOnClickListener {
            handleBackPress()
        }
        requestAccountInfo()
    }

    private fun requestAccountInfo() {
        lifecycleScope.launch {
            loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
            val res = AccountManager.sdk.queryAccountByAuthid()
            loadingDialog.dismissAllowingStateLoss()
            if (res != null && res.isSuccess()) {
                val data = res.data
                // make sure phone or email is provided
                if (data?.phone.isNullOrEmpty() && data?.email.isNullOrEmpty()) {
                    layFragment(0)
                } else {
                    layFragment(1)
                }
            } else {
                ToastUtil.show(this@KycSubmitActivity, "get account info failed!")
                finish()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            handleBackPress()
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    private fun handleBackPress() {
        if (supportFragmentManager.backStackEntryCount <= 1) {
            finish()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onConfirmProfile() {
        layFragment(1)
    }

    override fun onConfirmIdCard() {
        layFragment(2)
    }

    private fun layFragment(index: Int) {
        if (index < 0 || index >= createFragments.size) {
            return
        }
        val f = createFragments[index].invoke()
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fl_fragment_container, f, f.javaClass.simpleName)
            if (index > 0) {
                addToBackStack(null)
            }
        }.commit()
    }
}