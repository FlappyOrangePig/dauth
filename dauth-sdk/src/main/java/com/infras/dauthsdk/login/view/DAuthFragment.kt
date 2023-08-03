package com.infras.dauthsdk.login.view

import android.content.Intent
import androidx.fragment.app.Fragment
import com.infras.dauthsdk.login.callback.OnActivityResultListener

class MyFragment : Fragment() {

    private var onActivityResultListener: OnActivityResultListener? = null

    fun setOnActivityResultListener(listener: OnActivityResultListener) {
        onActivityResultListener = listener
    }

    // 在需要触发 onActivityResult() 时调用该方法
    fun triggerOnActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        onActivityResult(requestCode, resultCode, data)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResultListener?.onActivityResult(requestCode, resultCode, data)
    }
}
