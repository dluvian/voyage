package com.dluvian.voyage.data.event

import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.createHashtagTag
import com.dluvian.nostr_kt.createLabelTag
import com.dluvian.nostr_kt.createReplyTag
import com.dluvian.nostr_kt.createTitleTag
import com.dluvian.nostr_kt.secs
import com.dluvian.voyage.data.keys.AccountKeyManager
import com.dluvian.voyage.data.keys.MnemonicManager
import com.dluvian.voyage.data.model.EventIdAndPubkey
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventBuilder
import rust.nostr.protocol.EventId
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.Tag
import rust.nostr.protocol.Timestamp

private const val POST_LABEL = "post"
private const val REPLY_LABEL = "reply"

class EventMaker(
    private val singleUseKeyManager: MnemonicManager,
    private val accountKeyManager: AccountKeyManager,
) {
    fun buildPost(title: String, content: String, topic: String): Event {
        val timestamp = Timestamp.now()
        val tags = listOf(
            createLabelTag(POST_LABEL),
            createTitleTag(title),
            createHashtagTag(topic)
        )
        return EventBuilder.textNote(content, tags)
            .customCreatedAt(timestamp)
            .toEvent(keys = singleUseKeyManager.getPostingKeys(timestamp.secs()))
    }

    fun buildReply(
        rootId: EventId,
        rootCreatedAt: Long,
        parentEvent: EventIdAndPubkey,
        relayHint: RelayUrl,
        content: String
    ): Event {
        val timestamp = Timestamp.now()
        val tags = listOf(
            createLabelTag(label = REPLY_LABEL),
            createReplyTag(
                parentEventId = parentEvent.id,
                relayHint = relayHint,
                parentIsRoot = rootId.toHex() == parentEvent.id.toHex()
            ),
            Tag.publicKey(publicKey = parentEvent.pubkey)
        )
        return EventBuilder.textNote(content = content, tags = tags)
            .customCreatedAt(createdAt = timestamp)
            .toEvent(
                keys = singleUseKeyManager.getReplySectionKeys(
                    rootId = rootId,
                    rootCreatedAt = rootCreatedAt
                )
            )
    }

    fun buildVote(
        eventId: EventId,
        pubkey: PublicKey,
        isPositive: Boolean,
    ): Result<Event> {
        val content = if (isPositive) "+" else "-"
        val unsignedEvent = EventBuilder.reaction(eventId, pubkey, content)
            .toUnsignedEvent(accountKeyManager.getPubkey())

        return accountKeyManager.sign(unsignedEvent)
    }

    fun buildDelete(eventId: EventId): Result<Event> {
        val unsignedEvent = EventBuilder.delete(ids = listOf(eventId), reason = null)
            .toUnsignedEvent(accountKeyManager.getPubkey())

        return accountKeyManager.sign(unsignedEvent)
    }
}
