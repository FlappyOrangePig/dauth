package com.cyberflow.dauthsdk.twitter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cyberflow.dauthsdk.google.GoogleLoginManager
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import com.twitter.sdk.android.core.identity.TwitterLoginButton
import com.twitter.sdk.android.core.internal.CommonUtils
import java.lang.ref.WeakReference

private const val CONSUMER_KEY = "yrIpcNpxNZ1z0gPZfrgY6gMJN"
private const val CONSUMER_SECRET = "r7n64NTvfYMIvyEZfeFtrWSExhuUZrxCW5IyhNVKnxgid4EKuR"
private const val ERROR_MSG_NO_ACTIVITY = ("TwitterLoginButton requires an activity."
        + " Override getActivity to provide the activity for this button.")

class TwitterLoginManager(activity: Activity) {

    private var activityRef: WeakReference<Activity>? = null

    var callback: Callback<TwitterSession>? = null

    @Volatile

    var authClient: TwitterAuthClient? = null

    companion object {

        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            TwitterLoginManager(activity = Activity())
        }

    }

    init {

        activityRef = WeakReference(activity)

    }

    fun initTwitterSDK(context: Context) {
        val config = TwitterConfig.Builder(context)
            .logger(DefaultLogger(Log.DEBUG))
            .twitterAuthConfig(TwitterAuthConfig(CONSUMER_KEY, CONSUMER_SECRET))
            .debug(true)
            .build()
        Twitter.initialize(config)

    }


    fun twitterLoginAuth(callback: Callback<TwitterSession>?) {

        this.callback = callback

        checkCallback(callback)

        checkActivity(activityRef?.get())

        twitterAuthClient?.authorize(activityRef?.get(), callback)

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

            CommonUtils.logOrThrowIllegalStateException(TwitterCore.TAG,

                "Callback must not be null, did you call setCallback?")
        }

    }

    private fun checkActivity(activity: Activity?) {

        if (activity == null || activity.isFinishing) {

            CommonUtils.logOrThrowIllegalStateException(TwitterCore.TAG, ERROR_MSG_NO_ACTIVITY)

        }

    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == twitterAuthClient?.requestCode) {
            twitterAuthClient!!.onActivityResult(requestCode, resultCode, data)
        }
    }

}