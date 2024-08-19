package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.voyage.core.MAX_CONTENT_LEN
import com.dluvian.voyage.core.MAX_KEYS_SQL
import com.dluvian.voyage.core.MAX_TOPICS
import com.dluvian.voyage.core.utils.getNormalizedDescription
import com.dluvian.voyage.core.utils.getNormalizedMuteWords
import com.dluvian.voyage.core.utils.getNormalizedTitle
import com.dluvian.voyage.core.utils.getNormalizedTopics
import com.dluvian.voyage.core.utils.getTrimmedSubject
import com.dluvian.voyage.core.utils.takeRandom
import com.dluvian.voyage.data.account.IMyPubkeyProvider
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.data.nostr.SubId
import com.dluvian.voyage.data.nostr.getMetadata
import com.dluvian.voyage.data.nostr.getNip65s
import com.dluvian.voyage.data.nostr.getReplyToId
import com.dluvian.voyage.data.nostr.isPostOrReply
import com.dluvian.voyage.data.nostr.secs
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter
import rust.nostr.protocol.Kind
import rust.nostr.protocol.KindEnum
import rust.nostr.protocol.PublicKey


private const val TAG = "EventValidator"

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

    private val TEXT_NOTE_U64 = Kind.fromEnum(KindEnum.TextNote).asU64()
    private val REPOST_U64 = Kind.fromEnum(KindEnum.Repost).asU64()
    private val REACTION_U64 = Kind.fromEnum(KindEnum.Reaction).asU64()
    private val CONTACT_U64 = Kind.fromEnum(KindEnum.ContactList).asU64()
    private val RELAYS_U64 = Kind.fromEnum(KindEnum.RelayList).asU64()
    private val METADATA_U64 = Kind.fromEnum(KindEnum.Metadata).asU64()
    private val FOLLOW_SET_U64 = Kind.fromEnum(KindEnum.FollowSet).asU64()
    private val INTEREST_SET_U64 = Kind.fromEnum(KindEnum.InterestSet).asU64()
    private val INTERESTS_U64 = Kind.fromEnum(KindEnum.Interests).asU64()
    private val BOOKMARKS_U64 = Kind.fromEnum(KindEnum.Bookmarks).asU64()
    private val MUTE_LIST_U64 = Kind.fromEnum(KindEnum.MuteList).asU64()
    private val LOCK_U64 = 1000uL

    private fun validate(event: Event, relayUrl: RelayUrl): ValidatedEvent? {
        // Match against enum once included in rust-nostr
        val validatedEvent = when (event.kind().asU64()) {
            TEXT_NOTE_U64 -> createValidatedMainPost(
                event = event,
                relayUrl = relayUrl,
                myPubkey = myPubkeyProvider.getPublicKey()
            )

            REPOST_U64 -> createValidatedRepost(event = event, relayUrl = relayUrl)
            REACTION_U64 -> {
                if (event.content() == "-") return null
                val postId = event.eventIds().firstOrNull() ?: return null
                ValidatedVote(
                    id = event.id().toHex(),
                    postId = postId.toHex(),
                    pubkey = event.author().toHex(),
                    createdAt = event.createdAt().secs()
                )
            }

            CONTACT_U64 -> {
                val author = event.author()
                ValidatedContactList(
                    pubkey = author.toHex(),
                    friendPubkeys = event.publicKeys()
                        .filter { it != author }
                        .map { it.toHex() }
                        .distinct()
                        .takeRandom(MAX_KEYS_SQL)
                        .toSet(),
                    createdAt = event.createdAt().secs()
                )
            }

            RELAYS_U64 -> {
                val relays = event.getNip65s()
                if (relays.isEmpty()) return null
                ValidatedNip65(
                    pubkey = event.author().toHex(),
                    relays = relays,
                    createdAt = event.createdAt().secs()
                )
            }

            METADATA_U64 -> {
                val metadata = event.getMetadata() ?: return null
                ValidatedProfile(
                    id = event.id().toHex(),
                    pubkey = event.author().toHex(),
                    metadata = metadata,
                    createdAt = event.createdAt().secs()
                )
            }

            FOLLOW_SET_U64 -> {
                if (event.author().toHex() != myPubkeyProvider.getPubkeyHex()) return null
                createValidatedProfileSet(event = event)
            }

            INTEREST_SET_U64 -> {
                if (event.author().toHex() != myPubkeyProvider.getPubkeyHex()) return null
                createValidatedTopicSet(event = event)
            }

            INTERESTS_U64 -> {
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

            BOOKMARKS_U64 -> {
                val authorHex = event.author().toHex()
                if (authorHex != myPubkeyProvider.getPubkeyHex()) return null
                ValidatedBookmarkList(
                    myPubkey = authorHex,
                    postIds = event.eventIds()
                        .map { it.toHex() }
                        .distinct()
                        .takeRandom(MAX_KEYS_SQL)
                        .toSet(),
                    createdAt = event.createdAt().secs()
                )
            }

            MUTE_LIST_U64 -> {
                val authorHex = event.author().toHex()
                if (authorHex != myPubkeyProvider.getPubkeyHex()) return null
                ValidatedMuteList(
                    myPubkey = authorHex,
                    pubkeys = event.publicKeys().map { it.toHex() }
                        .distinct()
                        .takeRandom(MAX_KEYS_SQL)
                        .toSet(),
                    topics = event.getNormalizedTopics(limit = MAX_KEYS_SQL).toSet(),
                    words = event.getNormalizedMuteWords(limit = MAX_KEYS_SQL).toSet(),
                    createdAt = event.createdAt().secs()
                )
            }

            LOCK_U64 -> ValidatedLock(pubkey = event.author().toHex(), json = event.asJson())

            else -> {
                Log.w(TAG, "Invalid event kind ${event.asJson()}")
                return null
            }
        }

        if (validatedEvent == null) return null
        if (validatedEvent !is ValidatedVote && !event.verify()) return null

        return validatedEvent
    }

    private fun createValidatedRepost(event: Event, relayUrl: RelayUrl): ValidatedCrossPost? {
        val parsedEvent = runCatching { Event.fromJson(event.content()) }.getOrNull()
            ?: return null
        if (!parsedEvent.isPostOrReply()) return null
        val validated = createValidatedMainPost(
            event = parsedEvent,
            relayUrl = relayUrl,
            myPubkey = myPubkeyProvider.getPublicKey()
        )
            ?: return null
        if (!parsedEvent.verify()) return null
        return ValidatedCrossPost(
            id = event.id().toHex(),
            pubkey = event.author().toHex(),
            topics = event.getNormalizedTopics(limit = MAX_TOPICS),
            createdAt = event.createdAt().secs(),
            relayUrl = relayUrl,
            crossPosted = validated
        )
    }

    companion object {
        fun createValidatedMainPost(
            event: Event,
            relayUrl: RelayUrl,
            myPubkey: PublicKey,
        ): ValidatedMainPost? {
            if (!event.isPostOrReply()) return null
            val replyToId = event.getReplyToId()
            val content = event.content().trim().take(MAX_CONTENT_LEN)
            return if (replyToId == null) {
                val subject = event.getTrimmedSubject()
                if (subject.isNullOrEmpty() && content.isEmpty()) return null
                ValidatedRootPost(
                    id = event.id().toHex(),
                    pubkey = event.author().toHex(),
                    topics = event.getNormalizedTopics(limit = MAX_TOPICS),
                    subject = subject,
                    content = content,
                    createdAt = event.createdAt().secs(),
                    relayUrl = relayUrl,
                    json = event.asJson(),
                    isMentioningMe = event.publicKeys().contains(myPubkey)
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
                    json = event.asJson(),
                    isMentioningMe = event.publicKeys().contains(myPubkey)
                )
            }
        }

        fun createValidatedProfileSet(event: Event): ValidatedProfileSet? {
            val identifier = event.identifier() ?: return null

            return ValidatedProfileSet(
                identifier = identifier,
                myPubkey = event.author().toHex(),
                title = event.getNormalizedTitle(),
                description = event.getNormalizedDescription(),
                pubkeys = event.publicKeys()
                    .distinct()
                    .takeRandom(MAX_KEYS_SQL)
                    .map { it.toHex() }
                    .toSet(),
                createdAt = event.createdAt().secs()
            )
        }

        fun createValidatedTopicSet(event: Event): ValidatedTopicSet? {
            val identifier = event.identifier() ?: return null

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
