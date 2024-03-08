package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import com.dluvian.voyage.core.EventIdHex

@Entity(
    tableName = "hashtag",
    primaryKeys = ["postId", "hashtag"],
    foreignKeys = [ForeignKey(
        entity = PostEntity::class,
        parentColumns = ["id"],
        childColumns = ["postId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )]
)
data class HashtagEntity(
    val postId: EventIdHex,
    val hashtag: String,
)
