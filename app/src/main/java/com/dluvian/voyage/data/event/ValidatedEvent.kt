package com.dluvian.voyage.data.event

import com.dluvian.nostr_kt.Nip65Relay
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import rust.nostr.protocol.Metadata

sealed class ValidatedEvent

sealed class ValidatedPost(
    open val id: EventIdHex,
    open val pubkey: PubkeyHex,
    open val topics: List<Topic>
) : ValidatedEvent()

sealed class ValidatedMainPost(
    override val id: EventIdHex,
    override val pubkey: PubkeyHex,
    override val topics: List<Topic>,
    open val subject: String?,
) : ValidatedPost(id = id, pubkey = pubkey, topics = topics)

data class ValidatedRootPost(
    override val id: EventIdHex,
    override val pubkey: PubkeyHex,
    override val topics: List<String>,
    override val subject: String?,
    val content: String,
    val createdAt: Long,
    val relayUrl: RelayUrl,
    val json: String,
) : ValidatedMainPost(id = id, pubkey = pubkey, subject = subject, topics = topics)

data class ValidatedReply(
    override val id: EventIdHex,
    override val pubkey: PubkeyHex,
    val parentId: EventIdHex,
    val content: String,
    val createdAt: Long,
    val relayUrl: RelayUrl,
    val json: String,
) : ValidatedMainPost(id = id, pubkey = pubkey, subject = null, topics = emptyList())

data class ValidatedCrossPost(
    override val id: EventIdHex,
    override val pubkey: PubkeyHex,
    override val topics: List<String>,
    val createdAt: Long,
    val relayUrl: RelayUrl,
    val crossPosted: ValidatedMainPost,
) : ValidatedPost(id = id, pubkey = pubkey, topics = topics)

data class ValidatedVote(
    val id: EventIdHex,
    val postId: EventIdHex,
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

data class ValidatedBookmarkList(
    val myPubkey: PubkeyHex,
    val postIds: Set<EventIdHex>,
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
    val topics: Set<Topic>,
    override val createdAt: Long
) : ValidatedSet(
    identifier = identifier,
    createdAt = createdAt
)


data class ValidatedNip65(
    val pubkey: PubkeyHex,
    val relays: List<Nip65Relay>,
    override val createdAt: Long
) : ValidatedList(owner = pubkey, createdAt = createdAt)
