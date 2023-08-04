package com.infras.dauthsdk.wallet.util

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

object SystemUIUtil {

    fun hideActionBarAndTitleBar(a: AppCompatActivity) {
        a.requestWindowFeature(Window.FEATURE_NO_TITLE)
        a.supportActionBar?.hide()
    }

    fun show(w: Window, mode: Mode) {
        var systemUiVisibility = 0
        when (mode) {
            is ThemeOnlySetColor -> {
                if (mode.translucentStatus) {
                    w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    w.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                } else {
                    w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                }
            }
            is ThemeDrawByDeveloper -> {
                w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                w.statusBarColor = mode.statusBarColor
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    systemUiVisibility = if (mode.lightStatusMode) {
                        systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    } else {
                        systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                    }
                }
            }
        }

        systemUiVisibility = if (mode.extendLayoutToStatusBar) {
            systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            systemUiVisibility and View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN.inv()
        }

        w.decorView.systemUiVisibility = systemUiVisibility
    }

    sealed class Mode(val extendLayoutToStatusBar: Boolean)

    @Deprecated("包含较多废弃的API")
    class ThemeOnlySetColor(
        extendLayoutToStatusBar: Boolean = false,
        val translucentStatus: Boolean = false
    ) : Mode(extendLayoutToStatusBar)

    class ThemeDrawByDeveloper(
        extendLayoutToStatusBar: Boolean = false,
        val lightStatusMode: Boolean = false,
        val statusBarColor: Int = Color.TRANSPARENT
    ) : Mode(extendLayoutToStatusBar)
}