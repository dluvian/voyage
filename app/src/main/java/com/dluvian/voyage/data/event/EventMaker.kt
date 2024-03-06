package com.dluvian.voyage.data.event

import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.createHashtagTag
import com.dluvian.nostr_kt.createKindTag
import com.dluvian.nostr_kt.createLabelTag
import com.dluvian.nostr_kt.createReplyTag
import com.dluvian.nostr_kt.createTitleTag
import com.dluvian.voyage.data.keys.AccountKeyManager
import com.dluvian.voyage.data.keys.SingleUseKeyManager
import com.dluvian.voyage.data.model.EventSubset
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventBuilder
import rust.nostr.protocol.EventId
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.Tag
import rust.nostr.protocol.Timestamp

private const val POST_LABEL = "post"
private const val REPLY_LABEL = "reply"

class EventMaker(
    private val singleUseKeyManager: SingleUseKeyManager,
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
            .toEvent(keys = singleUseKeyManager.getPostingKeys(timestamp))
    }

    fun buildReply(
        rootEvent: EventSubset,
        parentEvent: EventSubset,
        relayHint: RelayUrl,
        content: String
    ): Event {
        val timestamp = Timestamp.now()
        val tags = listOf(
            createLabelTag(REPLY_LABEL),
            createReplyTag(
                parentEvent.id,
                relayHint,
                parentIsRoot = rootEvent.id.toHex() == parentEvent.id.toHex()
            ),
            Tag.publicKey(parentEvent.pubkey)
        )
        return EventBuilder.textNote(content, tags)
            .customCreatedAt(timestamp)
            .toEvent(keys = singleUseKeyManager.getReplySectionKeys(rootEvent))
    }

    fun buildVote(eventId: EventId, pubkey: PublicKey, isPositive: Boolean, kind: Int): Event {
        val tags = listOf(createKindTag(kind)) // TODO: Set kind tags
        val content = if (isPositive) "+" else "-"
        val unsignedEvent = EventBuilder.reaction(eventId, pubkey, content)
            .toUnsignedEvent(accountKeyManager.getPubkey())

        return accountKeyManager.sign(unsignedEvent)
    }
}