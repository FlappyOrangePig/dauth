package com.infras.dauth.widget

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

private fun Float.dp(context: Context): Int {
    val scale = context.applicationContext.resources.displayMetrics.density
    return (this * scale + 0.5f).toInt()
}

class LoadingDialogFragment : DialogFragment() {
    companion object {
        const val TAG = "LoadingDialogFragment"
        fun newInstance(): LoadingDialogFragment {
            return LoadingDialogFragment()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val a = requireActivity()
        val fl = FrameLayout(a).apply {
            setBackgroundColor(Color.parseColor("#88000000"))
            val padding = 10F.dp(a)
            setPadding(padding, padding, padding, padding)
            addView(ProgressBar(a), ViewGroup.LayoutParams(-2, -2))
        }

        val fl2 = FrameLayout(a)
        fl2.addView(fl, FrameLayout.LayoutParams(-2, -2).also { it.gravity = Gravity.CENTER })
        val builder = AlertDialog.Builder(a)
        builder.setView(fl2)

        val dialog = builder.create()

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

    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, tag)
        dialog?.requestWindowFeature(STYLE_NO_TITLE);
        val window = dialog?.window
        if (window != null) {
            val layoutParams = window.attributes
            layoutParams.gravity = Gravity.CENTER
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.dimAmount = 0.6f
            window.decorView.setPadding(0, 0, 0, 0)
            window.decorView.setBackgroundColor(Color.TRANSPARENT)
            window.attributes = layoutParams
            window.setBackgroundDrawable(null)
        }
        isCancelable = false
    }
}