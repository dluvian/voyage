package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.dluvian.voyage.core.PubkeyHex

@Entity(
    tableName = "weboftrust",
    primaryKeys = ["webOfTrustPubkey"],
    foreignKeys = [ForeignKey(
        entity = FriendEntity::class,
        parentColumns = ["friendPubkey"],
        childColumns = ["friendPubkey"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )],
    indices = [Index(value = ["friendPubkey"], unique = false)], // ksp suggestion: "Highly advised"
)
data class WebOfTrustEntity(
    val friendPubkey: PubkeyHex,
    val webOfTrustPubkey: PubkeyHex,
    val createdAt: Long
)
