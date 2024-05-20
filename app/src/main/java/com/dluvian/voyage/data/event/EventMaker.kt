package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.nostr_kt.Nip65Relay
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.createHashtagTag
import com.dluvian.nostr_kt.createMentionTag
import com.dluvian.nostr_kt.createReplyTag
import com.dluvian.nostr_kt.createSubjectTag
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.SignerLauncher
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.account.AccountManager
import rust.nostr.protocol.Contact
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventBuilder
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Interests
import rust.nostr.protocol.Kind
import rust.nostr.protocol.Metadata
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.RelayMetadata
import rust.nostr.protocol.Tag

private const val TAG = "EventMaker"

class EventMaker(
    private val accountManager: AccountManager,
) {
    suspend fun buildPost(
        subject: String,
        content: String,
        topics: List<Topic>,
        mentions: List<PubkeyHex>,
        signerLauncher: SignerLauncher,
    ): Result<Event> {
        val tags = mutableListOf<Tag>()
        if (subject.isNotEmpty()) tags.add(createSubjectTag(subject = subject))
        topics.forEach { tags.add(createHashtagTag(it)) }
        if (mentions.isNotEmpty()) tags.addAll(createMentionTag(pubkeys = mentions))

        val unsignedEvent = EventBuilder
            .textNote(content = content, tags = tags)
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(signerLauncher = signerLauncher, unsignedEvent = unsignedEvent)
    }

    suspend fun buildReply(
        parentId: EventId,
        mentions: Collection<PublicKey>,
        relayHint: RelayUrl,
        content: String,
        signerLauncher: SignerLauncher,
    ): Result<Event> {
        val tags = mutableListOf(createReplyTag(parentEventId = parentId, relayHint = relayHint))
        mentions.forEach { tags.add(Tag.publicKey(publicKey = it)) }

        val unsignedEvent = EventBuilder
            .textNote(content = content, tags = tags)
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(signerLauncher = signerLauncher, unsignedEvent = unsignedEvent)
    }

    suspend fun buildCrossPost(
        crossPostedEvent: Event,
        topics: List<Topic>,
        relayHint: RelayUrl,
        signerLauncher: SignerLauncher,
    ): Result<Event> {
        val unsignedEvent = EventBuilder
            .repost(event = crossPostedEvent, relayUrl = relayHint)
            .addTags(tags = topics.map { createHashtagTag(hashtag = it) })
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(signerLauncher = signerLauncher, unsignedEvent = unsignedEvent)
    }

    suspend fun buildVote(
        eventId: EventId,
        mention: PublicKey,
        isPositive: Boolean,
        kind: Kind,
        signerLauncher: SignerLauncher,
    ): Result<Event> {
        val unsignedEvent = EventBuilder.reactionExtended(
            eventId = eventId,
            publicKey = mention,
            kind = kind,
            reaction = if (isPositive) "+" else "-",
        ).toUnsignedEvent(publicKey = accountManager.getPublicKey())

        return accountManager.sign(signerLauncher = signerLauncher, unsignedEvent = unsignedEvent)
    }

    suspend fun buildDelete(
        eventId: EventId,
        signerLauncher: SignerLauncher,
    ): Result<Event> {
        val unsignedEvent = EventBuilder.delete(ids = listOf(eventId), reason = null)
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(signerLauncher = signerLauncher, unsignedEvent = unsignedEvent)
    }

    suspend fun buildTopicList(
        topics: List<Topic>,
        signerLauncher: SignerLauncher,
    ): Result<Event> {
        val interests = Interests(hashtags = topics, coordinate = emptyList())
        val unsignedEvent = EventBuilder.interests(list = interests)
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(signerLauncher = signerLauncher, unsignedEvent = unsignedEvent)
    }

    suspend fun buildContactList(
        pubkeys: List<PubkeyHex>,
        signerLauncher: SignerLauncher,
    ): Result<Event> {
        val contacts = pubkeys
            .map { Contact(pk = PublicKey.fromHex(it), relayUrl = null, alias = null) }
        val unsignedEvent = EventBuilder.contactList(list = contacts)
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(signerLauncher = signerLauncher, unsignedEvent = unsignedEvent)
    }

    suspend fun buildNip65(
        relays: List<Nip65Relay>,
        signerLauncher: SignerLauncher,
    ): Result<Event> {
        val metadata = mutableMapOf<RelayUrl, RelayMetadata?>()

        relays.forEach {
            if (it.isRead && it.isWrite) metadata[it.url] = null
            else if (it.isRead) metadata[it.url] = RelayMetadata.READ
            else metadata[it.url] = RelayMetadata.WRITE
        }

        val unsignedEvent = EventBuilder.relayList(map = metadata)
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(signerLauncher = signerLauncher, unsignedEvent = unsignedEvent)
    }

    suspend fun buildProfile(
        metadata: Metadata,
        signerLauncher: SignerLauncher,
    ): Result<Event> {
        val unsignedEvent = EventBuilder.metadata(metadata)
            .toUnsignedEvent(publicKey = accountManager.getPublicKey())

        return accountManager.sign(signerLauncher = signerLauncher, unsignedEvent = unsignedEvent)
    }

    suspend fun buildAuth(
        relayUrl: RelayUrl,
        challenge: String,
        signerLauncher: SignerLauncher,
    ): Result<Event> {
        Log.d(TAG, "Build AUTH for $relayUrl")
        val unsignedEvent = runCatching {
            EventBuilder.auth(challenge = challenge, relayUrl = relayUrl)
                .toUnsignedEvent(publicKey = accountManager.getPublicKey())
        }

        return if (unsignedEvent.isSuccess) {
            accountManager.sign(
                signerLauncher = signerLauncher,
                unsignedEvent = unsignedEvent.getOrThrow()
            )
        } else {
            Log.w(TAG, "EventBuilder.auth for $relayUrl failed")
            Result.failure(
                unsignedEvent.exceptionOrNull() ?: IllegalStateException("EventBuilder.auth failed")
            )
        }
    }
}
