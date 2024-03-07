package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.SubId
import com.dluvian.nostr_kt.getHashtags
import com.dluvian.nostr_kt.getReplyToId
import com.dluvian.nostr_kt.getTitle
import com.dluvian.nostr_kt.isContactList
import com.dluvian.nostr_kt.isPostOrReply
import com.dluvian.nostr_kt.isReplyPost
import com.dluvian.nostr_kt.isRootPost
import com.dluvian.nostr_kt.isTopicList
import com.dluvian.nostr_kt.isVote
import com.dluvian.nostr_kt.matches
import com.dluvian.voyage.data.keys.IPubkeyProvider
import com.dluvian.voyage.data.model.RelayedItem
import com.dluvian.voyage.data.model.ValidatedContactList
import com.dluvian.voyage.data.model.ValidatedEvent
import com.dluvian.voyage.data.model.ValidatedReplyPost
import com.dluvian.voyage.data.model.ValidatedRootPost
import com.dluvian.voyage.data.model.ValidatedTopicList
import com.dluvian.voyage.data.model.ValidatedVote
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter
import rust.nostr.protocol.Timestamp
import java.util.Collections


private const val TAG = "EventQueueQualityGate"

class EventValidator(
    private val filterCache: Map<SubId, List<Filter>>,
    private val pubkeyProvider: IPubkeyProvider,
) {

    fun getValidatedEvent(
        event: Event,
        subId: SubId,
        relayUrl: RelayUrl
    ): RelayedItem<ValidatedEvent>? {
        if (isCached(event = event, relayUrl = relayUrl)) return null

        if (isFromFuture(event = event)) {
            Log.w(TAG, "Discard event from the future, ${event.id().toHex()} from $relayUrl")
            return null
        }
        if (!matchesFilter(subId = subId, event = event)) {
            Log.w(TAG, "Discard event not matching filter, ${event.id().toHex()} from $relayUrl")
            return null
        }
        val validatedEvent = validate(event = event)
        if (validatedEvent == null) {
            Log.w(TAG, "Discard invalid event, ${event.id().toHex()} from $relayUrl")
            return null
        }

        cache(event = event, relayUrl = relayUrl)

        return RelayedItem(item = validatedEvent, relayUrl = relayUrl)
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
        val filters = filterCache.getOrDefault(subId, emptyList()).toList()
        if (filters.isEmpty()) return false
        // TODO: Make sure ReplyEvents match queried e tag. Or we get Foreign Key exceptions
        //TODO: Make sure to not subscribe root posts and replies in a single Subscription. Replies we receive need to be matched against a list of root ids
        return filters.any { it.matches(event) }
    }

    private fun validate(event: Event): ValidatedEvent? {
        if (!event.verify()) return null

        if (event.isRootPost()) {
            val topic = event.getHashtags().firstOrNull() ?: return null
            return ValidatedRootPost(
                id = event.id(),
                pubkey = event.author(),
                topic = topic,
                title = event.getTitle(),
                content = event.content(),
                createdAt = event.createdAt().asSecs().toLong()
            )
        }
        if (event.isReplyPost()) {
            val replyToId = event.getReplyToId() ?: return null
            if (replyToId == event.id().toHex()) return null
            return ValidatedReplyPost(
                id = event.id(),
                pubkey = event.author(),
                replyToId = replyToId,
                content = event.content(),
                createdAt = event.createdAt().asSecs().toLong()
            )
        }
        if (event.isVote()) {
            val postId = event.eventIds().firstOrNull() ?: return null
            return ValidatedVote(
                id = event.id(),
                postId = postId,
                pubkey = event.author(),
                isPositive = event.content() != "-",
                createdAt = event.createdAt().asSecs().toLong()
            )
        }
        if (event.isContactList()) {
            return ValidatedContactList(
                pubkey = event.author(),
                friendPubkeys = event.publicKeys().toSet(),
                createdAt = event.createdAt().asSecs().toLong()
            )
        }
        if (event.isTopicList()) {
            if (event.author().toHex() != pubkeyProvider.getPubkeyHex()) return null
            return ValidatedTopicList(
                myPubkey = event.author(),
                topics = event.getHashtags().toSet(),
                createdAt = event.createdAt().asSecs().toLong()
            )
        }
        return null
    }
}
