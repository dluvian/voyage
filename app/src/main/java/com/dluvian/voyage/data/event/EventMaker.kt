package com.dluvian.voyage.data.event

import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.createHashtagTag
import com.dluvian.nostr_kt.createLabelTag
import com.dluvian.nostr_kt.createReplyTag
import com.dluvian.nostr_kt.createTitleTag
import com.dluvian.voyage.data.model.EventIdAndPubkey
import com.dluvian.voyage.data.signer.AccountManager
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventBuilder
import rust.nostr.protocol.EventId
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.Tag

private const val POST_LABEL = "post"
private const val REPLY_LABEL = "reply"

class EventMaker(
    private val accountManager: AccountManager,
) {
    fun buildPost(title: String, content: String, topic: String): Result<Event> {
        val tags = listOf(
            createLabelTag(POST_LABEL),
            createTitleTag(title),
            createHashtagTag(topic)
        )
        val publicKey = accountManager.getPublicKey()
        val unsignedEvent = EventBuilder.textNote(content, tags).toUnsignedEvent(publicKey)

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    fun buildReply(
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

    fun buildVote(
        eventId: EventId,
        pubkey: PublicKey,
        isPositive: Boolean,
        kind: Int,
    ): Result<Event> {
        // TODO: set kind tag
        val content = if (isPositive) "+" else "-"
        val unsignedEvent = EventBuilder.reaction(
            eventId = eventId,
            publicKey = pubkey,
            content = content
        )
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent)
    }

    fun buildDelete(eventId: EventId): Result<Event> {
        val unsignedEvent = EventBuilder.delete(ids = listOf(eventId), reason = null)
            .toUnsignedEvent(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent)
    }
}
