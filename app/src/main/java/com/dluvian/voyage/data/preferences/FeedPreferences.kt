package com.dluvian.voyage.data.preferences

import android.content.Context
import com.dluvian.voyage.data.model.ListTopics
import com.dluvian.voyage.data.model.MyTopics
import com.dluvian.voyage.data.model.NoTopics
import com.dluvian.voyage.data.model.feed.Friends
import com.dluvian.voyage.data.model.feed.Global
import com.dluvian.voyage.data.model.feed.HomeFeedSetting
import com.dluvian.voyage.data.model.feed.NoPubkeys
import com.dluvian.voyage.data.model.feed.WebOfTrust

private const val TOPICS = "topics"
private const val NO_TOPICS = "no_topics"
private const val MY_TOPICS = "my_topics"

private const val PUBKEYS = "pubkeys"
private const val NO_PUBKEYS = "no_pubkeys"
private const val FRIENDS = "friends"
private const val WEB_OF_TRUST = "web_of_trust"
private const val GLOBAL = "global"

class FeedPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(DATABASE_FILE, Context.MODE_PRIVATE)

    fun getHomeFeedSetting(): HomeFeedSetting {
        val topics = when (preferences.getString(TOPICS, MY_TOPICS)) {
            NO_TOPICS -> NoTopics
            MY_TOPICS -> MyTopics
            else -> MyTopics
        }
        val pubkeys = when (preferences.getString(PUBKEYS, FRIENDS)) {
            NO_PUBKEYS -> NoPubkeys
            FRIENDS -> Friends
            WEB_OF_TRUST -> WebOfTrust
            GLOBAL -> Global
            else -> Friends
        }
        return HomeFeedSetting(topicSelection = topics, feedPubkeySelection = pubkeys)
    }

    fun setHomeFeedSettings(setting: HomeFeedSetting) {
        val topics = when (setting.topicSelection) {
            MyTopics, is ListTopics -> MY_TOPICS
            NoTopics -> NO_TOPICS
        }
        val pubkeys = when (setting.feedPubkeySelection) {
            NoPubkeys -> NO_PUBKEYS
            Friends -> FRIENDS
            WebOfTrust -> WEB_OF_TRUST
            Global -> GLOBAL
        }
        preferences.edit()
            .putString(TOPICS, topics)
            .putString(PUBKEYS, pubkeys)
            .apply()
    }
}
