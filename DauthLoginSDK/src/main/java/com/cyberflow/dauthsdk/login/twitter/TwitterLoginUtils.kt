package com.cyberflow.dauthsdk.login.twitter

import android.app.Activity
import android.content.Intent
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import com.twitter.sdk.android.core.identity.TwitterLoginButton
import com.twitter.sdk.android.core.internal.CommonUtils
import java.lang.ref.WeakReference

class TwitterLoginUtils(activity: Activity) {

    private var activityRef: WeakReference<Activity>? = null

    var callback: Callback<TwitterSession>? = null

    @Volatile

    var authClient: TwitterAuthClient? = null

    companion object {

        const val ERROR_MSG_NO_ACTIVITY = ("TwitterLoginButton requires an activity."

                + " Override getActivity to provide the activity for this button.")

    }

    init {

        activityRef = WeakReference(activity)

        TwitterCore.getInstance()

    }

    fun setOnLoginByTwitterClick(callback: Callback<TwitterSession>?) {

        this.callback = callback

        checkCallback(callback)

        checkActivity(activityRef?.get())

        twitterAuthClient!!.authorize(activityRef?.get(), callback)

    }

    private val twitterAuthClient: TwitterAuthClient?

        get() {

            if (authClient == null) {

                synchronized(TwitterLoginButton::class.java) {

                    if (authClient == null) {

                        authClient = TwitterAuthClient()

                    }

                }

            }

            return authClient

        }

    private fun checkCallback(callback: Callback<*>?) {

        if (callback == null) {

            CommonUtils.logOrThrowIllegalStateException(
                TwitterCore.TAG,

                "Callback must not be null, did you call setCallback?")

        }

    }

    private fun checkActivity(activity: Activity?) {

        if (activity == null || activity.isFinishing) {

            CommonUtils.logOrThrowIllegalStateException(TwitterCore.TAG, ERROR_MSG_NO_ACTIVITY)

        }

    }

    /**

     * Call this method when [Activity]

     * is called to complete the authorization flow.

     *

     * @param requestCode the request code used for SSO

     * @param resultCode  the result code returned by the SSO activity

     * @param data        the result data returned by the SSO activity

     */

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == twitterAuthClient!!.requestCode) {

            twitterAuthClient!!.onActivityResult(requestCode, resultCode, data)

        }
    }



}