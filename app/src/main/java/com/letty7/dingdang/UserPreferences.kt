package com.letty7.dingdang

import android.content.Context
import android.content.SharedPreferences

object UserPreferences {

    private const val USER_PREFERENCE = "user_preferences"

    private const val FIRST_INSTALL = "first_install"

    fun setFirstInstall(state: Boolean) {
        val editor = getSharedPreferences().edit()
        editor.putBoolean(FIRST_INSTALL, state)
        editor.apply()
    }

    fun isFirstInstall(): Boolean {
        return getSharedPreferences().getBoolean(FIRST_INSTALL, true)
    }

    private fun getSharedPreferences(): SharedPreferences {
        return App.sContext.getSharedPreferences(USER_PREFERENCE, Context.MODE_PRIVATE)
    }
}