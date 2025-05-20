package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.voyage.core.MAX_CONTENT_LEN
import com.dluvian.voyage.core.MAX_KEYS_SQL
import com.dluvian.voyage.core.MAX_TOPICS
import com.dluvian.voyage.core.utils.getNormalizedDescription
import com.dluvian.voyage.core.utils.getNormalizedTitle
import com.dluvian.voyage.core.utils.getNormalizedTopics
import com.dluvian.voyage.core.utils.takeRandom
import com.dluvian.voyage.data.account.IMyPubkeyProvider
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.data.nostr.SubId
import com.dluvian.voyage.data.nostr.getKindTag
import com.dluvian.voyage.data.nostr.getLegacyReplyToId
import com.dluvian.voyage.data.nostr.getMetadata
import com.dluvian.voyage.data.nostr.getNip65s
import com.dluvian.voyage.data.nostr.getParentId
import com.dluvian.voyage.data.nostr.isTextNote
import com.dluvian.voyage.data.nostr.secs
import rust.nostr.sdk.Event
import rust.nostr.sdk.EventId
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.PublicKey


private const val TAG = "EventValidator"

val TEXT_NOTE_U16 = Kind.fromStd(KindStandard.TEXT_NOTE).asU16()
val REPOST_U16 = Kind.fromStd(KindStandard.REPOST).asU16()
val GENERIC_REPOST_U16 = Kind.fromStd(KindStandard.GENERIC_REPOST).asU16()
private val REACTION_U16 = Kind.fromStd(KindStandard.REACTION).asU16()
private val CONTACT_U16 = Kind.fromStd(KindStandard.CONTACT_LIST).asU16()
private val RELAYS_U16 = Kind.fromStd(KindStandard.RELAY_LIST).asU16()
private val METADATA_U16 = Kind.fromStd(KindStandard.METADATA).asU16()
private val FOLLOW_SET_U16 = Kind.fromStd(KindStandard.FOLLOW_SET).asU16()
private val INTEREST_SET_U16 = Kind.fromStd(KindStandard.INTEREST_SET).asU16()
private val INTERESTS_U16 = Kind.fromStd(KindStandard.INTERESTS).asU16()
private val BOOKMARKS_U16 = Kind.fromStd(KindStandard.BOOKMARKS).asU16()
val COMMENT_U16: UShort = Kind.fromStd(KindStandard.COMMENT).asU16()

class EventValidator(
    private val syncedFilterCache: Map<SubId, List<Filter>>,
    private val syncedIdCache: MutableSet<EventId>,
    private val myPubkeyProvider: IMyPubkeyProvider,
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
        // Match against enum once included in rust-nostr
        val validatedEvent = when (event.kind().asU16()) {
            TEXT_NOTE_U16 -> createValidatedTextNote(
                event = event,
                relayUrl = relayUrl,
                myPubkey = myPubkeyProvider.getPublicKey()
            )

            REPOST_U16, GENERIC_REPOST_U16 -> createValidatedCrosspost(
                event = event,
                relayUrl = relayUrl
            )

            REACTION_U16 -> {
                if (event.content() == "-") return null
                ValidatedVote(
                    id = event.id().toHex(),
                    eventId = event.tags().eventIds().firstOrNull()?.toHex() ?: return null,
                    pubkey = event.author().toHex(),
                    createdAt = event.createdAt().secs()
                )
            }

            COMMENT_U16 -> createValidatedComment(
                event = event,
                relayUrl = relayUrl,
                myPubkey = myPubkeyProvider.getPublicKey()
            )

            CONTACT_U16 -> {
                val author = event.author()
                ValidatedContactList(
                    pubkey = author.toHex(),
                    friendPubkeys = event.tags().publicKeys()
                        .filter { it != author }
                        .map { it.toHex() }
                        .distinct()
                        .takeRandom(MAX_KEYS_SQL)
                        .toSet(),
                    createdAt = event.createdAt().secs()
                )
            }

            RELAYS_U16 -> {
                val relays = event.getNip65s()
                if (relays.isEmpty()) return null
                ValidatedNip65(
                    pubkey = event.author().toHex(),
                    relays = relays,
                    createdAt = event.createdAt().secs()
                )
            }

            METADATA_U16 -> {
                val metadata = event.getMetadata() ?: return null
                ValidatedProfile(
                    id = event.id().toHex(),
                    pubkey = event.author().toHex(),
                    metadata = metadata,
                    createdAt = event.createdAt().secs()
                )
            }

            FOLLOW_SET_U16 -> {
                if (event.author().toHex() != myPubkeyProvider.getPubkeyHex()) return null
                createValidatedProfileSet(event = event)
            }

            INTEREST_SET_U16 -> {
                if (event.author().toHex() != myPubkeyProvider.getPubkeyHex()) return null
                createValidatedTopicSet(event = event)
            }

            INTERESTS_U16 -> {
                val authorHex = event.author().toHex()
                if (authorHex != myPubkeyProvider.getPubkeyHex()) return null
                ValidatedTopicList(
                    myPubkey = authorHex,
                    topics = event.getNormalizedTopics()
                        .distinct()
                        .takeRandom(MAX_KEYS_SQL)
                        .toSet(),
                    createdAt = event.createdAt().secs()
                )
            }

            BOOKMARKS_U16 -> {
                val authorHex = event.author().toHex()
                if (authorHex != myPubkeyProvider.getPubkeyHex()) return null
                ValidatedBookmarkList(
                    myPubkey = authorHex,
                    eventIds = event.tags().eventIds()
                        .map { it.toHex() }
                        .distinct()
                        .takeRandom(MAX_KEYS_SQL)
                        .toSet(),
                    createdAt = event.createdAt().secs()
                )
            }

            else -> {
                Log.w(TAG, "Invalid event kind ${event.asJson()}")
                return null
            }
        }

        if (validatedEvent == null) return null
        if (validatedEvent !is ValidatedVote && !event.verify()) return null

        return validatedEvent
    }

    private fun createValidatedCrosspost(event: Event, relayUrl: RelayUrl): ValidatedCrossPost? {
        val crossPostedId = event.tags().eventIds().firstOrNull()?.toHex() ?: return null
        val crossPostedKind = when (event.kind().asU16()) {
            REPOST_U16 -> TEXT_NOTE_U16
            GENERIC_REPOST_U16 -> event.getKindTag()
            else -> null
        } ?: return null

        val parsedEvent = runCatching { Event.fromJson(event.content()) }.getOrNull()
        val parsedEventKind = parsedEvent?.kind()?.asU16()
        if (parsedEventKind != null && parsedEventKind != crossPostedKind) return null

        val validatedCrossPostedEvent = parsedEvent?.let {
            when (parsedEventKind) {
                TEXT_NOTE_U16 -> createValidatedTextNote(
                    event = it,
                    relayUrl = relayUrl,
                    myPubkey = myPubkeyProvider.getPublicKey()
                )

                COMMENT_U16 -> createValidatedComment(
                    event = it,
                    relayUrl = relayUrl,
                    myPubkey = myPubkeyProvider.getPublicKey()
                )

                else -> null
            }

        }

        if (validatedCrossPostedEvent != null && validatedCrossPostedEvent.id != crossPostedId) {
            return null
        }

        if (parsedEvent?.verify() == false) return null

        return ValidatedCrossPost(
            id = event.id().toHex(),
            pubkey = event.author().toHex(),
            createdAt = event.createdAt().secs(),
            relayUrl = relayUrl,
            topics = event.getNormalizedTopics(limit = MAX_TOPICS),
            crossPostedId = crossPostedId,
            crossPostedThreadableEvent = validatedCrossPostedEvent,
        )
    }

    companion object {
        fun createValidatedTextNote(
            event: Event,
            relayUrl: RelayUrl,
            myPubkey: PublicKey,
        ): ValidatedTextNote? {
            if (!event.isTextNote()) return null
            val replyToId = event.getLegacyReplyToId()
            val content = event.content().trim().take(MAX_CONTENT_LEN)
            return if (replyToId == null) {
                if (content.isEmpty()) return null // TODO: Allow empty notes
                ValidatedRootPost(
                    id = event.id().toHex(),
                    pubkey = event.author().toHex(),
                    content = content,
                    createdAt = event.createdAt().secs(),
                    relayUrl = relayUrl,
                    json = event.asJson(),
                    isMentioningMe = event.isMentioningMe(myPubkey = myPubkey),
                    topics = event.getNormalizedTopics(limit = MAX_TOPICS),
                )
            } else {
                if (content.isEmpty() || replyToId == event.id().toHex()) return null
                ValidatedLegacyReply(
                    id = event.id().toHex(),
                    pubkey = event.author().toHex(),
                    content = content,
                    createdAt = event.createdAt().secs(),
                    relayUrl = relayUrl,
                    json = event.asJson(),
                    isMentioningMe = event.isMentioningMe(myPubkey = myPubkey),
                    parentId = replyToId,
                )
            }
        }

        fun createValidatedComment(
            event: Event,
            relayUrl: RelayUrl,
            myPubkey: PublicKey,
        ): ValidatedComment? {
            if (event.kind().asU16() != COMMENT_U16) return null

            return ValidatedComment(
                id = event.id().toHex(),
                pubkey = event.author().toHex(),
                content = event.content(),
                createdAt = event.createdAt().secs(),
                relayUrl = relayUrl,
                json = event.asJson(),
                isMentioningMe = event.isMentioningMe(myPubkey = myPubkey),
                // Null means we don't support the parent (i and a tags)
                parentId = event.getParentId(),
                parentKind = event.getKindTag(),
            )
        }

        fun createValidatedProfileSet(event: Event): ValidatedProfileSet? {
            val identifier = event.tags().identifier() ?: return null

            return ValidatedProfileSet(
                identifier = identifier,
                myPubkey = event.author().toHex(),
                title = event.getNormalizedTitle(),
                description = event.getNormalizedDescription(),
                pubkeys = event.tags().publicKeys()
                    .distinct()
                    .takeRandom(MAX_KEYS_SQL)
                    .map { it.toHex() }
                    .toSet(),
                createdAt = event.createdAt().secs()
            )
        }

        fun createValidatedTopicSet(event: Event): ValidatedTopicSet? {
            val identifier = event.tags().identifier() ?: return null

            return ValidatedTopicSet(
                identifier = identifier,
                myPubkey = event.author().toHex(),
                title = event.getNormalizedTitle(),
                description = event.getNormalizedDescription(),
                topics = event.getNormalizedTopics().takeRandom(MAX_KEYS_SQL).toSet(),
                createdAt = event.createdAt().secs()
            )
        }
    }

}

private fun Event.isMentioningMe(myPubkey: PublicKey): Boolean {
    return this.tags().publicKeys().any { it == myPubkey }
}
