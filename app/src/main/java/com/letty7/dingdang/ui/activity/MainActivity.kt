package com.letty7.dingdang.ui.activity

import android.app.KeyguardManager
import android.content.*
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.os.PowerManager
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.android.internal.telephony.ITelephony
import com.letty7.dingdang.R
import com.letty7.dingdang.ui.fragment.PrefsFragment
import com.tencent.bugly.beta.Beta
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var telephonyManager: TelephonyManager
    private lateinit var listener: PhoneStateListener
    private lateinit var spf: SharedPreferences

    private lateinit var localBroadcastManager: LocalBroadcastManager

    companion object {
        const val sTAG = "MainActivity"
        const val ACTION_SYNC_STATE = "com.letty7.dingdang.ACTION_SYNC_STATE"
        const val ACTION_REGISTER_SUCCEED = "com.letty7.dingdang.ACTION_REGISTER_SUCCEED"

        const val INIT_CODE = "init_code"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        spf = PreferenceManager.getDefaultSharedPreferences(this)

        localBroadcastManager = LocalBroadcastManager.getInstance(this)

        val intentFilter = IntentFilter(ACTION_SYNC_STATE)
        localBroadcastManager.registerReceiver(broadcastReceiver, intentFilter)

        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        listener = PhoneCallListener()

        telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)

        fragmentManager.beginTransaction()
                .add(R.id.frame_layout, PrefsFragment())
                .commitAllowingStateLoss()
    }


    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            intent?.apply {
                if (ACTION_SYNC_STATE == action) {
                    val code = getIntExtra(INIT_CODE, -1)
                    when (code) {
                        0 -> showSnackbar(getString(R.string.sync_succeed))
                        6002, 6014 -> syncSnackbar(getString(R.string.sync_failure_network) + " errorCode $code")
                        else -> syncSnackbar(getString(R.string.sync_failure) + " errorCode $code")
                    }
                } else if (ACTION_REGISTER_SUCCEED == action) {
                    showSnackbar(getString(R.string.push_init_succeed))
                }
            }
        }
    }

    inner class PhoneCallListener : PhoneStateListener() {

        override fun onCallStateChanged(state: Int, incomingNumber: String?) {
            super.onCallStateChanged(state, incomingNumber)

            Log.e(sTAG, incomingNumber)

            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {

                    val phoneStyle = spf.getBoolean(PrefsFragment.KEY_PHONE_SWITCH, false)
                    val phoneResponse = spf.getString(PrefsFragment.KEY_PHONE_RESPONSE_LIST, "0")
                    val phoneNumber = spf.getString(PrefsFragment.KEY_PHONE_NUMBER, "")

                    Log.e(sTAG, "phoneStyle : $phoneStyle \n phoneResponse : $phoneResponse \n phoneNumber : $phoneNumber")

                    if (phoneStyle && phoneResponse == "1" && incomingNumber == phoneNumber) {

                        try {

                            val method = TelephonyManager::class.java.getDeclaredMethod("getITelephony")
                            method.isAccessible = true
                            val telephony: ITelephony = method.invoke(telephonyManager) as ITelephony
                            telephony.endCall()

                            val ding = checkDingDing(this@MainActivity)

                            if (ding) {
                                wakeUpAndUnlock(this@MainActivity)

                                val packageManager = this@MainActivity.packageManager
                                val i = packageManager.getLaunchIntentForPackage("com.alibaba.android.rimet")
                                this@MainActivity.startActivity(i)
                            }

                        } catch (e: Exception) {
                            Log.e(sTAG, "End call fail.", e)
                        }

                    }
                }
            }
        }
    }

    fun showSnackbar(text: String) {
        Snackbar.make(container, text, Snackbar.LENGTH_SHORT).show()
    }

    fun syncSnackbar(text: String) {
        Snackbar.make(container, text, Snackbar.LENGTH_INDEFINITE).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        localBroadcastManager.unregisterReceiver(broadcastReceiver)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_update -> {
                Beta.checkUpgrade(true, false)
                true
            }
            R.id.action_exit -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

}
