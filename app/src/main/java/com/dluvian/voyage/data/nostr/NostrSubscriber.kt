package com.dluvian.voyage.data.nostr

import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.FEED_RESUB_SPAN_THRESHOLD_SECS
import com.dluvian.voyage.core.MAX_KEYS
import com.dluvian.voyage.core.RESUB_TIMEOUT
import com.dluvian.voyage.core.textNoteAndRepostKinds
import com.dluvian.voyage.data.account.IPubkeyProvider
import com.dluvian.voyage.data.model.FeedSetting
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.InboxFeedSetting
import com.dluvian.voyage.data.model.ProfileRootFeedSetting
import com.dluvian.voyage.data.model.ReplyFeedSetting
import com.dluvian.voyage.data.model.TopicFeedSetting
import com.dluvian.voyage.data.provider.FriendProvider
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.provider.TopicProvider
import com.dluvian.voyage.data.provider.WebOfTrustProvider
import com.dluvian.voyage.data.room.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rust.nostr.protocol.Filter
import rust.nostr.protocol.Kind
import rust.nostr.protocol.KindEnum
import rust.nostr.protocol.Nip19Event
import rust.nostr.protocol.Nip19Profile
import rust.nostr.protocol.Timestamp
import java.util.concurrent.atomic.AtomicBoolean

class NostrSubscriber(
    topicProvider: TopicProvider,
    friendProvider: FriendProvider,
    pubkeyProvider: IPubkeyProvider,
    val subCreator: SubscriptionCreator,
    private val relayProvider: RelayProvider,
    private val webOfTrustProvider: WebOfTrustProvider,
    private val subBatcher: SubBatcher,
    private val room: AppDatabase
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val feedSubscriber = NostrFeedSubscriber(
        scope = scope,
        relayProvider = relayProvider,
        topicProvider = topicProvider,
        pubkeyProvider = pubkeyProvider,
    )

    suspend fun subFeed(until: Long, limit: Int, setting: FeedSetting) {
        val since = getCachedSinceTimestamp(setting = setting, until = until, pageSize = limit)

        val subscriptions = when (setting) {
            is HomeFeedSetting -> feedSubscriber.getHomeFeedSubscriptions(
                until = until.toULong(),
                since = since,
                limit = (3 * limit).toULong() // We don't know if we receive enough root posts
            )

            is ReplyFeedSetting -> feedSubscriber.getHomeFeedSubscriptions(
                until = until.toULong(),
                since = since,
                limit = (2 * limit).toULong()
            )

            is TopicFeedSetting -> feedSubscriber.getTopicFeedSubscription(
                topic = setting.topic,
                until = until.toULong(),
                since = since,
                // Smaller than adjustedLimit, bc posts with topics tend to be root
                limit = (2 * limit).toULong()
            )

            is ProfileRootFeedSetting -> feedSubscriber.getProfileFeedSubscription(
                pubkey = setting.pubkey,
                until = until.toULong(),
                since = since,
                limit = (3 * limit).toULong()
            )

            InboxFeedSetting -> feedSubscriber.getInboxFeedSubscription(
                until = until.toULong(),
                since = since,
                limit = limit.toULong()
            )
        }

        subscriptions.forEach { (relay, filters) ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    // No lazySubProfile bc we always don't save fields in db
    suspend fun subProfile(nprofile: Nip19Profile) {
        val profileFilter = Filter()
            .kind(kind = Kind.fromEnum(KindEnum.Metadata))
            .author(author = nprofile.publicKey())
            .until(timestamp = Timestamp.now())
            .limit(1u)
        val filters = listOf(profileFilter)

        relayProvider.getObserveRelays(nprofile = nprofile).forEach { relay ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    suspend fun subPost(nevent: Nip19Event) {
        val postFilter = Filter()
            .kinds(kinds = textNoteAndRepostKinds)
            .id(id = nevent.eventId())
            .until(timestamp = Timestamp.now())
            .limit(limit = 1u)
        val filters = listOf(postFilter)

        relayProvider.getObserveRelays(nevent = nevent, includeConnected = true).forEach { relay ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    private val votesAndRepliesCache = mutableSetOf<EventIdHex>()
    private var lastUpdate = System.currentTimeMillis()
    private val isSubbingVotesAndReplies = AtomicBoolean(false)

    fun subVotesAndReplies(parentIds: Collection<EventIdHex>) {
        if (parentIds.isEmpty()) return
        if (!isSubbingVotesAndReplies.compareAndSet(false, true)) return

        scope.launch(Dispatchers.Default) {
            val currentMillis = System.currentTimeMillis()
            if (currentMillis - lastUpdate > RESUB_TIMEOUT) {
                votesAndRepliesCache.clear()
                lastUpdate = currentMillis
            }

            val newIds = parentIds - votesAndRepliesCache
            if (newIds.isEmpty()) return@launch

            votesAndRepliesCache.addAll(newIds)

            val votePubkeys = webOfTrustProvider
                .getFriendsAndWebOfTrustPubkeys(includeMyself = true, max = MAX_KEYS)
            relayProvider.getReadRelays().forEach { relay ->
                subBatcher.submitVotesAndReplies(
                    relayUrl = relay,
                    eventIds = newIds,
                    votePubkeys = votePubkeys
                )
            }
        }.invokeOnCompletion {
            isSubbingVotesAndReplies.set(false)
        }
    }

    private suspend fun getCachedSinceTimestamp(
        setting: FeedSetting,
        until: Long,
        pageSize: Int
    ): ULong {
        val pageSizeAndHalfOfNext = pageSize.times(1.5).toInt()

        val timestamps = when (setting) {
            HomeFeedSetting -> room.rootPostDao().getHomeRootPostsCreatedAt(
                until = until,
                size = pageSizeAndHalfOfNext
            )

            is TopicFeedSetting -> room.rootPostDao().getTopicRootPostsCreatedAt(
                topic = setting.topic,
                until = until,
                size = pageSizeAndHalfOfNext
            )

            is ProfileRootFeedSetting -> room.rootPostDao().getProfileRootPostsCreatedAt(
                pubkey = setting.pubkey,
                until = until,
                size = pageSizeAndHalfOfNext,
            )

            is ReplyFeedSetting -> room.replyDao().getProfileRepliesCreatedAt(
                pubkey = setting.pubkey,
                until = until,
                size = pageSizeAndHalfOfNext,
            )

            InboxFeedSetting -> room.postDao().getInboxPostsCreatedAt(
                until = until,
                size = pageSizeAndHalfOfNext
            )
        }

        if (timestamps.size < pageSizeAndHalfOfNext) return 1uL

        val min = timestamps.min()
        val max = timestamps.max()
        val selectedSince = if (max - min <= FEED_RESUB_SPAN_THRESHOLD_SECS) max else min

        return (selectedSince + 1).toULong()
    }
}
