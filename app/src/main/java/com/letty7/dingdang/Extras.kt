package com.letty7.dingdang

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.PowerManager

/**
 * 自动亮屏
 */
fun BroadcastReceiver.wakeUpAndUnlock(context: Context) {
    val pm: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val screenOn = pm.isInteractive

    if (!screenOn) {
        val wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright")
        wl.acquire(10000)
    }

    val km: KeyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    val keyguardLock = km.newKeyguardLock("unLock")

    // 屏幕锁定
    keyguardLock.reenableKeyguard()
    // 解锁
    keyguardLock.disableKeyguard()
}

/**
 * 检查设备上是否已安装钉钉
 */
fun BroadcastReceiver.checkDingDing(context: Context): Boolean {

    val packageManager = context.packageManager
    val infos = packageManager.getInstalledPackages(0)

    infos.filter {
        (it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
    }.forEach {
        if (it.packageName == "com.alibaba.android.rimet") {
            return true
        }
    }

    return false
}