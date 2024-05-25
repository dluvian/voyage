package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.SubId
import com.dluvian.nostr_kt.getMetadata
import com.dluvian.nostr_kt.getNip65s
import com.dluvian.nostr_kt.getReactToId
import com.dluvian.nostr_kt.isPostOrReply
import com.dluvian.nostr_kt.secs
import com.dluvian.voyage.core.createValidatedMainPost
import com.dluvian.voyage.core.getNormalizedTopics
import com.dluvian.voyage.data.account.IPubkeyProvider
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter
import rust.nostr.protocol.KindEnum


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
            Log.w(TAG, "Discard event not matching filter, ${id.toHex()} from $relayUrl")
            return null
        }

        val validatedEvent = validate(event = event, relayUrl = relayUrl)
        syncedIdCache.add(id)

        if (validatedEvent == null) {
            Log.w(TAG, "Discard invalid event ${id.toHex()} from $relayUrl: ${event.asJson()}")
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
        val validatedEvent = when (event.kind().asEnum()) {
            is KindEnum.TextNote -> createValidatedMainPost(event = event, relayUrl = relayUrl)
            is KindEnum.Repost -> createValidatedRepost(event = event, relayUrl = relayUrl)
            is KindEnum.Reaction -> {
                val postId = event.getReactToId() ?: return null
                ValidatedVote(
                    id = event.id().toHex(),
                    postId = postId,
                    pubkey = event.author().toHex(),
                    isPositive = event.content() != "-",
                    createdAt = event.createdAt().secs()
                )
            }

            is KindEnum.ContactList -> ValidatedContactList(
                pubkey = event.author().toHex(),
                friendPubkeys = event.publicKeys().map { it.toHex() }.toSet(),
                createdAt = event.createdAt().secs()
            )

            is KindEnum.Interests -> {
                if (event.author().toHex() != pubkeyProvider.getPubkeyHex()) null
                else ValidatedTopicList(
                    myPubkey = event.author().toHex(),
                    topics = event.getNormalizedTopics(limited = false).toSet(),
                    createdAt = event.createdAt().secs()
                )
            }

            is KindEnum.RelayList -> {
                val relays = event.getNip65s()
                if (relays.isEmpty()) return null
                ValidatedNip65(
                    pubkey = event.author().toHex(),
                    relays = relays,
                    createdAt = event.createdAt().secs()
                )
            }

            is KindEnum.Metadata -> {
                val metadata = event.getMetadata() ?: return null
                ValidatedProfile(
                    id = event.id().toHex(),
                    pubkey = event.author().toHex(),
                    metadata = metadata,
                    createdAt = event.createdAt().secs()
                )
            }

            else -> {
                Log.w(TAG, "Invalid event kind ${event.asJson()}")
                return null
            }
        }

        if (validatedEvent == null) return null
        if (!event.verify()) return null

        return validatedEvent
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
            crossPosted = validated
        )
    }
}
