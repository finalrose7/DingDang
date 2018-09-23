package com.letty7.dingdang.ui.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.preference.*
import android.provider.Settings
import android.support.v4.app.NotificationManagerCompat
import android.text.TextUtils
import cn.jpush.android.api.JPushInterface
import com.letty7.dingdang.App
import com.letty7.dingdang.R
import com.letty7.dingdang.UserPreferences
import com.letty7.dingdang.service.SmsNotificationListenerService
import com.letty7.dingdang.ui.activity.AboutActivity
import com.letty7.dingdang.ui.activity.MainActivity


class PrefsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var pushSwitch: SwitchPreference
    private lateinit var pushTimeList: ListPreference

    private lateinit var phoneResponseList: ListPreference
    private lateinit var phoneNumber: EditTextPreference

    private lateinit var smsState: Preference
    private lateinit var smsNumber: EditTextPreference

    private lateinit var aboutDingDang: Preference

    private lateinit var spf: SharedPreferences
    private var state: Boolean = false

    private lateinit var tlValues: Array<String>
    private lateinit var prValues: Array<String>

    private var mainActivity: MainActivity? = null

    companion object {

        const val sTAG = "PrefsFragment"

        const val KEY_PUSH_SWITCH = "push_switch"
        const val KEY_PUSH_TIME_LIST = "push_time_list"

        const val KEY_PHONE_SWITCH = "phone_switch"
        const val KEY_PHONE_RESPONSE_LIST = "phone_response_list"
        const val KEY_PHONE_NUMBER = "phone_number"

        const val KEY_SMS_SWITCH = "sms_switch"
        const val KEY_SMS_STATE = "sms_state"
        const val KEY_SMS_NUMBER = "sms_number"

        const val KEY_ABOUT_DING_DANG = "about_dingdang"

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        context?.apply {
            mainActivity = this as MainActivity
        }
    }

    override fun onDetach() {
        super.onDetach()
        mainActivity?.apply {
            mainActivity = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)

        spf = PreferenceManager.getDefaultSharedPreferences(activity)
        tlValues = resources.getStringArray(R.array.time_list_entries)
        prValues = resources.getStringArray(R.array.phone_list_entries)

        pushSwitch = findPreference(KEY_PUSH_SWITCH) as SwitchPreference
        pushTimeList = findPreference(KEY_PUSH_TIME_LIST) as ListPreference

        phoneResponseList = findPreference(KEY_PHONE_RESPONSE_LIST) as ListPreference
        phoneNumber = findPreference(KEY_PHONE_NUMBER) as EditTextPreference

        smsState = findPreference(KEY_SMS_STATE)
        smsNumber = findPreference(KEY_SMS_NUMBER) as EditTextPreference

        aboutDingDang = findPreference(KEY_ABOUT_DING_DANG)

        initState()

        if (UserPreferences.isFirstInstall()) {
            val values = spf.getString(KEY_PUSH_TIME_LIST, "1")
            val sets = mutableSetOf(values)
            JPushInterface.setTags(activity, 100, sets)
            mainActivity?.syncSnackbar(getString(R.string.sync))
            UserPreferences.setFirstInstall(false)
        }

    }

    private fun initState() {

        state = JPushInterface.isPushStopped(App.sContext)
        pushSwitch.isChecked = !state

        val timeListValues = spf.getString(KEY_PUSH_TIME_LIST, "1")
        pushTimeList.summary = tlValues[timeListValues.toInt()]

        val phoneResponseValues = spf.getString(KEY_PHONE_RESPONSE_LIST, "0")
        phoneResponseList.summary = prValues[phoneResponseValues.toInt()]

        val phoneNumberValues = spf.getString(KEY_PHONE_NUMBER, "")
        phoneNumber.summary =
                if (TextUtils.isEmpty(phoneNumberValues))
                    getString(R.string.c_not_set)
                else
                    phoneNumberValues

        val smsNumberValues = spf.getString(KEY_SMS_NUMBER, "")
        smsNumber.summary =
                if (TextUtils.isEmpty(smsNumberValues))
                    getString(R.string.c_not_set)
                else
                    smsNumberValues

        val packageInfo = getPackageInfo(activity)
        packageInfo?.apply {
            aboutDingDang.title = getString(R.string.app_name) + " " + versionName
        }

    }

    override fun onResume() {
        super.onResume()

        smsState.summary = if (isNotificationListenerServiceEnabled(App.sContext))
            getString(R.string.sms_state_running)
        else
            getString(R.string.sms_state_stop)

        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }


    override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen?,
                                       preference: Preference?): Boolean {

        when (preference?.key) {
            KEY_ABOUT_DING_DANG -> {
                startActivity(Intent(activity, AboutActivity::class.java))
            }

            KEY_SMS_STATE -> {

                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                else
                    Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")

                startActivity(intent)
                toggleNotificationListenerService(App.sContext)
            }
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference)
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {

            KEY_PUSH_SWITCH -> {
                val values = sharedPreferences.getBoolean(KEY_PUSH_SWITCH, false)
                if (values)
                    JPushInterface.resumePush(App.sContext)
                else
                    JPushInterface.stopPush(App.sContext)
            }

            KEY_PUSH_TIME_LIST -> {

                val values = sharedPreferences.getString(KEY_PUSH_TIME_LIST, "1")
                pushTimeList.summary = tlValues[values.toInt()]

                state = JPushInterface.isPushStopped(App.sContext)

                if (!state) {
                    val sets = mutableSetOf(values)
                    JPushInterface.setTags(activity, 100, sets)
                    mainActivity?.syncSnackbar(getString(R.string.sync))
                } else {
                    mainActivity?.showSnackbar(getString(R.string.push_close_tips))
                }

            }

            KEY_PHONE_RESPONSE_LIST -> {
                val values = sharedPreferences.getString(KEY_PHONE_RESPONSE_LIST, "0")
                phoneResponseList.summary = prValues[values.toInt()]
            }

            KEY_PHONE_NUMBER -> {
                val values = sharedPreferences.getString(KEY_PHONE_NUMBER, "")
                phoneNumber.summary =
                        if (TextUtils.isEmpty(values))
                            getString(R.string.c_not_set)
                        else
                            values
            }

            KEY_SMS_NUMBER -> {
                val values = sharedPreferences.getString(KEY_SMS_NUMBER, "")
                smsNumber.summary =
                        if (TextUtils.isEmpty(values))
                            getString(R.string.c_not_set)
                        else
                            values
            }

        }
    }

    private fun toggleNotificationListenerService(context: Context) {
        val pm = context.packageManager
        pm.setComponentEnabledSetting(ComponentName(context, SmsNotificationListenerService::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        pm.setComponentEnabledSetting(ComponentName(context, SmsNotificationListenerService::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
    }

    private fun isNotificationListenerServiceEnabled(context: Context): Boolean {
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(context)
        return packageNames.contains(context.packageName)
    }

    private fun getPackageInfo(context: Context): PackageInfo? {
        return try {
            val pm: PackageManager = context.packageManager
            pm.getPackageInfo(context.packageName, PackageManager.GET_CONFIGURATIONS)
        } catch (e: Exception) {
            null
        }
    }
}