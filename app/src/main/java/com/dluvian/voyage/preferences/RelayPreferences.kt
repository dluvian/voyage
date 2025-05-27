package com.dluvian.voyage.preferences

import android.content.Context

private const val SEND_AUTH = "send_auth"

class RelayPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(RELAY_FILE, Context.MODE_PRIVATE)

    fun getSendAuth(): Boolean {
        return preferences.getBoolean(SEND_AUTH, false)
    }

    fun setSendAuth(sendAuth: Boolean) {
        preferences.edit()
            .putBoolean(SEND_AUTH, sendAuth)
            .apply()
    }
}
