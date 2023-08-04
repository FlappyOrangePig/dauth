package com.infras.dauthsdk.wallet.ext

import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.ApplicationInfoFlags
import android.os.Build
import com.infras.dauthsdk.api.DAuthSDK

internal fun safeApp() = DAuthSDK.impl.context.applicationContext as? Application
@Deprecated("使用依赖注入方便单元测试", ReplaceWith("Managers"))
internal fun app() = safeApp()!!

internal fun Context.applicationInfo() =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getApplicationInfo(
            packageName,
            ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
        )
    } else {
        packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
    }

internal fun Context.getMetaData(key: String): String {
    val metaData = this.applicationInfo().metaData
    return metaData.getString(key).orEmpty()
}

internal fun Context.getPackageInfo(): PackageInfo? {
    return runCatchingWithLog {
        val packageInfo = this.packageManager.getPackageInfo(packageName, 0)
        packageInfo
    }
}

internal fun PackageInfo.getVersionCode() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    this.longVersionCode
} else {
    this.versionCode
}

internal fun Float.dp(): Int {
    val context = app()
    val scale = context.applicationContext.resources.displayMetrics.density
    return (this * scale + 0.5f).toInt()
}

internal fun Int.dp() = this.toFloat().dp()