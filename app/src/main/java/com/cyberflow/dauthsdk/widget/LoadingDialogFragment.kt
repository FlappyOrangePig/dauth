package com.cyberflow.dauthsdk.widget

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

class LoadingDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity).let {
            it.setMessage("Loading...")
            it.create()
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        dialog?.requestWindowFeature(STYLE_NO_TITLE);
        isCancelable = false
        super.show(manager, tag)
    }

    companion object {
        const val TAG = "LoadingDialogFragment"
        fun newInstance(): LoadingDialogFragment {
            return LoadingDialogFragment()
        }
    }
}