package com.dluvian.voyage.data.nostr

import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.RESUB_TIMEOUT
import com.dluvian.voyage.core.utils.textNoteAndRepostKinds
import com.dluvian.voyage.data.account.IMyPubkeyProvider
import com.dluvian.voyage.data.model.BookmarksFeedSetting
import com.dluvian.voyage.data.model.FeedSetting
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.InboxFeedSetting
import com.dluvian.voyage.data.model.ListFeedSetting
import com.dluvian.voyage.data.model.ProfileRootFeedSetting
import com.dluvian.voyage.data.model.ReplyFeedSetting
import com.dluvian.voyage.data.model.TopicFeedSetting
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.provider.TopicProvider
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
    myPubkeyProvider: IMyPubkeyProvider,
    val subCreator: SubscriptionCreator,
    private val relayProvider: RelayProvider,
    private val subBatcher: SubBatcher,
    private val room: AppDatabase
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val feedSubscriber = NostrFeedSubscriber(
        scope = scope,
        relayProvider = relayProvider,
        topicProvider = topicProvider,
        myPubkeyProvider = myPubkeyProvider,
        bookmarkDao = room.bookmarkDao(),
    )

    suspend fun subFeed(
        until: Long,
        limit: Int,
        setting: FeedSetting,
        forceSubscription: Boolean = false
    ) {
        val since = getCachedSinceTimestamp(
            setting = setting,
            until = until,
            pageSize = limit,
            forceSubscription = forceSubscription
        )

        val subscriptions = when (setting) {
            is HomeFeedSetting -> feedSubscriber.getHomeFeedSubscriptions(
                setting = setting,
                until = until.toULong(),
                since = since,
                limit = (4 * limit).toULong() // We don't know if we receive enough root posts
            )

            is ListFeedSetting -> feedSubscriber.getListFeedSubscriptions(
                identifier = setting.identifier,
                until = until.toULong(),
                since = since,
                limit = (4 * limit).toULong() // We don't know if we receive enough root posts
            )

            is TopicFeedSetting -> feedSubscriber.getTopicFeedSubscription(
                topic = setting.topic,
                until = until.toULong(),
                since = since,
                // Smaller than adjustedLimit, bc posts with topics tend to be root
                limit = (2 * limit).toULong()
            )

            is ReplyFeedSetting -> feedSubscriber.getProfileFeedSubscription(
                nprofile = setting.nprofile,
                until = until.toULong(),
                since = since,
                limit = (3 * limit).toULong()
            )

            is ProfileRootFeedSetting -> feedSubscriber.getProfileFeedSubscription(
                nprofile = setting.nprofile,
                until = until.toULong(),
                since = since,
                limit = (4 * limit).toULong()
            )

            is InboxFeedSetting -> feedSubscriber.getInboxFeedSubscription(
                until = until.toULong(),
                since = since,
                limit = limit.toULong()
            )

            BookmarksFeedSetting -> feedSubscriber.getBookmarksFeedSubscription(
                until = until.toULong(),
                since = since,
                limit = limit.toULong()
            )
        }

        subscriptions.forEach { (relay, filters) ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    suspend fun subProfile(nprofile: Nip19Profile) {
        val profileSince = room.profileDao()
            .getMaxCreatedAt(pubkey = nprofile.publicKey().toHex())
            ?.toULong()
            ?: 1uL

        val profileFilter = Filter()
            .kind(kind = Kind.fromEnum(KindEnum.Metadata))
            .author(author = nprofile.publicKey())
            // No +1 because we don't cache all fields
            .since(timestamp = Timestamp.fromSecs(profileSince))
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

    suspend fun subContactList(nprofile: Nip19Profile) {
        val contactFilter = Filter()
            .kind(kind = Kind.fromEnum(KindEnum.ContactList))
            .author(author = nprofile.publicKey())
            .until(timestamp = Timestamp.now())
            .limit(limit = 1u)
        val filters = listOf(contactFilter)

        relayProvider.getObserveRelays(nprofile = nprofile, includeConnected = false)
            .forEach { relay -> subCreator.subscribe(relayUrl = relay, filters = filters) }
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

            relayProvider.getReadRelays().forEach { relay ->
                subBatcher.submitVotesAndReplies(relayUrl = relay, eventIds = newIds)
            }
        }.invokeOnCompletion {
            isSubbingVotesAndReplies.set(false)
        }
    }

    private suspend fun getCachedSinceTimestamp(
        setting: FeedSetting,
        until: Long,
        pageSize: Int,
        forceSubscription: Boolean,
    ): ULong {
        if (pageSize <= 0) return 1uL

        val eightPages = 8 * pageSize

        val timestamps = when (setting) {
            is HomeFeedSetting -> room.homeFeedDao().getHomeRootPostsCreatedAt(
                setting = setting,
                until = until,
                size = eightPages
            )

            is TopicFeedSetting -> room.rootPostDao().getTopicRootPostsCreatedAt(
                topic = setting.topic,
                until = until,
                size = eightPages
            )

            is ListFeedSetting -> room.rootPostDao().getListRootPostsCreatedAt(
                identifier = setting.identifier,
                until = until,
                size = eightPages
            )

            is ProfileRootFeedSetting -> room.rootPostDao().getProfileRootPostsCreatedAt(
                pubkey = setting.nprofile.publicKey().toHex(),
                until = until,
                size = eightPages,
            )

            is ReplyFeedSetting -> room.replyDao().getProfileRepliesCreatedAt(
                pubkey = setting.nprofile.publicKey().toHex(),
                until = until,
                size = eightPages,
            )

            is InboxFeedSetting -> room.postDao().getInboxPostsCreatedAt(
                until = until,
                size = eightPages
            )

            BookmarksFeedSetting -> room.bookmarkDao().getBookmarkedPostsCreatedAt(
                until = until,
                size = eightPages
            )
        }

        if (timestamps.size < pageSize) return 1uL

        val max = timestamps.first()
        val firstPageMin = timestamps[pageSize - 1]
        val avgFeedSpan = max.minus(timestamps.last()).div(7) // 8/7 above average

        val selectedSince = if (forceSubscription) firstPageMin
        // Don't resub page bc it's denser than average + 8/7
        else if (timestamps.size == eightPages && max - firstPageMin < avgFeedSpan) max
        else firstPageMin

        return (selectedSince + 1).toULong()
    }
}
