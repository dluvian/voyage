package com.dluvian.voyage.provider

import android.util.Log
import com.dluvian.voyage.NostrService
import com.dluvian.voyage.filterSetting.BookmarksFeedSetting
import com.dluvian.voyage.filterSetting.FeedSetting
import com.dluvian.voyage.filterSetting.FriendPubkeys
import com.dluvian.voyage.filterSetting.Global
import com.dluvian.voyage.filterSetting.HomeFeedSetting
import com.dluvian.voyage.filterSetting.InboxFeedSetting
import com.dluvian.voyage.filterSetting.ListFeedSetting
import com.dluvian.voyage.filterSetting.NoPubkeys
import com.dluvian.voyage.filterSetting.ProfileFeedSetting
import com.dluvian.voyage.filterSetting.TopicFeedSetting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rust.nostr.sdk.Event
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Timestamp

class FeedProvider(
    private val service: NostrService,
    private val topicProvider: TopicProvider,
    private val trustProvider: TrustProvider
) {
    private val logTag = "FeedProvider"
    private val scope = CoroutineScope(Dispatchers.IO)

    // TODO: Events are currently skipped
    //  if a page ends in a createdAt of which there are more events available
    suspend fun buildFeed(
        until: Timestamp,
        setting: FeedSetting,
    ): List<Event> {
        return when (setting) {
            is HomeFeedSetting -> buildHomeFeed(until, setting)
            is InboxFeedSetting -> TODO()
            is ListFeedSetting -> TODO()
            is ProfileFeedSetting -> TODO()
            is TopicFeedSetting -> TODO()
            BookmarksFeedSetting -> TODO()
        }
    }

    suspend fun buildHomeFeed(until: Timestamp, setting: HomeFeedSetting): List<Event> {
        val primaryFeedFilter = when (setting.pubkeySelection) {
            FriendPubkeys -> Filter().authors(trustProvider.friends())
            Global -> Filter()
            NoPubkeys -> null
        }
        val topicFeedFilter = if (setting.withTopics) Filter().hashtags(topicProvider.topics())
        else null

        val filters = listOfNotNull(primaryFeedFilter, topicFeedFilter)
            .map { it.kinds(setting.kinds).until(until).limit(setting.pageSize) }

        if (filters.isEmpty()) {
            Log.i(logTag, "No filter for home feed selected")
            return emptyList()
        }

        val feed = mutableListOf<Event>()
        for (filter in filters) {
            feed.addAll(service.dbQuery(filter).toVec())
        }

        val orderedFeed = feed.distinctBy { it.id() }
            .sortedBy { it.id().toHex() } // Use pow to decide order when createdAt is same
            .sortedByDescending { it.createdAt().asSecs() } // Newest first
            .take(setting.pageSize.toInt())

        for (filter in filters) {
            scope.launch {
                service.sync(filter)
            }
        }

        return orderedFeed
    }
}
