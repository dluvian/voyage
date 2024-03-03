package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "post",
    foreignKeys = [ForeignKey(
        entity = PostEntity::class,
        parentColumns = ["id"],
        childColumns = ["replyToId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )]
)
data class PostEntity(
    @PrimaryKey val id: String,
    val pubkey: String,
    val replyToId: String?,
    val replyRelayHint: String?,
    val title: String,
    val content: String,
    val createdAt: Long,
)
