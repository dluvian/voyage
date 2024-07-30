package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.voyage.core.MAX_EVENTS_TO_SUB
import com.dluvian.voyage.core.MAX_KEYS
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.utils.limitRestricted
import com.dluvian.voyage.core.utils.syncedPutOrAdd
import com.dluvian.voyage.core.utils.takeRandom
import com.dluvian.voyage.core.utils.textNoteAndRepostKinds
import com.dluvian.voyage.data.account.IMyPubkeyProvider
import com.dluvian.voyage.data.model.FriendPubkeys
import com.dluvian.voyage.data.model.ListPubkeys
import com.dluvian.voyage.data.model.ListTopics
import com.dluvian.voyage.data.model.MyTopics
import com.dluvian.voyage.data.model.PubkeySelection
import com.dluvian.voyage.data.model.TopicSelection
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.provider.TopicProvider
import com.dluvian.voyage.data.room.dao.BookmarkDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter
import rust.nostr.protocol.Kind
import rust.nostr.protocol.KindEnum
import rust.nostr.protocol.Nip19Profile
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.Timestamp

private const val TAG = "NostrFeedSubscriber"

class NostrFeedSubscriber(
    private val scope: CoroutineScope,
    private val relayProvider: RelayProvider,
    private val topicProvider: TopicProvider,
    private val myPubkeyProvider: IMyPubkeyProvider,
    private val bookmarkDao: BookmarkDao,
) {
    suspend fun getHomeFeedSubscriptions(
        until: ULong,
        since: ULong,
        limit: ULong
    ): Map<RelayUrl, List<Filter>> {
        return getPeopleAndTopicFeed(
            pubkeySelection = FriendPubkeys,
            topicSelection = MyTopics,
            until = until,
            since = since,
            limit = limit
        )
    }

    suspend fun getListFeedSubscriptions(
        identifier: String,
        until: ULong,
        since: ULong,
        limit: ULong
    ): Map<RelayUrl, List<Filter>> {
        return getPeopleAndTopicFeed(
            pubkeySelection = ListPubkeys(identifier = identifier),
            topicSelection = ListTopics(identifier = identifier),
            until = until,
            since = since,
            limit = limit
        )
    }

    private suspend fun getPeopleAndTopicFeed(
        pubkeySelection: PubkeySelection,
        topicSelection: TopicSelection,
        until: ULong,
        since: ULong,
        limit: ULong
    ): Map<RelayUrl, List<Filter>> {
        if (limit <= 0u || since >= until) return emptyMap()

        val result = mutableMapOf<RelayUrl, MutableList<Filter>>()

        val sinceTimestamp = Timestamp.fromSecs(since)
        val untilTimestamp = Timestamp.fromSecs(until)

        val peopleJob = scope.launch {
            relayProvider
                .getObserveRelays(selection = pubkeySelection)
                .filter { (_, pubkeys) -> pubkeys.isNotEmpty() }
                .forEach { (relayUrl, pubkeys) ->
                    val publicKeys = pubkeys.takeRandom(MAX_KEYS).map { PublicKey.fromHex(it) }
                    val pubkeysNoteFilter = Filter()
                        .kinds(kinds = textNoteAndRepostKinds)
                        .authors(authors = publicKeys)
                        .since(timestamp = sinceTimestamp)
                        .until(timestamp = untilTimestamp)
                        .limitRestricted(limit = limit)
                    val pubkeysNoteFilters = mutableListOf(pubkeysNoteFilter)
                    result.syncedPutOrAdd(relayUrl, pubkeysNoteFilters)
                }
        }

        val topics = topicProvider.getTopicSelection(
            topicSelection = topicSelection,
            limit = MAX_KEYS
        )
        if (topics.isNotEmpty()) {
            val topicedNoteFilter = Filter()
                .kinds(kinds = textNoteAndRepostKinds)
                .hashtags(hashtags = topics)
                .since(timestamp = sinceTimestamp)
                .until(timestamp = untilTimestamp)
                .limitRestricted(limit = limit)
            val topicedNoteFilters = mutableListOf(topicedNoteFilter)

            relayProvider.getReadRelays().forEach { relay ->
                result.syncedPutOrAdd(relay, topicedNoteFilters)
            }
        }

        peopleJob.join()

        return result
    }

    fun getTopicFeedSubscription(
        topic: Topic,
        until: ULong,
        since: ULong,
        limit: ULong
    ): Map<RelayUrl, List<Filter>> {
        if (limit <= 0u || topic.isBlank() || since >= until) return emptyMap()

        val result = mutableMapOf<RelayUrl, MutableList<Filter>>()

        val topicedNoteFilter = Filter()
            .kinds(kinds = textNoteAndRepostKinds)
            .hashtag(hashtag = topic)
            .since(timestamp = Timestamp.fromSecs(since))
            .until(timestamp = Timestamp.fromSecs(until))
            .limitRestricted(limit = limit)
        val topicedNoteFilters = mutableListOf(topicedNoteFilter)

        relayProvider.getReadRelays().forEach { relay ->
            result.syncedPutOrAdd(relay, topicedNoteFilters)
        }

        return result
    }

    suspend fun getProfileFeedSubscription(
        nprofile: Nip19Profile,
        until: ULong,
        since: ULong,
        limit: ULong
    ): Map<RelayUrl, List<Filter>> {
        if (limit <= 0u || since >= until) return emptyMap()

        val result = mutableMapOf<RelayUrl, MutableList<Filter>>()

        val pubkeyNoteFilter = Filter()
            .kinds(kinds = textNoteAndRepostKinds)
            .author(author = nprofile.publicKey())
            .since(timestamp = Timestamp.fromSecs(since))
            .until(timestamp = Timestamp.fromSecs(until))
            .limitRestricted(limit = limit)
        val pubkeyNoteFilters = mutableListOf(pubkeyNoteFilter)

        relayProvider.getObserveRelays(nprofile = nprofile).forEach { relay ->
            val present = result.putIfAbsent(relay, pubkeyNoteFilters)
            present?.addAll(pubkeyNoteFilters)
        }

        return result
    }

    fun getInboxFeedSubscription(
        until: ULong,
        since: ULong,
        limit: ULong
    ): Map<RelayUrl, List<Filter>> {
        if (limit <= 0u || since >= until) return emptyMap()
        Log.d(TAG, "getInboxFeedSubscription")

        val mentionFilter = Filter()
            .kind(kind = Kind.fromEnum(KindEnum.TextNote))
            .pubkey(pubkey = myPubkeyProvider.getPublicKey())
            .since(timestamp = Timestamp.fromSecs(since))
            .until(timestamp = Timestamp.fromSecs(until))
            .limitRestricted(limit = MAX_EVENTS_TO_SUB)
        val filters = listOf(mentionFilter)

        return relayProvider.getReadRelays().associateWith { filters }
    }

    suspend fun getBookmarksFeedSubscription(
        until: ULong,
        since: ULong,
        limit: ULong
    ): Map<RelayUrl, List<Filter>> {
        if (limit <= 0u) return emptyMap()
        Log.d(TAG, "getBookmarksFeedSubscription")

        val ids = bookmarkDao.getUnknownBookmarks()
            .takeRandom(MAX_KEYS)
            .map { EventId.fromHex(it) }

        if (ids.isEmpty()) return emptyMap()

        val bookedMarkedNotesFilter = Filter()
            .kind(kind = Kind.fromEnum(KindEnum.TextNote)) // We don't support bookmarking the repost itself
            .events(ids = ids)
            .since(timestamp = Timestamp.fromSecs(since))
            .until(timestamp = Timestamp.fromSecs(until))
            .limitRestricted(limit = limit)
        val filters = mutableListOf(bookedMarkedNotesFilter)

        return relayProvider.getReadRelays().associateWith { filters }
    }
}
