package com.letty7.dingdang

import android.app.Application
import cn.jpush.android.api.JPushInterface
import com.tencent.bugly.Bugly
import kotlin.properties.Delegates

class App : Application() {

    companion object {
        var sContext: App by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
        sContext = this

        Bugly.init(this, BuildConfig.BUGLY_ID, BuildConfig.DEBUG)

        JPushInterface.setDebugMode(BuildConfig.DEBUG)
        JPushInterface.init(this)
        JPushInterface.setLatestNotificationNumber(this, 1)
    }


}