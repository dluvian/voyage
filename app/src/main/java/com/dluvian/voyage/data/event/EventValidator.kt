package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.SubId
import com.dluvian.nostr_kt.getMetadata
import com.dluvian.nostr_kt.getNip65s
import com.dluvian.nostr_kt.getReplyToId
import com.dluvian.nostr_kt.getSubject
import com.dluvian.nostr_kt.isContactList
import com.dluvian.nostr_kt.isNip65
import com.dluvian.nostr_kt.isPostOrReply
import com.dluvian.nostr_kt.isProfile
import com.dluvian.nostr_kt.isRepost
import com.dluvian.nostr_kt.isTopicList
import com.dluvian.nostr_kt.isVote
import com.dluvian.nostr_kt.secs
import com.dluvian.voyage.core.MAX_SUBJECT_LEN
import com.dluvian.voyage.core.getNormalizedTopics
import com.dluvian.voyage.data.account.IPubkeyProvider
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter


private const val TAG = "EventValidator"

class EventValidator(
    private val syncedFilterCache: Map<SubId, List<Filter>>,
    private val syncedIdCache: MutableSet<EventId>,
    private val pubkeyProvider: IPubkeyProvider,
) {

    fun getValidatedEvent(
        event: Event,
        subId: SubId,
        relayUrl: RelayUrl
    ): ValidatedEvent? {
        val id = event.id()
        if (syncedIdCache.contains(id)) return null

        if (!matchesFilter(subId = subId, event = event)) {
            Log.v(TAG, "Discard event not matching filter, $id from $relayUrl")
            return null
        }

        val validatedEvent = validate(event = event, relayUrl = relayUrl)
        syncedIdCache.add(id)

        if (validatedEvent == null) {
            Log.w(TAG, "Discard invalid event $id from $relayUrl")
            return null
        }

        return validatedEvent
    }

    private fun matchesFilter(subId: SubId, event: Event): Boolean {
        val filters = syncedFilterCache.getOrDefault(subId, emptyList()).toList()
        if (filters.isEmpty()) {
            Log.w(TAG, "Filter is empty")
            return false
        }

        return filters.any { it.matchEvent(event = event) }
    }

    private fun validate(event: Event, relayUrl: RelayUrl): ValidatedEvent? {
        val validatedEvent = if (event.isPostOrReply()) {
            createValidatedMainPost(event = event, relayUrl = relayUrl)
        } else if (event.isRepost()) {
            Log.i("LOLOL", "repost: ${event.id().toHex()}")
            createValidatedRepost(event = event, relayUrl = relayUrl)
        } else if (event.isVote()) {
            val postId = event.eventIds().firstOrNull() ?: return null
            ValidatedVote(
                id = event.id().toHex(),
                postId = postId.toHex(),
                pubkey = event.author().toHex(),
                isPositive = event.content() != "-",
                createdAt = event.createdAt().secs()
            )
        } else if (event.isContactList()) {
            ValidatedContactList(
                pubkey = event.author().toHex(),
                friendPubkeys = event.publicKeys().map { it.toHex() }.toSet(),
                createdAt = event.createdAt().secs()
            )
        } else if (event.isTopicList()) {
            if (event.author().toHex() != pubkeyProvider.getPubkeyHex()) return null
            ValidatedTopicList(
                myPubkey = event.author().toHex(),
                topics = event.getNormalizedTopics(limited = false).toSet(),
                createdAt = event.createdAt().secs()
            )
        } else if (event.isNip65()) {
            val relays = event.getNip65s()
            if (relays.isEmpty()) return null
            ValidatedNip65(
                pubkey = event.author().toHex(),
                relays = relays,
                createdAt = event.createdAt().secs()
            )
        } else if (event.isProfile()) {
            val metadata = event.getMetadata() ?: return null
            ValidatedProfile(
                id = event.id().toHex(),
                pubkey = event.author().toHex(),
                metadata = metadata,
                createdAt = event.createdAt().secs()
            )
        } else null

        if (validatedEvent == null) return null
        if (!event.verify()) return null

        return validatedEvent
    }

    private fun createValidatedMainPost(event: Event, relayUrl: RelayUrl): ValidatedMainPost? {
        val replyToId = event.getReplyToId()
        val content = event.content().trim()
        return if (replyToId == null) {
            val subject = event.getSubject()?.trim()?.take(MAX_SUBJECT_LEN)
            if (subject.isNullOrEmpty() && content.isEmpty()) return null
            ValidatedRootPost(
                id = event.id().toHex(),
                pubkey = event.author().toHex(),
                topics = event.getNormalizedTopics(limited = true),
                subject = subject,
                content = content,
                createdAt = event.createdAt().secs(),
                relayUrl = relayUrl,
            )
        } else {
            if (content.isEmpty() || replyToId == event.id().toHex()) return null
            ValidatedReply(
                id = event.id().toHex(),
                pubkey = event.author().toHex(),
                parentId = replyToId,
                content = content,
                createdAt = event.createdAt().secs(),
                relayUrl = relayUrl,
            )
        }
    }

    private fun createValidatedRepost(event: Event, relayUrl: RelayUrl): ValidatedCrossPost? {
        val parsedEvent = runCatching { Event.fromJson(event.content()) }.getOrNull()
            ?: return null
        if (!parsedEvent.isPostOrReply()) return null
        val validated = createValidatedMainPost(event = parsedEvent, relayUrl = relayUrl)
            ?: return null
        if (!parsedEvent.verify()) return null
        return ValidatedCrossPost(
            id = event.id().toHex(),
            pubkey = event.author().toHex(),
            topics = event.getNormalizedTopics(limited = true),
            createdAt = event.createdAt().secs(),
            relayUrl = relayUrl,
            crossPost = validated
        )
    }
}
