package com.letty7.dingdang.receiver

import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import cn.jpush.android.api.JPushMessage
import cn.jpush.android.service.JPushMessageReceiver
import com.letty7.dingdang.ui.activity.MainActivity

class InitJPushReceiver : JPushMessageReceiver() {

    companion object {
        const val sTAG = "InitJPushReceiver"
    }

    override fun onCheckTagOperatorResult(context: Context?, jPushMessag: JPushMessage?) {
        super.onCheckTagOperatorResult(context, jPushMessag)
        Log.e(sTAG, "onCheckTagOperatorResult -> " + jPushMessag.toString())
    }

    override fun onTagOperatorResult(context: Context, jPushMessag: JPushMessage?) {
        super.onTagOperatorResult(context, jPushMessag)
        Log.e(sTAG, "onTagOperatorResult -> " + jPushMessag.toString())

        jPushMessag?.apply {
            val code = this.errorCode
            val intent = Intent(MainActivity.ACTION_SYNC_STATE)
            intent.putExtra(MainActivity.INIT_CODE, code)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }

    override fun onMobileNumberOperatorResult(context: Context?, p1: JPushMessage?) {
        super.onMobileNumberOperatorResult(context, p1)
        Log.e(sTAG, "onMobileNumberOperatorResult -> " + p1.toString())
    }

    override fun onAliasOperatorResult(context: Context?, p1: JPushMessage?) {
        super.onAliasOperatorResult(context, p1)
        Log.e(sTAG, "onAliasOperatorResult -> " + p1.toString())
    }

}