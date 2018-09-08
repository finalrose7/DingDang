package com.letty7.dingdang.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.telephony.TelephonyManager
import android.util.Log
import com.android.internal.telephony.ITelephony
import com.letty7.dingdang.checkDingDing
import com.letty7.dingdang.ui.fragment.PrefsFragment
import com.letty7.dingdang.wakeUpAndUnlock

class PhoneReceiver : BroadcastReceiver() {

    companion object {
        const val sTAG = "PhoneReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {

        val action = intent.action

        if (Intent.ACTION_NEW_OUTGOING_CALL != action) {

            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            Log.e(sTAG, "state: $state")
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            Log.e(sTAG, "incomingNumber: $incomingNumber")

            val spf = PreferenceManager.getDefaultSharedPreferences(context)

            val phoneStyle = spf.getBoolean(PrefsFragment.KEY_PHONE_SWITCH, false)
            val phoneResponse = spf.getString(PrefsFragment.KEY_PHONE_RESPONSE_LIST, "0")
            val phoneNumber = spf.getString(PrefsFragment.KEY_PHONE_NUMBER, "")

            Log.e(sTAG, "phoneStyle : $phoneStyle \n phoneResponse : $phoneResponse \n phoneNumber : $phoneNumber")

            if (state == TelephonyManager.EXTRA_STATE_RINGING
                    && phoneStyle
                    && phoneResponse == "0"
                    && phoneNumber == incomingNumber) {

                try {
                    // End call.
                    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    val method = TelephonyManager::class.java.getDeclaredMethod("getITelephony")
                    method.isAccessible = true
                    val telephony: ITelephony = method.invoke(telephonyManager) as ITelephony
                    telephony.endCall()

                    wakeUpAndUnlock(context)

                    val ding = checkDingDing(context)

                    if (ding) {
                        val packageManager = context.packageManager
                        val i = packageManager.getLaunchIntentForPackage("com.alibaba.android.rimet")
                        context.startActivity(i)
                    }

                } catch (e: Exception) {
                    Log.e(sTAG, "End call fail.", e)
                }
            }
        }
    }

}