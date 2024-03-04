package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex

@Entity(
    tableName = "vote",
    primaryKeys = ["postId", "pubkey"],
    foreignKeys = [ForeignKey(
        entity = PostEntity::class,
        parentColumns = ["id"],
        childColumns = ["postId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )]
)
data class VoteEntity(
    val id: EventIdHex,
    val postId: EventIdHex,
    val pubkey: PubkeyHex,
    val isPositive: Boolean,
    val createdAt: Long
)
