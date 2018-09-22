package com.letty7.dingdang.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.graphics.BitmapFactory
import android.os.Build
import android.os.PowerManager
import android.preference.PreferenceManager
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.support.v4.content.ContextCompat
import com.letty7.dingdang.App
import com.letty7.dingdang.R
import com.letty7.dingdang.ui.fragment.PrefsFragment


class SmsNotificationListenerService : NotificationListenerService() {

    private lateinit var spf: SharedPreferences
    private lateinit var notificationManager: NotificationManager

    companion object {
        const val sTAG = "SmsListenerService"
        const val notifyId = 100
    }

    override fun onCreate() {
        super.onCreate()
        spf = PreferenceManager.getDefaultSharedPreferences(App.sContext)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        val notification = buildNotification()
        startForeground(notifyId, notification)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        stopForeground(true)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?, rankingMap: RankingMap?) {
        super.onNotificationPosted(sbn, rankingMap)

        sbn?.apply {

            val notification = notification

            val smsPhone = spf.getString(PrefsFragment.KEY_SMS_NUMBER, "")
            val smsEnable = spf.getBoolean(PrefsFragment.KEY_SMS_SWITCH, false)

            val tickerText = notification.tickerText

            if (tickerText != null) {

                val content = tickerText.toString()

                if (content.contains(smsPhone) && smsEnable) {
                    val context = App.sContext
                    wakeUpAndUnlock(context)
                    if (checkDingDing(context)) {
                        val packageManager = context.packageManager
                        val i = packageManager.getLaunchIntentForPackage("com.alibaba.android.rimet")
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(i)
                    }
                }
            }
        }
    }

    private fun buildNotification(): Notification {

        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        else
            Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")

        val pendingIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT)

        val builder = Notification.Builder(this)
                .setContentTitle(getString(R.string.notify_title))
                .setContentText(getString(R.string.notify_content))
                .setWhen(System.currentTimeMillis())
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(ContextCompat.getColor(App.sContext, R.color.colorAccent))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(R.string.notify_channel_name)
            val channelId = "ding_dang_channel_id"
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = getString(R.string.notify_channel_description)
            notificationManager.createNotificationChannel(channel)
            builder.setChannelId(channelId)
        }

        builder.setOnlyAlertOnce(true)

        return builder.build()
    }


    private fun checkDingDing(context: Context): Boolean {

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

    private fun wakeUpAndUnlock(context: Context) {
        val pm: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val screenOn = pm.isInteractive

        if (!screenOn) {
            val wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright")
            wl.acquire(10000)
        }

        val km: KeyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val keyguardLock = km.newKeyguardLock("unLock")

        keyguardLock.reenableKeyguard()

        keyguardLock.disableKeyguard()
    }
}