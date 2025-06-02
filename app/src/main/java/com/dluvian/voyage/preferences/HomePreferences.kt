package com.dluvian.voyage.preferences

import android.content.Context
import com.dluvian.voyage.PAGE_SIZE
import com.dluvian.voyage.filterSetting.FriendPubkeys
import com.dluvian.voyage.filterSetting.Global
import com.dluvian.voyage.filterSetting.HomeFeedSetting
import com.dluvian.voyage.filterSetting.NoPubkeys

private const val WITH_TOPICS = "with_topics"

private const val KINDS = "kinds"
private const val DEFAULT_KINDS = "1,6" // Only text notes and reposts

private const val PUBKEYS = "pubkeys"
private const val NO_PUBKEYS = "no_pubkeys"
private const val FRIENDS = "friends"
private const val GLOBAL = "global"

class HomePreferences(context: Context) {
    private val preferences = context.getSharedPreferences(HOME_FILE, Context.MODE_PRIVATE)

    fun getHomeFeedSetting(): HomeFeedSetting {
        val withTopics = preferences.getBoolean(WITH_TOPICS, true)
        val pubkeys = when (preferences.getString(PUBKEYS, FRIENDS)) {
            NO_PUBKEYS -> NoPubkeys
            FRIENDS -> FriendPubkeys
            GLOBAL -> Global
            else -> FriendPubkeys
        }
        val kinds = parseKinds(preferences.getString(KINDS, DEFAULT_KINDS) ?: DEFAULT_KINDS)

        return HomeFeedSetting(
            pubkeySelection = pubkeys,
            withTopics = withTopics,
            kinds = kinds,
            pageSize = PAGE_SIZE.toULong()
        )
    }

    fun setHomeFeedSettings(setting: HomeFeedSetting) {
        val pubkeys = when (setting.pubkeySelection) {
            NoPubkeys -> NO_PUBKEYS
            FriendPubkeys -> FRIENDS
            Global -> GLOBAL
        }
        preferences.edit()
            .putBoolean(WITH_TOPICS, setting.withTopics)
            .putString(PUBKEYS, pubkeys)
            .putString(KINDS, kindsToString(setting.kinds))
            .apply()
    }
}
