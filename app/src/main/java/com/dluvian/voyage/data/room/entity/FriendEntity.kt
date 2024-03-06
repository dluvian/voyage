package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.model.ValidatedContactList


@Entity(
    tableName = "friend",
    primaryKeys = ["friendPubkey"],
    foreignKeys = [ForeignKey(
        entity = AccountEntity::class,
        parentColumns = ["pubkey"],
        childColumns = ["myPubkey"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )],
    indices = [Index(value = ["myPubkey"], unique = false)], // ksp suggestion: "Highly advised"
)
data class FriendEntity(
    val myPubkey: PubkeyHex,
    val friendPubkey: PubkeyHex,
    val createdAt: Long,
) {
    companion object {
        fun from(validatedContactList: ValidatedContactList): List<FriendEntity> {
            val myPubkey = validatedContactList.pubkey.toHex()
            return validatedContactList.friendPubkeys.map { friendPubkey ->
                FriendEntity(
                    myPubkey = myPubkey,
                    friendPubkey = friendPubkey.toHex(),
                    createdAt = validatedContactList.createdAt
                )
            }
        }
    }
}
