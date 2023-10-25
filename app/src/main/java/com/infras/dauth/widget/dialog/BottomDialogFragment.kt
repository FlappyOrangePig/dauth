package com.infras.dauth.widget.dialog

import android.view.Gravity
import android.view.ViewGroup

open class BottomDialogFragment : BaseDialogFragment() {

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(null)
            setGravity(Gravity.BOTTOM)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }
}