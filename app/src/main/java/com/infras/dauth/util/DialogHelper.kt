package com.infras.dauth.util

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.widget.EditText
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
        editTextHolder: (() -> EditText)? = null,
        onOk: (String) -> Unit
    ) {
        val inputEditText = editTextHolder?.invoke() ?: EditText(activity)
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
            .setCancelable(false)
            .create()
        dialog.show()
    }

    suspend fun suspendShow2ButtonsDialogMayHaveLeak(
        activity: Activity,
        message: String,
    ): Boolean {
        return suspendCoroutine { continuation ->
            AlertDialog.Builder(activity)
                .setTitle(TITLE)
                .setMessage(message)
                .setPositiveButton(YES) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    continuation.resume(true)
                }
                .setNegativeButton(NO) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    continuation.resume(false)
                }
                .setCancelable(false)
                .create().show()
        }
    }

    suspend fun suspendShowInputDialogMayHaveLeak(
        activity: Activity,
        title: String = TITLE,
        defaultValue: String? = null,
        editTextHolder: (() -> EditText)? = null,
    ): String? {
        return suspendCoroutine { continuation ->
            val inputEditText = editTextHolder?.invoke() ?: EditText(activity)
            defaultValue?.let {
                inputEditText.setText(it)
            }
            val dialog = androidx.appcompat.app.AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(inputEditText)
                .setPositiveButton(YES) { _, _ ->
                    continuation.resume(inputEditText.text?.toString() ?: "")
                }
                .setNegativeButton(NO) { _, _ ->
                    continuation.resume(null)
                }
                .setCancelable(false)
                .create()
            dialog.show()
        }
    }
}