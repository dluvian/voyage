package com.dluvian.voyage.data.preferences

import android.content.Context
import com.dluvian.voyage.core.DEFAULT_AUTOPILOT_RELAYS
import com.dluvian.voyage.core.MAX_AUTOPILOT_RELAYS
import com.dluvian.voyage.core.MIN_AUTOPILOT_RELAYS

private const val SEND_AUTH = "send_auth"
private const val SEND_BOOKMARKED_TO_LOCAL_RELAY = "send_bookmarked_to_local_relay"
private const val SEND_UPVOTED_TO_LOCAL_RELAY = "send_upvoted_to_local_relay"
private const val LOCAL_RELAY_PORT = "local_relay_port"
private const val AUTOPILOT_RELAYS = "autopilot_relays"

// https://github.com/greenart7c3/Citrine/blob/main/app/src/main/java/com/greenart7c3/citrine/server/Settings.kt#L11
private const val DEFAULT_LOCAL_RELAY_PORT = 4869

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

    fun getSendBookmarkedToLocalRelay(): Boolean {
        return preferences.getBoolean(SEND_BOOKMARKED_TO_LOCAL_RELAY, true)
    }

    fun setSendBookmarkedToLocalRelay(sendToLocalRelay: Boolean) {
        preferences.edit()
            .putBoolean(SEND_BOOKMARKED_TO_LOCAL_RELAY, sendToLocalRelay)
            .apply()
    }

    fun getSendUpvotedToLocalRelay(): Boolean {
        return preferences.getBoolean(SEND_UPVOTED_TO_LOCAL_RELAY, false)
    }

    fun setSendUpvotedToLocalRelay(sendToLocalRelay: Boolean) {
        preferences.edit()
            .putBoolean(SEND_UPVOTED_TO_LOCAL_RELAY, sendToLocalRelay)
            .apply()
    }

    fun getAutopilotRelays(): Int {
        val num = preferences.getInt(AUTOPILOT_RELAYS, DEFAULT_AUTOPILOT_RELAYS)
        return if (num in MIN_AUTOPILOT_RELAYS..MAX_AUTOPILOT_RELAYS) num else DEFAULT_AUTOPILOT_RELAYS
    }

    fun setAutopilotRelays(newNumber: Int) {
        val newNum = if (newNumber in MIN_AUTOPILOT_RELAYS..MAX_AUTOPILOT_RELAYS) newNumber
        else DEFAULT_AUTOPILOT_RELAYS

        preferences.edit()
            .putInt(AUTOPILOT_RELAYS, newNum)
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
