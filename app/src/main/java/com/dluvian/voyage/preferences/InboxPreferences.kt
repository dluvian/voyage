package com.dluvian.voyage.preferences

import android.content.Context
import com.dluvian.voyage.PAGE_SIZE
import com.dluvian.voyage.filterSetting.FriendPubkeys
import com.dluvian.voyage.filterSetting.Global
import com.dluvian.voyage.filterSetting.InboxFeedSetting
import com.dluvian.voyage.filterSetting.NoPubkeys

private const val PUBKEYS = "pubkeys"
private const val FRIENDS = "friends"
private const val GLOBAL = "global"

private const val KINDS = "kinds"
private const val DEFAULT_KINDS = "1,1111" // Only text notes and comments

class InboxPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(INBOX_FILE, Context.MODE_PRIVATE)

    fun getInboxFeedSetting(): InboxFeedSetting {
        val pubkeys = when (preferences.getString(PUBKEYS, GLOBAL)) {
            FRIENDS -> FriendPubkeys
            GLOBAL -> Global
            else -> Global
        }
        val kinds = parseKinds(preferences.getString(KINDS, DEFAULT_KINDS) ?: DEFAULT_KINDS)
        return InboxFeedSetting(
            pubkeySelection = pubkeys,
            kinds = kinds,
            pageSize = PAGE_SIZE.toULong()
        )
    }

    fun setInboxFeedSettings(setting: InboxFeedSetting) {
        val pubkeys = when (setting.pubkeySelection) {
            FriendPubkeys -> FRIENDS
            Global -> GLOBAL
            NoPubkeys -> GLOBAL
        }
        preferences.edit()
            .putString(PUBKEYS, pubkeys)
            .putString(KINDS, kindsToString(setting.kinds))
            .apply()
    }
}
