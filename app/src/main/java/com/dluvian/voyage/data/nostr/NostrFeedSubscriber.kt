package com.dluvian.voyage.data.nostr

import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.syncedPutOrAdd
import com.dluvian.voyage.data.provider.FriendProvider
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.provider.TopicProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import rust.nostr.protocol.Filter
import rust.nostr.protocol.Kind
import rust.nostr.protocol.KindEnum
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.Timestamp

class NostrFeedSubscriber(
    private val scope: CoroutineScope,
    private val relayProvider: RelayProvider,
    private val topicProvider: TopicProvider,
    private val friendProvider: FriendProvider,
) {
    suspend fun getHomeFeedSubscriptions(
        until: Timestamp,
        since: Timestamp,
        limit: ULong
    ): Map<RelayUrl, List<Filter>> {
        if (limit <= 0u) return emptyMap()

        val result = mutableMapOf<RelayUrl, MutableList<Filter>>()

        val friendJob = scope.launch {
            relayProvider
                .getObserveRelays(pubkeys = friendProvider.getFriendPubkeys())
                .forEach { (relayUrl, pubkeys) ->
                    val publicKeys = pubkeys.map { PublicKey.fromHex(it) }
                    val friendsNoteFilter = Filter()
                        .kind(kind = Kind.fromEnum(KindEnum.TextNote))
                        .authors(authors = publicKeys)
                        .until(timestamp = until)
                        .limit(limit = limit)
                        .since(timestamp = since)
                    val friendsNoteFilters = mutableListOf(friendsNoteFilter)
                    result.syncedPutOrAdd(relayUrl, friendsNoteFilters)
                }
        }

        val topics = topicProvider.getMyTopics()
        if (topics.isNotEmpty()) {
            val topicedNoteFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.TextNote))
                .hashtags(hashtags = topics)
                .until(timestamp = until)
                .limit(limit = limit)
                .since(timestamp = since)
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
        until: Timestamp,
        since: Timestamp,
        limit: ULong
    ): Map<RelayUrl, List<Filter>> {
        if (limit <= 0u || topic.isBlank()) return emptyMap()

        val result = mutableMapOf<RelayUrl, MutableList<Filter>>()

        val topicedNoteFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.TextNote))
            .hashtag(hashtag = topic)
            .until(timestamp = until)
            .since(timestamp = since)
            .limit(limit = limit)
        val topicedNoteFilters = mutableListOf(topicedNoteFilter)

        relayProvider.getReadRelays().forEach { relay ->
            result.syncedPutOrAdd(relay, topicedNoteFilters)
        }

        return result
    }

    suspend fun getProfileFeedSubscription(
        pubkey: PubkeyHex,
        until: Timestamp,
        since: Timestamp,
        limit: ULong
    ): Map<RelayUrl, List<Filter>> {
        if (limit <= 0u || pubkey.isBlank()) return emptyMap()

        val result = mutableMapOf<RelayUrl, MutableList<Filter>>()

        val pubkeyNoteFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.TextNote))
            .author(PublicKey.fromHex(hex = pubkey))
            .until(timestamp = until)
            .since(timestamp = since)
            .limit(limit = limit)
        val pubkeyNoteFilters = mutableListOf(pubkeyNoteFilter)

        relayProvider.getObserveRelays(pubkey = pubkey).forEach { relay ->
            val present = result.putIfAbsent(relay, pubkeyNoteFilters)
            present?.addAll(pubkeyNoteFilters)
        }

        return result
    }
}
