package com.dluvian.voyage.data.nostr

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.FEED_OFFSET
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.RESUB_TIMEOUT
import com.dluvian.voyage.core.model.Poll
import com.dluvian.voyage.data.account.IMyPubkeyProvider
import com.dluvian.voyage.data.model.BookmarksFeedSetting
import com.dluvian.voyage.data.model.FeedSetting
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.InboxFeedSetting
import com.dluvian.voyage.data.model.ListFeedSetting
import com.dluvian.voyage.data.model.ProfileFeedSetting
import com.dluvian.voyage.data.model.ReplyFeedSetting
import com.dluvian.voyage.data.model.TopicFeedSetting
import com.dluvian.voyage.data.provider.FriendProvider
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.provider.TopicProvider
import com.dluvian.voyage.data.room.AppDatabase
import com.dluvian.voyage.data.room.entity.main.poll.PollEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Nip19Event
import rust.nostr.sdk.Nip19Profile
import rust.nostr.sdk.PublicKey
import rust.nostr.sdk.Timestamp
import java.util.concurrent.atomic.AtomicBoolean

class NostrSubscriber(
    topicProvider: TopicProvider,
    myPubkeyProvider: IMyPubkeyProvider,
    friendProvider: FriendProvider,
    val subCreator: SubscriptionCreator,
    private val relayProvider: RelayProvider,
    private val subBatcher: SubBatcher,
    private val room: AppDatabase,
    private val filterCreator: FilterCreator,
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val feedSubscriber = FeedSubscriber(
        relayProvider = relayProvider,
        topicProvider = topicProvider,
        myPubkeyProvider = myPubkeyProvider,
        bookmarkDao = room.bookmarkDao(),
        friendProvider = friendProvider,
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
                limit = if (setting.showRoots) (3 * limit).toULong() // We don't know if we receive enough root posts
                else limit.toULong() + FEED_OFFSET.toULong()
            )

            is ListFeedSetting -> feedSubscriber.getListFeedSubscriptions(
                identifier = setting.identifier,
                until = until.toULong(),
                since = since,
                limit = (3 * limit).toULong() // We don't know if we receive enough root posts
            )

            is TopicFeedSetting -> feedSubscriber.getTopicFeedSubscription(
                topic = setting.topic,
                until = until.toULong(),
                since = since,
                // Smaller than adjustedLimit, bc posts with topics tend to be root
                limit = (2.5 * limit).toULong()
            )

            is ReplyFeedSetting -> feedSubscriber.getReplyFeedSubscription(
                nprofile = setting.nprofile,
                until = until.toULong(),
                since = since,
                limit = (2.5 * limit).toULong()
            )

            is ProfileFeedSetting -> feedSubscriber.getProfileFeedSubscription(
                nprofile = setting.nprofile,
                until = until.toULong(),
                since = since,
                limit = (3 * limit).toULong()
            )

            is InboxFeedSetting -> feedSubscriber.getInboxFeedSubscription(
                setting = setting,
                until = until.toULong(),
                since = since,
                limit = limit.toULong() + FEED_OFFSET.toULong()
            )

            BookmarksFeedSetting -> feedSubscriber.getBookmarksFeedSubscription(
                until = until.toULong(),
                since = since,
                limit = limit.toULong() + FEED_OFFSET.toULong()
            )
        }

        subscriptions.forEach { (relay, filters) ->
            subCreator.subscribeMany(relayUrl = relay, filters = filters)
        }
    }

    suspend fun subPost(nevent: Nip19Event) {
        val filter = filterCreator.getPostFilter(eventId = nevent.eventId())

        relayProvider.getObserveRelays(nevent = nevent, includeConnected = true).forEach { relay ->
            subCreator.subscribe(relayUrl = relay, filter = filter)
        }
    }

    suspend fun subContactList(nprofile: Nip19Profile) {
        val filter = filterCreator.getContactFilter(pubkeys = listOf(nprofile.publicKey()))

        relayProvider.getObserveRelays(nprofile = nprofile, includeConnected = false)
            .forEach { relay -> subCreator.subscribe(relayUrl = relay, filter = filter) }
    }

    private val isSubbingVotes = AtomicBoolean(false)
    private val lastVoteUpdate = mutableLongStateOf(System.currentTimeMillis())
    private val voteCache = mutableSetOf<EventIdHex>()
    fun subMyVotes(parentIds: Collection<EventIdHex>) {
        subReactoryEvents(
            parentIds = parentIds,
            isSubbing = isSubbingVotes,
            lastUpdate = lastVoteUpdate,
            cache = voteCache,
            isVote = true,
            isReply = false,
        )
    }

    private val isSubbingReplies = AtomicBoolean(false)
    private val lastReplyUpdate = mutableLongStateOf(System.currentTimeMillis())
    private val replyCache = mutableSetOf<EventIdHex>()
    fun subReplies(parentIds: Collection<EventIdHex>) {
        subReactoryEvents(
            parentIds = parentIds,
            isSubbing = isSubbingReplies,
            lastUpdate = lastReplyUpdate,
            cache = replyCache,
            isVote = false,
            isReply = true,
        )
    }

    private fun subReactoryEvents(
        parentIds: Collection<EventIdHex>,
        isSubbing: AtomicBoolean,
        lastUpdate: MutableState<Long>,
        cache: MutableSet<EventIdHex>,
        isVote: Boolean,
        isReply: Boolean,
    ) {
        if (parentIds.isEmpty()) return
        if (!isSubbing.compareAndSet(false, true)) return

        scope.launch(Dispatchers.Default) {
            val currentMillis = System.currentTimeMillis()
            if (currentMillis - lastUpdate.value > RESUB_TIMEOUT) {
                cache.clear()
                lastUpdate.value = currentMillis
            }

            val newIds = parentIds - cache
            if (newIds.isEmpty()) return@launch

            cache.addAll(newIds)

            relayProvider.getReadRelays().forEach { relay ->
                if (isVote) subBatcher.submitMyVotes(relayUrl = relay, eventIds = newIds)
                if (isReply) subBatcher.submitReplies(relayUrl = relay, eventIds = newIds)
            }
        }.invokeOnCompletion {
            isSubbing.set(false)
        }
    }


    private val pollCache = mutableSetOf<EventIdHex>()
    private var lastPollUpdate = System.currentTimeMillis()
    private val isSubbingPolls = AtomicBoolean(false)

    fun subPollResponses(polls: Collection<Poll>) {
        if (polls.isEmpty()) return
        if (!isSubbingPolls.compareAndSet(false, true)) return

        scope.launch(Dispatchers.Default) {
            val currentMillis = System.currentTimeMillis()
            if (currentMillis - lastPollUpdate > RESUB_TIMEOUT) {
                pollCache.clear()
                lastPollUpdate = currentMillis
            }

            val newPolls = polls.filterNot { pollCache.contains(it.id) }
            if (newPolls.isEmpty()) return@launch

            pollCache.addAll(newPolls.map { it.id })

            val now = Timestamp.now()
            val nowInSecs = now.asSecs().toLong()

            val filters = mutableListOf<Filter>()

            val openPolls = newPolls.filter { it.endsAt == null || it.endsAt > nowInSecs }
            if (openPolls.isNotEmpty()) {
                val filter = filterCreator.getPollResponseFilter(
                    pollIds = openPolls.map { it.id },
                    since = Timestamp.fromSecs(openPolls.minOf { it.createdAt }.toULong()),
                    until = now
                )
                filters.add(filter)
            }

            val closedPolls = newPolls.filter { it.endsAt != null && it.endsAt <= nowInSecs }
            // We are not too strict here :D
            val maxEndsAt = closedPolls.maxOfOrNull { it.endsAt ?: nowInSecs }
            if (closedPolls.isNotEmpty() && maxEndsAt != null) {
                val filter = filterCreator.getPollResponseFilter(
                    pollIds = closedPolls.map { it.id },
                    since = Timestamp.fromSecs(closedPolls.minOf { it.createdAt }.toULong()),
                    until = Timestamp.fromSecs(maxEndsAt.toULong())
                )
                filters.add(filter)
            }

            relayProvider.getReadRelays().forEach { relay ->
                subCreator.subscribeMany(relayUrl = relay, filters = filters)
            }
        }.invokeOnCompletion {
            isSubbingPolls.set(false)
        }
    }

    fun subPollResponsesByEntity(poll: PollEntity) {
        val filter = filterCreator.getPollResponseFilter(
            pollIds = listOf(poll.eventId),
            since = Timestamp.fromSecs(1uL),
            until = poll.endsAt?.let { Timestamp.fromSecs(it.toULong()) } ?: Timestamp.now()
        )

        relayProvider.getReadRelays().forEach { relay ->
            subCreator.subscribe(relayUrl = relay, filter = filter)
        }
    }

    private val pubkeyCache = mutableSetOf<PubkeyHex>()
    private val isSubbingProfiles = AtomicBoolean(false)
    fun subProfiles(pubkeys: Collection<PubkeyHex>) {
        if (pubkeys.isEmpty()) return
        val newPubkeys = pubkeys - pubkeyCache

        if (newPubkeys.isEmpty()) return
        if (!isSubbingProfiles.compareAndSet(false, true)) return
        pubkeyCache.addAll(newPubkeys)

        scope.launch(Dispatchers.Default) {
            val filter = filterCreator.getProfileFilter(
                pubkeys = newPubkeys.map { PublicKey.parse(it) }
            )
            relayProvider.getReadRelays().forEach { relay ->
                subCreator.subscribe(relayUrl = relay, filter = filter)
            }
            delay(DEBOUNCE)
        }.invokeOnCompletion {
            isSubbingProfiles.set(false)
        }
    }

    private suspend fun getCachedSinceTimestamp(
        setting: FeedSetting,
        until: Long,
        pageSize: Int,
        forceSubscription: Boolean,
    ): ULong {
        if (pageSize <= 0) return 1uL

        val timestamps = when (setting) {
            is HomeFeedSetting -> room.homeFeedDao().getHomeFeedCreatedAt(
                setting = setting,
                until = until,
                size = pageSize
            )

            is TopicFeedSetting -> room.feedDao().getTopicFeedCreatedAt(
                topic = setting.topic,
                until = until,
                size = pageSize
            )

            is ListFeedSetting -> room.feedDao().getListFeedCreatedAt(
                identifier = setting.identifier,
                until = until,
                size = pageSize
            )

            is ProfileFeedSetting -> room.feedDao().getProfileFeedCreatedAt(
                pubkey = setting.nprofile.publicKey().toHex(),
                until = until,
                size = pageSize,
            )

            is ReplyFeedSetting -> room.someReplyDao().getProfileRepliesCreatedAt(
                pubkey = setting.nprofile.publicKey().toHex(),
                until = until,
                size = pageSize,
            )

            is InboxFeedSetting -> room.inboxDao().getInboxCreatedAt(
                setting = setting,
                until = until,
                size = pageSize
            )

            BookmarksFeedSetting -> room.bookmarkDao().getBookmarkedPostsCreatedAt(
                until = until,
                size = pageSize
            )
        }.sortedDescending()
        if (timestamps.size < pageSize) return 1uL

        val max = timestamps.first()
        val min = timestamps.last()

        val selectedSince = if (forceSubscription) min
        // Don't resub page when it's denser than 1h
        else if (timestamps.size == pageSize && max - min < 60 * 60) max
        else min

        return (selectedSince + 1).toULong()
    }
}
