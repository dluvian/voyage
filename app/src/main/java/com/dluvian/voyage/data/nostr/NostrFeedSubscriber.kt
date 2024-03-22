package com.dluvian.voyage.data.nostr

import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.syncedPutOrAdd
import com.dluvian.voyage.data.model.FilterWrapper
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
        limit: ULong
    ): Map<RelayUrl, List<FilterWrapper>> {
        if (limit <= 0u) return emptyMap()

        val result = mutableMapOf<RelayUrl, MutableList<FilterWrapper>>()

        val friendJob = scope.launch {
            relayProvider
                .getAutopilotRelays(pubkeys = friendProvider.getFriendPubkeys())
                .forEach { (relayUrl, pubkeys) ->
                    val publicKeys = pubkeys.map { PublicKey.fromHex(it) }
                    val friendsNoteFilter = Filter()
                        .kind(kind = Kind.fromEnum(KindEnum.TextNote)) // TODO: Support reposts
                        .authors(authors = publicKeys)
                        .until(timestamp = until)
                        .limit(limit = limit)
                    val friendsNoteFilters = mutableListOf(FilterWrapper(friendsNoteFilter))
                    result.syncedPutOrAdd(relayUrl, friendsNoteFilters)
                }
        }

        val topics = topicProvider.getMyTopics()
        if (topics.isEmpty()) return result

        val topicedNoteFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.TextNote))
            .hashtags(hashtags = topics)
            .until(timestamp = until)
            .limit(limit = limit)
        val topicedNoteFilters = mutableListOf(FilterWrapper(topicedNoteFilter))

        relayProvider.getReadRelays().forEach { relay ->
            result.syncedPutOrAdd(relay, topicedNoteFilters)
        }
        friendJob.join()

        return result
    }

    fun getTopicFeedSubscription(
        topic: Topic,
        until: Timestamp,
        limit: ULong
    ): Map<RelayUrl, List<FilterWrapper>> {
        if (limit <= 0u || topic.isBlank()) return emptyMap()

        val result = mutableMapOf<RelayUrl, MutableList<FilterWrapper>>()

        val topicedNoteFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.TextNote))
            .hashtag(hashtag = topic)
            .until(timestamp = until)
            .limit(limit = limit)
        val topicedNoteFilters = mutableListOf(FilterWrapper(topicedNoteFilter))

        relayProvider.getReadRelays().forEach { relay ->
            result.syncedPutOrAdd(relay, topicedNoteFilters)
        }

        return result
    }


    fun getProfileFeedSubscription(
        pubkey: PubkeyHex,
        until: Timestamp,
        limit: ULong
    ): Map<RelayUrl, List<FilterWrapper>> {
        if (limit <= 0u || pubkey.isBlank()) return emptyMap()

        val result = mutableMapOf<RelayUrl, MutableList<FilterWrapper>>()

        val pubkeyNoteFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.TextNote))
            .author(PublicKey.fromHex(hex = pubkey))
            .until(timestamp = until)
            .limit(limit = limit)
        val pubkeyNoteFilters = mutableListOf(FilterWrapper(pubkeyNoteFilter))

        relayProvider.getReadRelays().forEach { relay ->
            result.syncedPutOrAdd(relay, pubkeyNoteFilters)
        }

        return result
    }
}
