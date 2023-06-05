package com.cyberflow.dauthsdk.login.callback

import android.content.Intent

interface OnActivityResultListener {
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}
