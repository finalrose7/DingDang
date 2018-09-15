package com.letty7.dingdang.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class SmsNotificationListenerService : NotificationListenerService() {


    companion object {
        const val sTAG = "SmsListenerService"
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.e(sTAG, "onListenerConnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        Log.e(sTAG, "onNotificationPosted")
    }

}