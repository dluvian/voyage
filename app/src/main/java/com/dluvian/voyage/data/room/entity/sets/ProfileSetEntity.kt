package com.dluvian.voyage.data.room.entity.sets

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedProfileSet
import com.dluvian.voyage.data.room.entity.AccountEntity

@Entity(
    tableName = "profileSet",
    primaryKeys = ["identifier"],
    foreignKeys = [ForeignKey(
        entity = AccountEntity::class,
        parentColumns = ["pubkey"],
        childColumns = ["myPubkey"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )],
    indices = [Index(value = ["myPubkey"], unique = false)], // ksp suggestion: "Highly advised"
)
data class ProfileSetEntity(
    val identifier: String,
    val myPubkey: PubkeyHex,
    val title: String,
    val createdAt: Long,
) {
    companion object {
        fun from(set: ValidatedProfileSet): ProfileSetEntity {
            return ProfileSetEntity(
                identifier = set.identifier,
                myPubkey = set.myPubkey,
                title = set.title,
                createdAt = set.createdAt
            )
        }
    }
}
