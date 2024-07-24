package com.dluvian.voyage.data.preferences

import android.content.Context

private const val SEND_AUTH = "send_auth"
private const val LOCAL_RELAY_PORT = "local_relay_port"

private const val DEFAULT_LOCAL_RELAY_PORT = 8888

class RelayPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(DATABASE_FILE, Context.MODE_PRIVATE)

    fun getSendAuth(): Boolean {
        return preferences.getBoolean(SEND_AUTH, false)
    }

    fun setSendAuth(sendAuth: Boolean) {
        preferences.edit()
            .putBoolean(SEND_AUTH, sendAuth)
            .apply()
    }

    fun getLocalRelayPort(): Int? {
        val port = preferences.getInt(LOCAL_RELAY_PORT, DEFAULT_LOCAL_RELAY_PORT)
        return if (port >= 0) port else null
    }

    fun setLocalRelayPort(port: Int?) {
        preferences.edit()
            .putInt(LOCAL_RELAY_PORT, port ?: -1)
            .apply()
    }
}
