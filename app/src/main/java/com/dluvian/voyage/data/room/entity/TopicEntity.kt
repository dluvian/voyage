package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import com.dluvian.voyage.core.PubkeyHex


@Entity(
    tableName = "topic",
    primaryKeys = ["topic"],
    foreignKeys = [ForeignKey(
        entity = AccountEntity::class,
        parentColumns = ["pubkey"],
        childColumns = ["myPubkey"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )]
)
data class TopicEntity(
    val myPubkey: PubkeyHex,
    val topic: String,
    val createdAt: Long,
)
