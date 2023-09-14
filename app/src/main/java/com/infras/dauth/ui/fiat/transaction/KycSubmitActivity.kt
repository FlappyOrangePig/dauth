package com.infras.dauth.ui.fiat.transaction

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.infras.dauth.R
import com.infras.dauth.app.BaseActivity
import com.infras.dauth.databinding.ActivityKycSubmitBinding
import com.infras.dauth.ext.launch
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauth.ui.fiat.transaction.fragment.VerifySetIdCardFragment
import com.infras.dauth.ui.fiat.transaction.fragment.VerifySetProfileFragment
import com.infras.dauth.ui.fiat.transaction.fragment.VerifyUploadIdCardFragment

class KycSubmitActivity : BaseActivity(), VerifySetProfileFragment.ProfileConfirmCallback,
    VerifySetIdCardFragment.IdCardConfirmCallback {

    companion object {
        private const val EXTRA_IS_BOUND = "EXTRA_IS_BOUND"
        fun launch(context: Context, isBound: Boolean) {
            context.launch(KycSubmitActivity::class.java) {
                it.putExtra(EXTRA_IS_BOUND, isBound)
            }
        }
    }

    private var _binding: ActivityKycSubmitBinding? = null
    private val binding get() = _binding!!
    private val createFragments: List<() -> Fragment> = listOf(
        { VerifySetProfileFragment.newInstance() },
        { VerifySetIdCardFragment.newInstance() },
        { VerifyUploadIdCardFragment.newInstance() },
    )
    private val isBound get() = intent.getBooleanExtra(EXTRA_IS_BOUND, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityKycSubmitBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.ivBack.setDebouncedOnClickListener {
            handleBackPress()
        }
        if (isBound) {
            layFragment(1)
        } else {
            layFragment(0)
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