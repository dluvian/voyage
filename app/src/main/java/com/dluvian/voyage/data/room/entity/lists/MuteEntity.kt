package com.dluvian.voyage.data.room.entity.lists

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedMuteList
import com.dluvian.voyage.data.room.entity.AccountEntity

@Entity(
    tableName = "mute",
    primaryKeys = ["mutedItem", "tag"],
    foreignKeys = [ForeignKey(
        entity = AccountEntity::class,
        parentColumns = ["pubkey"],
        childColumns = ["myPubkey"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )],
    indices = [Index(value = ["myPubkey"], unique = false)], // ksp suggestion: "Highly advised"
)
data class MuteEntity(
    val myPubkey: PubkeyHex,
    val mutedItem: String,
    val tag: String,
    val createdAt: Long,
) {
    companion object {
        fun from(muteList: ValidatedMuteList): List<MuteEntity> {
            return muteList.pubkeys.map { pubkey ->
                MuteEntity(
                    myPubkey = muteList.myPubkey,
                    mutedItem = pubkey,
                    tag = "p",
                    createdAt = muteList.createdAt
                )
            } + muteList.topics.map { topic ->
                MuteEntity(
                    myPubkey = muteList.myPubkey,
                    mutedItem = topic,
                    tag = "t",
                    createdAt = muteList.createdAt
                )
            } + muteList.words.map { word ->
                MuteEntity(
                    myPubkey = muteList.myPubkey,
                    mutedItem = word,
                    tag = "word",
                    createdAt = muteList.createdAt
                )
            }
        }
    }
}
