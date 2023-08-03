package com.infras.dauth.util

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.widget.EditText

object DialogHelper {

    private const val TITLE = "PROMPT"
    private const val YES = "YES"
    private const val NO = "NO"

    fun show2ButtonsDialogMayHaveLeak(
        activity: Activity,
        message: String,
        yesBlock: () -> Unit
    ) {
        AlertDialog.Builder(activity)
            .setTitle(TITLE)
            .setMessage(message)
            .setPositiveButton(YES) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
                yesBlock.invoke()
            }
            .setNegativeButton(NO) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create().show()
    }

    fun show1ButtonDialogMayHaveLeak(
        activity: Activity,
        message: String
    ) {
        AlertDialog.Builder(activity)
            .setTitle(TITLE)
            .setMessage(message)
            .setPositiveButton(YES) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create().show()
    }

    fun showInputDialogMayHaveLeak(
        activity: Activity,
        title: String = TITLE,
        defaultValue: String? = null,
        onOk: (String) -> Unit
    ) {
        val inputEditText = EditText(activity)
        defaultValue?.let {
            inputEditText.setText(it)
        }
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle(title)
            .setView(inputEditText)
            .setPositiveButton(YES) { _, _ ->
                onOk.invoke(inputEditText.text?.toString() ?: "")
            }
            .setNegativeButton(NO, null)
            .create()
        dialog.show()
    }
}