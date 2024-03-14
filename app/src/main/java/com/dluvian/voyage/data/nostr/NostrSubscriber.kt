package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.nostr_kt.Kind
import com.dluvian.nostr_kt.NostrClient
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.SubId
import com.dluvian.nostr_kt.createFriendFilter
import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.MAX_EVENTS_TO_SUB
import com.dluvian.voyage.core.RND_RESUB_COUNT
import com.dluvian.voyage.data.account.IPubkeyProvider
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
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.Timestamp

class NostrSubscriber(
    private val relayProvider: RelayProvider,
    private val webOfTrustProvider: WebOfTrustProvider,
    private val topicProvider: TopicProvider,
    private val friendProvider: FriendProvider,
    private val pubkeyProvider: IPubkeyProvider,
    private val nostrClient: NostrClient,
    private val syncedFilterCache: MutableMap<SubId, List<Filter>>,
) {
    private val tag = "NostrSubscriber"
    private val scope = CoroutineScope(Dispatchers.IO)

    fun subFeed(until: Long, limit: Int) {
        val adjustedLimit = (3 * limit).toULong() // We don't know if we receive enough root posts
        val topicFilter = Filter().kind(kind = Kind.TEXT_NOTE.toULong())
            .hashtags(hashtags = topicProvider.getTopics())
            .until(timestamp = Timestamp.fromSecs(until.toULong()))
            .limit(limit = adjustedLimit)
        val topicFilters = listOf(topicFilter)
        relayProvider.getReadRelays().forEach { relay ->
            subscribe(filters = topicFilters, relayUrl = relay)
        }

        relayProvider
            .getAutopilotRelays(pubkeys = friendProvider.getFriendPubkeys())
            .forEach { (relayUrl, pubkeys) ->
                val publicKeys = pubkeys.map { PublicKey.fromHex(it) }
                val friendFilter = createFriendFilter(
                    pubkeys = publicKeys,
                    until = until.toULong(),
                    limit = adjustedLimit,
                )
                subscribe(filters = listOf(friendFilter), relayUrl = relayUrl)
            }
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
                subscribe(filters = filters, relayUrl = relay)
            }
        }
        votesAndRepliesJob?.invokeOnCompletion { ex ->
            if (ex != null) return@invokeOnCompletion

            votesAndRepliesCache.addAll(newIds)
            Log.d(tag, "Finished subscribing votes and replies")
        }
    }

    suspend fun subMyAccountAndTrustData() {
        subMyAccount()
        delay(DELAY_1SEC)
        lazySubFriendsNip65()
        delay(DELAY_1SEC)
        lazySubWebOfTrust()
    }

    private fun subMyAccount() {
        val timestamp = Timestamp.now()
        val myContactFilter = Filter().kind(kind = Kind.CONTACT_LIST.toULong())
            .author(pubkeyProvider.getPublicKey())
            .until(timestamp = timestamp)
            .limit(1u)
        val myTopicsFilter = Filter().kind(kind = Kind.TOPIC_LIST.toULong())
            .author(pubkeyProvider.getPublicKey())
            .until(timestamp = timestamp)
            .limit(1u)
        val myNip65Filter = Filter().kind(kind = Kind.NIP65.toULong())
            .author(pubkeyProvider.getPublicKey())
            .until(timestamp = timestamp)
            .limit(1u)
        val filters = listOf(myContactFilter, myTopicsFilter, myNip65Filter)

        relayProvider.getReadRelays().forEach { relay ->
            subscribe(filters = filters, relayUrl = relay)
        }
    }

    private suspend fun lazySubFriendsNip65() {
        val timestamp = Timestamp.now()
        val friends = friendProvider.getFriendPubkeys()
//
//        // TODO: Sub nip65s
//
    }

    private suspend fun lazySubWebOfTrust() {
        val timestamp = Timestamp.now()
        val friendsWithMissingContactList = friendProvider.getFriendsWithMissingContactList()
        val randomResub = friendProvider.getFriendPubkeys().shuffled().take(RND_RESUB_COUNT)
        val webOfTrustResub = (friendsWithMissingContactList + randomResub).distinct()
        if (webOfTrustResub.isEmpty()) return

        relayProvider
            .getObserveRelays(observeFrom = webOfTrustResub)
            .forEach { (relay, pubkeys) ->
                val webOfTrustFilter = Filter().kind(kind = Kind.CONTACT_LIST.toULong())
                    .authors(authors = pubkeys.map { PublicKey.fromHex(it) })
                    .until(timestamp = timestamp)
                val filters = listOf(webOfTrustFilter)
                subscribe(filters = filters, relayUrl = relay)
            }
    }

    private fun subscribe(filters: List<Filter>, relayUrl: RelayUrl): SubId? {
        if (filters.isEmpty()) return null
        Log.d(tag, "Subscribe ${filters.size} in $relayUrl")

        val subId = nostrClient.subscribe(filters = filters, relayUrl = relayUrl)
        if (subId == null) {
            Log.w(tag, "Failed to create subscription ID")
            return null
        }
        syncedFilterCache[subId] = filters

        return subId
    }
}
