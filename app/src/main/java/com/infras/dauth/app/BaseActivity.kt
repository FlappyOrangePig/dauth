package com.infras.dauth.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.infras.dauth.util.SystemUIUtil
import com.infras.dauth.util.ToastUtil
import com.infras.dauth.widget.LoadingDialogFragment
import kotlinx.coroutines.launch

open class BaseActivity : AppCompatActivity() {

    protected val logTag: String = this::class.java.simpleName
    private val loadingDialog = LoadingDialogFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showSystemUI()
        initDefaultViewModel()
    }

    open fun showSystemUI() {
        SystemUIUtil.show(window, SystemUIUtil.ThemeDrawByDeveloper())
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            dispatchRelease()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dispatchRelease()
    }

    private var dispatched = false

    private fun dispatchRelease() {
        if (!dispatched) {
            dispatched = true
            onRelease()
        }
    }

    open fun onRelease() = Unit

    open fun getDefaultViewModel(): BaseViewModel? = null

    private fun initDefaultViewModel() {
        val vm = getDefaultViewModel() ?: return
        lifecycleScope.launch {
            vm.toastEvent.collect {
                ToastUtil.show(this@BaseActivity, it)
            }
        }
        vm.showLoading.observe(this) {
            if (it) {
                loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
            } else {
                loadingDialog.dismissAllowingStateLoss()
            }
        }
    }
}