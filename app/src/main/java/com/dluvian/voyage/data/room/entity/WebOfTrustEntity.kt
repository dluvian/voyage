package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import com.dluvian.voyage.core.PubkeyHex

@Entity(
    tableName = "weboftrust",
    primaryKeys = ["friendPubkey", "webOfTrustPubkey"],
    foreignKeys = [ForeignKey(
        entity = FriendEntity::class,
        parentColumns = ["friendPubkey"],
        childColumns = ["friendPubkey"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )]
)
data class WebOfTrustEntity(
    val friendPubkey: PubkeyHex,
    val webOfTrustPubkey: PubkeyHex,
    val createdAt: Long
)
