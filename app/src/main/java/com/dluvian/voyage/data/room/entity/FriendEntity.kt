package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import com.dluvian.voyage.core.PubkeyHex


@Entity(
    tableName = "friend",
    primaryKeys = ["friendPubkey"],
    foreignKeys = [ForeignKey(
        entity = AccountEntity::class,
        parentColumns = ["pubkey"],
        childColumns = ["myPubkey"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )]
)
data class FriendEntity(
    val myPubkey: PubkeyHex,
    val friendPubkey: PubkeyHex,
    val createdAt: Long,
)
