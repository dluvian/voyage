package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.MAX_EVENTS_TO_SUB
import com.dluvian.voyage.core.MAX_KEYS
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.limitRestricted
import com.dluvian.voyage.core.syncedPutOrAdd
import com.dluvian.voyage.core.takeRandom
import com.dluvian.voyage.core.textNoteAndRepostKinds
import com.dluvian.voyage.data.account.IPubkeyProvider
import com.dluvian.voyage.data.model.FriendPubkeys
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.provider.TopicProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import rust.nostr.protocol.Filter
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.Timestamp

private const val TAG = "NostrFeedSubscriber"
class NostrFeedSubscriber(
    private val scope: CoroutineScope,
    private val relayProvider: RelayProvider,
    private val topicProvider: TopicProvider,
    private val pubkeyProvider: IPubkeyProvider,
) {
    suspend fun getHomeFeedSubscriptions(
        until: ULong,
        since: ULong,
        limit: ULong
    ): Map<RelayUrl, List<Filter>> {
        if (limit <= 0u || since >= until) return emptyMap()

        val result = mutableMapOf<RelayUrl, MutableList<Filter>>()

        val sinceTimestamp = Timestamp.fromSecs(since)
        val untilTimestamp = Timestamp.fromSecs(until)

        val friendJob = scope.launch {
            relayProvider
                .getObserveRelays(selection = FriendPubkeys)
                .forEach { (relayUrl, pubkeys) ->
                    val publicKeys = pubkeys.takeRandom(MAX_KEYS).map { PublicKey.fromHex(it) }
                    val friendsNoteFilter = Filter()
                        .kinds(kinds = textNoteAndRepostKinds)
                        .authors(authors = publicKeys)
                        .since(timestamp = sinceTimestamp)
                        .until(timestamp = untilTimestamp)
                        .limitRestricted(limit = limit)
                    val friendsNoteFilters = mutableListOf(friendsNoteFilter)
                    result.syncedPutOrAdd(relayUrl, friendsNoteFilters)
                }
        }

        val topics = topicProvider.getMyTopics()
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

        friendJob.join()

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
        pubkey: PubkeyHex,
        until: ULong,
        since: ULong,
        limit: ULong
    ): Map<RelayUrl, List<Filter>> {
        if (limit <= 0u || pubkey.isBlank() || since >= until) return emptyMap()

        val result = mutableMapOf<RelayUrl, MutableList<Filter>>()

        val pubkeyNoteFilter = Filter()
            .kinds(kinds = textNoteAndRepostKinds)
            .author(PublicKey.fromHex(hex = pubkey))
            .since(timestamp = Timestamp.fromSecs(since))
            .until(timestamp = Timestamp.fromSecs(until))
            .limitRestricted(limit = limit)
        val pubkeyNoteFilters = mutableListOf(pubkeyNoteFilter)

        relayProvider.getObserveRelays(pubkey = pubkey).forEach { relay ->
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
            .kinds(kinds = textNoteAndRepostKinds)
            .pubkey(pubkey = pubkeyProvider.getPublicKey())
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

        val ids = TODO() // Take random max keys

        val bookedMarkedNotesFilter = Filter()
            .kinds(kinds = textNoteAndRepostKinds)
            .events(ids = ids)
            .since(timestamp = Timestamp.fromSecs(since))
            .until(timestamp = Timestamp.fromSecs(until))
            .limitRestricted(limit = limit)
        val filters = mutableListOf(bookedMarkedNotesFilter)

        return relayProvider.getReadRelays().associateWith { filters }
    }
}
