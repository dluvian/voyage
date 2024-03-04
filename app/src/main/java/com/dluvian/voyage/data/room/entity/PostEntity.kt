package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex

@Entity(
    tableName = "post",
    // ksp suggestion bc it's refrenced by post.replyToId and vote.postId
    indices = [Index(value = ["id"], unique = true)],
    primaryKeys = ["id"],
    foreignKeys = [ForeignKey(
        entity = PostEntity::class,
        parentColumns = ["id"],
        childColumns = ["replyToId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )]
)
data class PostEntity(
    val id: EventIdHex,
    val pubkey: PubkeyHex,
    val replyToId: EventIdHex?,
    val replyRelayHint: RelayUrl?,
    val topic: String,
    val title: String,
    val content: String,
    val createdAt: Long,
)
