package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.SubId
import com.dluvian.nostr_kt.getCurrentSecs
import com.dluvian.nostr_kt.getHashtags
import com.dluvian.nostr_kt.getNip65s
import com.dluvian.nostr_kt.getReplyToId
import com.dluvian.nostr_kt.getTitle
import com.dluvian.nostr_kt.isContactList
import com.dluvian.nostr_kt.isNip65
import com.dluvian.nostr_kt.isPostOrReply
import com.dluvian.nostr_kt.isReplyPost
import com.dluvian.nostr_kt.isRootPost
import com.dluvian.nostr_kt.isTopicList
import com.dluvian.nostr_kt.isVote
import com.dluvian.nostr_kt.matches
import com.dluvian.nostr_kt.secs
import com.dluvian.voyage.core.MAX_TOPIC_LEN
import com.dluvian.voyage.data.keys.IPubkeyProvider
import com.dluvian.voyage.data.model.RelayedItem
import com.dluvian.voyage.data.model.ValidatedContactList
import com.dluvian.voyage.data.model.ValidatedEvent
import com.dluvian.voyage.data.model.ValidatedNip65
import com.dluvian.voyage.data.model.ValidatedReplyPost
import com.dluvian.voyage.data.model.ValidatedRootPost
import com.dluvian.voyage.data.model.ValidatedTopicList
import com.dluvian.voyage.data.model.ValidatedVote
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter
import java.util.Collections


class EventValidator(
    private val filterCache: Map<SubId, List<Filter>>,
    private val pubkeyProvider: IPubkeyProvider,
) {
    private val tag = "EventValidator"

    fun getValidatedEvent(
        event: Event,
        subId: SubId,
        relayUrl: RelayUrl
    ): RelayedItem<ValidatedEvent>? {
        if (isCached(event = event, relayUrl = relayUrl)) return null

        if (isFromFuture(event = event)) {
            Log.w(tag, "Discard event from the future, ${event.id().toHex()} from $relayUrl")
            return null
        }
        if (!matchesFilter(subId = subId, event = event)) {
            Log.w(tag, "Discard event not matching filter, ${event.id().toHex()} from $relayUrl")
            return null
        }
        val validatedEvent = validate(event = event)
        cache(event = event, relayUrl = relayUrl)
        if (validatedEvent == null) {
            Log.w(tag, "Discard invalid event, ${event.id().toHex()} from $relayUrl")
            return null
        }

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
    private fun getUpperTimeBoundary() = getCurrentSecs() + 60
    private fun isFromFuture(event: Event): Boolean {
        val createdAt = event.createdAt().secs()
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
        val validatedEvent = if (event.isRootPost()) {
            val topics = event.getHashtags().map { it.take(MAX_TOPIC_LEN) }.distinct()
            ValidatedRootPost(
                id = event.id(),
                pubkey = event.author(),
                topics = topics,
                title = event.getTitle(),
                content = event.content(),
                createdAt = event.createdAt().secs()
            )
        } else if (event.isReplyPost()) {
            val replyToId = event.getReplyToId()
            if (replyToId == null || replyToId == event.id().toHex()) null
            else ValidatedReplyPost(
                id = event.id(),
                pubkey = event.author(),
                replyToId = replyToId,
                content = event.content(),
                createdAt = event.createdAt().secs()
            )
        } else if (event.isVote()) {
            val postId = event.eventIds().firstOrNull()
            if (postId == null) null
            else ValidatedVote(
                id = event.id(),
                postId = postId,
                pubkey = event.author(),
                isPositive = event.content() != "-",
                createdAt = event.createdAt().secs()
            )
        } else if (event.isContactList()) {
            ValidatedContactList(
                pubkey = event.author(),
                friendPubkeys = event.publicKeys().toSet(),
                createdAt = event.createdAt().secs()
            )
        } else if (event.isTopicList()) {
            if (event.author().toHex() != pubkeyProvider.getPubkeyHex()) null
            else ValidatedTopicList(
                myPubkey = event.author(),
                topics = event.getHashtags().toSet(),
                createdAt = event.createdAt().secs()
            )
        } else if (event.isNip65()) {
            val relays = event.getNip65s()
            if (relays.isEmpty()) null
            else ValidatedNip65(
                pubkey = event.author(),
                relays = relays,
                createdAt = event.createdAt().secs()
            )
        } else null

        if (validatedEvent == null) return null
        if (!event.verify()) return null

        return validatedEvent
    }
}
