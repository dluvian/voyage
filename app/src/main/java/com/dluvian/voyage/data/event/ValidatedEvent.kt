package com.dluvian.voyage.data.event

import com.dluvian.nostr_kt.Nip65Relay
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import rust.nostr.protocol.Metadata

sealed class ValidatedEvent

sealed class ValidatedPost(open val id: EventIdHex, open val topics: List<String>) :
    ValidatedEvent()
data class ValidatedRootPost(
    override val id: EventIdHex,
    val pubkey: PubkeyHex,
    override val topics: List<String>,
    val title: String?,
    val content: String,
    val createdAt: Long
) : ValidatedPost(id = id, topics = topics)

data class ValidatedComment(
    override val id: EventIdHex,
    val pubkey: PubkeyHex,
    val parentId: EventIdHex,
    val content: String,
    val createdAt: Long
) : ValidatedPost(id = id, topics = emptyList())

data class ValidatedVote(
    val id: EventIdHex,
    val postId: EventIdHex,
    val pubkey: PubkeyHex,
    val isPositive: Boolean,
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
