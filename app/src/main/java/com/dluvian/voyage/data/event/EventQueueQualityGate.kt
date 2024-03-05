package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.SubId
import com.dluvian.nostr_kt.getTopic
import com.dluvian.nostr_kt.isContactList
import com.dluvian.nostr_kt.isPostOrReply
import com.dluvian.nostr_kt.isReplyPost
import com.dluvian.nostr_kt.isRootPost
import com.dluvian.nostr_kt.isTopicList
import com.dluvian.nostr_kt.isVote
import com.dluvian.nostr_kt.matches
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter
import rust.nostr.protocol.Timestamp
import java.util.Collections


private const val TAG = "EventQueueQualityGate"

class EventQueueQualityGate(private val filterCache: Map<SubId, List<Filter>>) {

    // TODO: Return a new object. Use type system to know event is valid
    fun isSubmittable(event: Event, subId: SubId, relayUrl: RelayUrl): Boolean {
        if (isCached(event = event, relayUrl = relayUrl)) return false

        if (isFromFuture(event = event)) {
            Log.w(TAG, "Discard event from the future, ${event.id().toHex()} from $relayUrl")
            return false
        }
        if (!matchesFilter(subId = subId, event = event)) {
            Log.w(TAG, "Discard event not matching filter, ${event.id().toHex()} from $relayUrl")
            return false
        }
        if (!isValid(event = event)) {
            Log.w(TAG, "Discard invalid event, ${event.id().toHex()} from $relayUrl")
            return false
        }

        cache(event = event, relayUrl = relayUrl)

        return true
    }

    private val idCache = Collections.synchronizedSet(mutableSetOf<EventId>())
    private val postRelayCache =
        Collections.synchronizedSet(mutableSetOf<Pair<EventId, RelayUrl>>())

    private fun isCached(event: Event, relayUrl: RelayUrl): Boolean {
        val idIsCached = idCache.contains(event.id())
        if (event.isPostOrReply()) {
            return idIsCached && postRelayCache.contains(Pair(event.id(), relayUrl))
        }

        return idIsCached
    }

    private fun cache(event: Event, relayUrl: RelayUrl) {
        idCache.add(event.id())
        if (event.isPostOrReply()) postRelayCache.add(Pair(event.id(), relayUrl))
    }

    private var upperTimeBoundary = getUpperTimeBoundary()
    private fun getUpperTimeBoundary() = Timestamp.now().asSecs().toLong() + 60
    private fun isFromFuture(event: Event): Boolean {
        val createdAt = event.createdAt().asSecs().toLong()
        if (createdAt > upperTimeBoundary) {
            upperTimeBoundary = getUpperTimeBoundary()
            return createdAt > upperTimeBoundary
        }
        return false
    }

    private fun matchesFilter(subId: SubId, event: Event): Boolean {
        val cache = filterCache.getOrDefault(subId, emptyList()).toList()
        if (cache.isEmpty()) return false
        return cache.any { it.matches(event) }
    }

    private fun isValid(event: Event): Boolean {
        return ((event.isRootPost() && event.getTopic() != null) ||
                event.isReplyPost() ||
                event.isVote() ||
                event.isContactList() ||
                event.isTopicList()) &&
                event.verify()
    }
}
