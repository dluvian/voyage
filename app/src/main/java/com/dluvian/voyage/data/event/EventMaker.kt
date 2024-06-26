package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.nostr_kt.Nip65Relay
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.createHashtagTag
import com.dluvian.nostr_kt.createMentionTag
import com.dluvian.nostr_kt.createReplyTag
import com.dluvian.nostr_kt.createSubjectTag
import com.dluvian.nostr_kt.createUnsignedProfileSet
import com.dluvian.nostr_kt.createUnsignedTopicSet
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.account.AccountManager
import rust.nostr.protocol.Bookmarks
import rust.nostr.protocol.Contact
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventBuilder
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Interests
import rust.nostr.protocol.Kind
import rust.nostr.protocol.KindEnum
import rust.nostr.protocol.Metadata
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.RelayMetadata
import rust.nostr.protocol.Tag

private const val TAG = "EventMaker"

class EventMaker(
    private val accountManager: AccountManager,
) {
    suspend fun buildPost(
        subject: String, content: String,
        topics: List<Topic>,
        mentions: List<PubkeyHex>
    ): Result<Event> {
        val tags = mutableListOf<Tag>()
        if (subject.isNotEmpty()) tags.add(createSubjectTag(subject = subject))
        topics.forEach { tags.add(createHashtagTag(it)) }
        if (mentions.isNotEmpty()) tags.addAll(createMentionTag(pubkeys = mentions))

        val unsignedEvent = EventBuilder
            .textNote(content = content, tags = tags)
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildReply(
        parentId: EventId, mentions: Collection<PublicKey>,
        relayHint: RelayUrl,
        pubkeyHint: PubkeyHex,
        content: String
    ): Result<Event> {
        val tags = mutableListOf(
            createReplyTag(
                parentEventId = parentId,
                relayHint = relayHint,
                pubkeyHint = pubkeyHint
            )
        )
        mentions.forEach { tags.add(Tag.publicKey(publicKey = it)) }

        val unsignedEvent = EventBuilder
            .textNote(content = content, tags = tags)
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildCrossPost(
        crossPostedEvent: Event, topics: List<Topic>,
        relayHint: RelayUrl
    ): Result<Event> {
        val unsignedEvent = EventBuilder
            .repost(event = crossPostedEvent, relayUrl = relayHint)
            .addTags(tags = topics.map { createHashtagTag(hashtag = it) })
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildVote(eventId: EventId, mention: PublicKey): Result<Event> {
        val unsignedEvent = EventBuilder.reactionExtended(
            eventId = eventId,
            publicKey = mention,
            kind = Kind(1u),
            reaction = "+",
        ).toUnsignedEvent(publicKey = accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildDelete(eventId: EventId): Result<Event> {
        val unsignedEvent = EventBuilder.delete(ids = listOf(eventId), reason = null)
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildListDelete(identifier: String): Result<Event> {
        val pubkey = accountManager.getPublicKey()
        val tags = listOf(Kind.fromEnum(KindEnum.FollowSets), Kind.fromEnum(KindEnum.InterestSets))
            .map { kind -> "${kind.asU64()}:${pubkey.toHex()}:$identifier" }
            .map { coordinate -> Tag.parse(listOf("a", coordinate)) }
        val unsignedEvent = EventBuilder(
            kind = Kind.fromEnum(KindEnum.EventDeletion),
            content = "",
            tags = tags
        ).toUnsignedEvent(pubkey)

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildTopicList(topics: List<Topic>): Result<Event> {
        val interests = Interests(hashtags = topics, coordinate = emptyList())
        val unsignedEvent = EventBuilder.interests(list = interests)
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildBookmarkList(postIds: List<EventIdHex>): Result<Event> {
        val bookmarks = Bookmarks(
            eventIds = postIds.map { EventId.fromHex(it) },
            coordinate = emptyList(),
            hashtags = emptyList(),
            urls = emptyList()
        )
        val unsignedEvent = EventBuilder.bookmarks(list = bookmarks)
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildContactList(pubkeys: List<PubkeyHex>): Result<Event> {
        val contacts = pubkeys
            .map { Contact(pk = PublicKey.fromHex(it), relayUrl = null, alias = null) }
        val unsignedEvent = EventBuilder.contactList(list = contacts)
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildNip65(relays: List<Nip65Relay>): Result<Event> {
        val metadata = mutableMapOf<RelayUrl, RelayMetadata?>()

        relays.forEach {
            if (it.isRead && it.isWrite) metadata[it.url] = null
            else if (it.isRead) metadata[it.url] = RelayMetadata.READ
            else metadata[it.url] = RelayMetadata.WRITE
        }

        val unsignedEvent = EventBuilder.relayList(map = metadata)
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildProfileSet(
        identifier: String,
        title: String,
        pubkeys: List<PublicKey>
    ): Result<Event> {
        val unsignedEvent = createUnsignedProfileSet(
            identifier = identifier,
            title = title,
            pubkeys = pubkeys,
            author = accountManager.getPublicKey()
        )

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildTopicSet(
        identifier: String,
        title: String,
        topics: List<Topic>
    ): Result<Event> {
        val unsignedEvent = createUnsignedTopicSet(
            identifier = identifier,
            title = title,
            topics = topics,
            author = accountManager.getPublicKey()
        )

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildProfile(metadata: Metadata): Result<Event> {
        val unsignedEvent = EventBuilder.metadata(metadata)
            .toUnsignedEvent(publicKey = accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildAuth(relayUrl: RelayUrl, challenge: String): Result<Event> {
        Log.d(TAG, "Build AUTH for $relayUrl")
        val unsignedEvent = runCatching {
            EventBuilder.auth(challenge = challenge, relayUrl = relayUrl)
                .toUnsignedEvent(publicKey = accountManager.getPublicKey())
        }

        return if (unsignedEvent.isSuccess) {
            accountManager.sign(unsignedEvent = unsignedEvent.getOrThrow())
        } else {
            Log.w(TAG, "EventBuilder.auth for $relayUrl failed")
            Result.failure(
                unsignedEvent.exceptionOrNull() ?: IllegalStateException("EventBuilder.auth failed")
            )
        }
    }
}
