package com.infras.dauth.ui.transaction

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
import com.infras.dauth.ui.transaction.fragment.VerifySetIdCardFragment
import com.infras.dauth.ui.transaction.fragment.VerifySetProfileFragment
import com.infras.dauth.ui.transaction.fragment.VerifyUploadIdCardFragment

class KycSubmitActivity : BaseActivity(), VerifySetProfileFragment.ProfileConfirmCallback,
    VerifySetIdCardFragment.IdCardConfirmCallback {

    companion object {
        private const val FRAGMENT_TAG = "FRAGMENT_TAG"
        fun launch(context: Context) {
            context.launch(KycSubmitActivity::class.java)
        }
    }

    private var _binding: ActivityKycSubmitBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityKycSubmitBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.ivBack.setDebouncedOnClickListener {
            finish()
        }
        replaceFragment(VerifySetProfileFragment.newInstance())
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val r = super.onKeyDown(keyCode, event)
        if (keyCode == KeyEvent.KEYCODE_BACK && supportFragmentManager.backStackEntryCount <= 1) {
            finish()
            return true
        }
        return r
    }

    override fun onConfirmProfile() {
        replaceFragment(VerifySetIdCardFragment.newInstance())
    }

    override fun onConfirmIdCard() {
        replaceFragment(VerifyUploadIdCardFragment.newInstance())
    }

    private fun replaceFragment(f: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_fragment_container, f, FRAGMENT_TAG)
            addToBackStack(null)
        }.commit()
    }
}