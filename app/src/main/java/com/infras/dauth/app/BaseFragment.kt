package com.infras.dauth.app

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.infras.dauth.util.ToastUtil
import com.infras.dauth.widget.LoadingDialogFragment
import kotlinx.coroutines.launch

open class BaseFragment : Fragment() {
    protected val logTag: String = this::class.java.simpleName
    private val loadingDialog = LoadingDialogFragment.newInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDefaultViewModel()
    }

    open fun getDefaultViewModel(): BaseViewModel? = null

    private fun initDefaultViewModel() {
        val vm = getDefaultViewModel() ?: return
        lifecycleScope.launch {
            vm.toastEvent.collect {
                activity?.let { a-> ToastUtil.show(a, it) }
            }
        }
        vm.showLoading.observe(viewLifecycleOwner) {
            if (it) {
                loadingDialog.show(childFragmentManager, LoadingDialogFragment.TAG)
            } else {
                loadingDialog.dismissAllowingStateLoss()
            }
        }
    }
}