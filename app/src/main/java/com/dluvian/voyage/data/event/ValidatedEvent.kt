package com.dluvian.voyage.data.event

import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.nostr.Nip65Relay
import com.dluvian.voyage.data.nostr.RelayUrl
import rust.nostr.sdk.Metadata

sealed class ValidatedEvent

sealed class ValidatedMainEvent(
    open val id: EventIdHex,
    open val pubkey: PubkeyHex,
    open val createdAt: Long,
    open val relayUrl: RelayUrl,
) : ValidatedEvent()

sealed class ValidatedThreadableEvent(
    override val id: EventIdHex,
    override val pubkey: PubkeyHex,
    override val createdAt: Long,
    override val relayUrl: RelayUrl,
    open val content: String,
    open val json: String,
    open val isMentioningMe: Boolean,
) : ValidatedMainEvent(
    id = id,
    pubkey = pubkey,
    createdAt = createdAt,
    relayUrl = relayUrl,
)

sealed class ValidatedTextNote(
    override val id: EventIdHex,
    override val pubkey: PubkeyHex,
    override val createdAt: Long,
    override val relayUrl: RelayUrl,
    override val content: String,
    override val json: String,
    override val isMentioningMe: Boolean,
) : ValidatedThreadableEvent(
    id = id,
    pubkey = pubkey,
    createdAt = createdAt,
    relayUrl = relayUrl,
    content = content,
    json = json,
    isMentioningMe = isMentioningMe
)

data class ValidatedRootPost(
    override val id: EventIdHex,
    override val pubkey: PubkeyHex,
    override val createdAt: Long,
    override val relayUrl: RelayUrl,
    override val content: String,
    override val json: String,
    override val isMentioningMe: Boolean,
    val topics: List<String>,
) : ValidatedTextNote(
    id = id,
    pubkey = pubkey,
    createdAt = createdAt,
    relayUrl = relayUrl,
    content = content,
    json = json,
    isMentioningMe = isMentioningMe
)

data class ValidatedLegacyReply(
    override val id: EventIdHex,
    override val pubkey: PubkeyHex,
    override val createdAt: Long,
    override val relayUrl: RelayUrl,
    override val content: String,
    override val json: String,
    override val isMentioningMe: Boolean,
    val parentId: EventIdHex,
) : ValidatedTextNote(
    id = id,
    pubkey = pubkey,
    createdAt = createdAt,
    relayUrl = relayUrl,
    content = content,
    json = json,
    isMentioningMe = isMentioningMe
)

data class ValidatedComment(
    override val id: EventIdHex,
    override val pubkey: PubkeyHex,
    override val createdAt: Long,
    override val relayUrl: RelayUrl,
    override val content: String,
    override val json: String,
    override val isMentioningMe: Boolean,
    val parentId: EventIdHex?,
    val parentKind: UShort?,
) : ValidatedThreadableEvent(
    id = id,
    pubkey = pubkey,
    createdAt = createdAt,
    relayUrl = relayUrl,
    content = content,
    json = json,
    isMentioningMe = isMentioningMe
)

data class ValidatedCrossPost(
    override val id: EventIdHex,
    override val pubkey: PubkeyHex,
    override val createdAt: Long,
    override val relayUrl: RelayUrl,
    val topics: List<String>,
    val crossPostedId: EventIdHex,
    val crossPostedThreadableEvent: ValidatedThreadableEvent?
) : ValidatedMainEvent(
    id = id,
    pubkey = pubkey,
    createdAt = createdAt,
    relayUrl = relayUrl,
)

data class ValidatedVote(
    val id: EventIdHex,
    val eventId: EventIdHex,
    val pubkey: PubkeyHex,
    val createdAt: Long
) : ValidatedEvent()

data class ValidatedProfile(
    val id: EventIdHex,
    val pubkey: PubkeyHex,
    val metadata: Metadata,
    val createdAt: Long
) : ValidatedEvent()

sealed class ValidatedList(val owner: PubkeyHex, open val createdAt: Long) : ValidatedEvent()
data class ValidatedContactList(
    val pubkey: PubkeyHex,
    val friendPubkeys: Set<PubkeyHex>,
    override val createdAt: Long
) : ValidatedList(owner = pubkey, createdAt = createdAt)

data class ValidatedTopicList(
    val myPubkey: PubkeyHex,
    val topics: Set<Topic>,
    override val createdAt: Long
) : ValidatedList(owner = myPubkey, createdAt = createdAt)

data class ValidatedNip65(
    val pubkey: PubkeyHex,
    val relays: List<Nip65Relay>,
    override val createdAt: Long
) : ValidatedList(owner = pubkey, createdAt = createdAt)

data class ValidatedBookmarkList(
    val myPubkey: PubkeyHex,
    val eventIds: Set<EventIdHex>,
    override val createdAt: Long
) : ValidatedList(owner = myPubkey, createdAt = createdAt)

sealed class ValidatedSet(
    open val identifier: String,
    open val createdAt: Long
) : ValidatedEvent()

data class ValidatedProfileSet(
    override val identifier: String,
    val myPubkey: PubkeyHex,
    val title: String,
    val description: String,
    val pubkeys: Set<PubkeyHex>,
    override val createdAt: Long
) : ValidatedSet(
    identifier = identifier,
    createdAt = createdAt
)

data class ValidatedTopicSet(
    override val identifier: String,
    val myPubkey: PubkeyHex,
    val title: String,
    val description: String,
    val topics: Set<Topic>,
    override val createdAt: Long
) : ValidatedSet(
    identifier = identifier,
    createdAt = createdAt
)

