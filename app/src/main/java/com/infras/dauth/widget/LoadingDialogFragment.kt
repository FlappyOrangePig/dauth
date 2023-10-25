package com.infras.dauth.widget

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.fragment.app.FragmentManager
import com.infras.dauth.ext.dp
import com.infras.dauth.util.LogUtil
import com.infras.dauth.widget.dialog.BaseDialogFragment

class LoadingDialogFragment : BaseDialogFragment() {
    companion object {
        const val TAG = "LoadingDialogFragment"
        fun newInstance(): LoadingDialogFragment {
            return LoadingDialogFragment()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val a = requireActivity()
        val fl = FrameLayout(a).apply {
            //setBackgroundColor(Color.parseColor("#88000000"))
            val padding = 10F.dp()
            setPadding(padding, padding, padding, padding)
            addView(ProgressBar(a), ViewGroup.LayoutParams(-2, -2))
        }

        val fl2 = FrameLayout(a)
        fl2.addView(fl, FrameLayout.LayoutParams(-2, -2).also { it.gravity = Gravity.CENTER })
        val builder = AlertDialog.Builder(a)
        builder.setView(fl2)

        val dialog = builder.create()
        dialog?.requestWindowFeature(STYLE_NO_TITLE)
        // 去边框
        dialog.window?.apply {
            val layoutParams = attributes
            layoutParams.gravity = Gravity.CENTER
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.dimAmount = 0.6f
            decorView.setPadding(0, 0, 0, 0)
            decorView.setBackgroundColor(Color.TRANSPARENT)
            attributes = layoutParams
            setBackgroundDrawable(null)
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val layoutParams = window.attributes
            layoutParams.gravity = Gravity.CENTER
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.dimAmount = 0.2f
            window.decorView.setPadding(0, 0, 0, 0)
            window.decorView.setBackgroundColor(Color.TRANSPARENT)
            window.attributes = layoutParams
            window.setBackgroundDrawable(null)
        }
        isCancelable = false
    }

    override fun dismissAllowingStateLoss() {
        try {
            super.dismissAllowingStateLoss()
        } catch (t: Throwable) {
            LogUtil.d(TAG, t.stackTraceToString())
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            if (!isAdded) {
                super.show(manager, tag)
            }
        } catch (t: Throwable) {
            LogUtil.d(TAG, t.stackTraceToString())
        }
    }
}