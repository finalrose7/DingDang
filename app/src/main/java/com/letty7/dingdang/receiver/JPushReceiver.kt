package com.letty7.dingdang.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import cn.jpush.android.api.JPushInterface
import com.letty7.dingdang.checkDingDing
import com.letty7.dingdang.ui.activity.MainActivity
import com.letty7.dingdang.wakeUpAndUnlock


class JPushReceiver : BroadcastReceiver() {

    companion object {
        const val sTAG = "JPushReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        intent?.apply {

            when (this.action) {

                JPushInterface.ACTION_REGISTRATION_ID -> {
                    val i = Intent(MainActivity.ACTION_REGISTER_SUCCEED)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(i)
                }

                JPushInterface.ACTION_NOTIFICATION_RECEIVED -> {

                    wakeUpAndUnlock(context)

                    val ding = checkDingDing(context)
                    if (ding) {
                        val packageManager = context.packageManager
                        val i = packageManager.getLaunchIntentForPackage("com.alibaba.android.rimet")
                        context.startActivity(i)
                    }
                }

                else -> Log.e(sTAG, "极光推送 Unhandled intent - " + this.action)
            }

        }
    }

}