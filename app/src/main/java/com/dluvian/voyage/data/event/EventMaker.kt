package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.account.AccountManager
import com.dluvian.voyage.data.nostr.Nip65Relay
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.data.nostr.createHashtagTag
import com.dluvian.voyage.data.nostr.createMentionTag
import com.dluvian.voyage.data.nostr.createReplyTag
import com.dluvian.voyage.data.nostr.createSubjectTag
import com.dluvian.voyage.data.nostr.createTitleTag
import rust.nostr.protocol.Bookmarks
import rust.nostr.protocol.Contact
import rust.nostr.protocol.Coordinate
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventBuilder
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Interests
import rust.nostr.protocol.Kind
import rust.nostr.protocol.KindEnum
import rust.nostr.protocol.Metadata
import rust.nostr.protocol.MuteList
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
            reaction = "+",
        ).toUnsignedEvent(publicKey = accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildDelete(eventId: EventId): Result<Event> {
        val unsignedEvent = EventBuilder.delete(ids = listOf(eventId))
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildListDelete(identifier: String): Result<Event> {
        val pubkey = accountManager.getPublicKey()
        val coordinates = listOf(
            Coordinate(Kind.fromEnum(KindEnum.FollowSet), pubkey, identifier),
            Coordinate(Kind.fromEnum(KindEnum.InterestSet), pubkey, identifier)
        )
        val unsignedEvent = EventBuilder.delete(coordinates = coordinates).toUnsignedEvent(pubkey)

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildTopicList(topics: List<Topic>): Result<Event> {
        val interests = Interests(hashtags = topics)
        val unsignedEvent = EventBuilder.interests(list = interests)
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildBookmarkList(postIds: List<EventIdHex>): Result<Event> {
        val bookmarks = Bookmarks(eventIds = postIds.map { EventId.fromHex(it) })
        val unsignedEvent = EventBuilder.bookmarks(list = bookmarks)
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildMuteList(pubkeys: List<PubkeyHex>, topics: List<Topic>): Result<Event> {
        val mutes = MuteList(
            publicKeys = pubkeys.map { PublicKey.fromHex(it) },
            hashtags = topics
        )
        val unsignedEvent = EventBuilder.muteList(list = mutes)
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildContactList(pubkeys: List<PubkeyHex>): Result<Event> {
        val contacts = pubkeys.map { Contact(pk = PublicKey.fromHex(it)) }
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
        val additionalTags = listOf(createTitleTag(title = title))
        val unsignedEvent = EventBuilder.followSet(identifier = identifier, publicKeys = pubkeys)
            .addTags(tags = additionalTags)
            .toUnsignedEvent(publicKey = accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildTopicSet(
        identifier: String,
        title: String,
        topics: List<Topic>
    ): Result<Event> {
        val unsignedEvent = EventBuilder.interestSet(identifier = identifier, hashtags = topics)
            .addTags(tags = listOf(createTitleTag(title = title)))
            .toUnsignedEvent(publicKey = accountManager.getPublicKey())

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
