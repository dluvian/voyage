package com.dluvian.voyage.data.event

import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.createHashtagTag
import com.dluvian.nostr_kt.createLabelTag
import com.dluvian.nostr_kt.createMentionTag
import com.dluvian.nostr_kt.createReaction
import com.dluvian.nostr_kt.createReplyTag
import com.dluvian.nostr_kt.createTitleTag
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.account.AccountManager
import com.dluvian.voyage.data.model.EventIdAndPubkey
import rust.nostr.protocol.Contact
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventBuilder
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Interests
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.Tag

private const val ROOT_LABEL = "root"
private const val REPLY_LABEL = "reply"

class EventMaker(
    private val accountManager: AccountManager,
) {
    suspend fun buildPost(
        title: String,
        content: String,
        topics: List<Topic>,
        mentions: List<PubkeyHex>
    ): Result<Event> {
        val tags = mutableListOf(createLabelTag(ROOT_LABEL))
        topics.forEach { tags.add(createHashtagTag(it)) }
        if (title.isNotEmpty()) tags.add(createTitleTag(title = title))
        if (mentions.isNotEmpty()) tags.add(createMentionTag(pubkeys = mentions))

        val publicKey = accountManager.getPublicKey()
        val unsignedEvent = EventBuilder.textNote(content, tags).toUnsignedEvent(publicKey)

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildReply(
        rootId: EventId,
        parentEvent: EventIdAndPubkey,
        relayHint: RelayUrl,
        content: String
    ): Result<Event> {
        val tags = listOf(
            createLabelTag(label = REPLY_LABEL),
            createReplyTag(
                parentEventId = parentEvent.id,
                relayHint = relayHint,
                parentIsRoot = rootId.toHex() == parentEvent.id.toHex()
            ),
            Tag.publicKey(publicKey = parentEvent.pubkey)
        )

        val publicKey = accountManager.getPublicKey()
        val unsignedEvent = EventBuilder.textNote(content, tags).toUnsignedEvent(publicKey)

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildVote(
        eventId: EventId,
        pubkey: PublicKey,
        isPositive: Boolean,
        kind: Int,
    ): Result<Event> {
        val unsignedEvent = createReaction(
            eventId = eventId,
            pubkey = pubkey,
            content = if (isPositive) "+" else "-",
            kind = kind
        )
        return accountManager.sign(unsignedEvent)
    }

    suspend fun buildDelete(eventId: EventId): Result<Event> {
        val unsignedEvent = EventBuilder.delete(ids = listOf(eventId), reason = null)
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent)
    }

    suspend fun buildTopicList(topics: List<Topic>): Result<Event> {
        val interests = Interests(hashtags = topics, coordinate = emptyList())
        val unsignedEvent = EventBuilder.interests(list = interests)
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent)
    }

    suspend fun buildContactList(pubkeys: List<PubkeyHex>): Result<Event> {
        val contacts = pubkeys
            .map { Contact(pk = PublicKey.fromHex(it), relayUrl = null, alias = null) }
        val unsignedEvent = EventBuilder.contactList(list = contacts)
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent)
    }
}
