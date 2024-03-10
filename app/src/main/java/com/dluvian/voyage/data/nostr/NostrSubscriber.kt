package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.nostr_kt.Kind
import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.MAX_EVENTS_TO_SUB
import com.dluvian.voyage.data.keys.IPubkeyProvider
import com.dluvian.voyage.data.provider.FriendProvider
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.provider.TopicProvider
import com.dluvian.voyage.data.provider.WebOfTrustProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter
import rust.nostr.protocol.Timestamp

class NostrSubscriber(
    private val nostrService: NostrService,
    private val relayProvider: RelayProvider,
    private val webOfTrustProvider: WebOfTrustProvider,
    private val topicProvider: TopicProvider,
    private val friendProvider: FriendProvider,
    private val pubkeyProvider: IPubkeyProvider,
) {
    private val tag = "NostrSubscriber"
    private val scope = CoroutineScope(Dispatchers.IO)
    fun subFeed(until: Long, size: Int) {
        val adjustedSize = (5 * size).toULong() // We don't know if we receive enough root posts
        val friendFilter = Filter().kind(kind = Kind.TEXT_NOTE.toULong()) // TODO: Support reposts
            .authors(authors = friendProvider.getFriendPublicKeys())
            .until(timestamp = Timestamp.fromSecs(until.toULong()))
            .limit(limit = adjustedSize)
        val topicFilter = Filter().kind(kind = Kind.TEXT_NOTE.toULong())
            .hashtags(hashtags = topicProvider.getTopics())
            .until(timestamp = Timestamp.fromSecs(until.toULong()))
            .limit(limit = adjustedSize)
        val filters = listOf(friendFilter, topicFilter)

        relayProvider.getReadRelays().forEach { relay ->
            nostrService.subscribe(filters = filters, relayUrl = relay)
        }
        // TODO: sub friend filter in autopilot relays
    }

    // TODO: remove ids after x seconds to enable resubbing
    private val votesAndRepliesCache = mutableSetOf<EventIdHex>()
    private var votesAndRepliesJob: Job? = null
    fun subVotesAndReplies(postIds: Collection<EventIdHex>) {
        if (postIds.isEmpty()) return

        val newIds = postIds - votesAndRepliesCache
        if (newIds.isEmpty()) return

        votesAndRepliesJob?.cancel(CancellationException("Debounce"))
        votesAndRepliesJob = scope.launch {
            delay(DEBOUNCE)
            val currentTimestamp = Timestamp.now()
            val ids = newIds.map { EventId.fromHex(it) }
            val voteFilter = Filter().events(ids)
                .kind(Kind.REACTION.toULong())
                .authors(webOfTrustProvider.getWebOfTrustPubkeys()) // TODO: split into multiple subscriptions if size > 1000
                .until(currentTimestamp)
                .limit(MAX_EVENTS_TO_SUB.toULong())
            val replyFilter = Filter().events(ids)
                .kind(Kind.TEXT_NOTE.toULong())
                .until(currentTimestamp)
                .limit(MAX_EVENTS_TO_SUB.toULong())
            val filters = listOf(voteFilter, replyFilter)

            relayProvider.getReadRelays().forEach { relay ->
                nostrService.subscribe(filters = filters, relayUrl = relay)
            }
        }
        votesAndRepliesJob?.invokeOnCompletion { ex ->
            if (ex == null) votesAndRepliesCache.addAll(newIds)
            else Log.d(tag, "Subbing votes and replies failed: ${ex.message}")
        }
    }

    fun subMyContacts() {
        val contactFilter = Filter().kind(kind = Kind.CONTACT_LIST.toULong())
            .author(pubkeyProvider.getPublicKey())
            .until(timestamp = Timestamp.now())
            .limit(1u)
        val filters = listOf(contactFilter)

        relayProvider.getReadRelays().forEach { relay ->
            nostrService.subscribe(filters = filters, relayUrl = relay)
        }
    }

    fun subMyTopics() {
        val topicFilter = Filter().kind(kind = Kind.TOPIC_LIST.toULong())
            .author(pubkeyProvider.getPublicKey())
            .until(timestamp = Timestamp.now())
            .limit(1u)
        val filters = listOf(topicFilter)

        relayProvider.getReadRelays().forEach { relay ->
            nostrService.subscribe(filters = filters, relayUrl = relay)
        }
    }
}
