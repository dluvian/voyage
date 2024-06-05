package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedVote

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
    val createdAt: Long
) {
    companion object {
        fun from(validatedVote: ValidatedVote): VoteEntity {
            return VoteEntity(
                id = validatedVote.id,
                postId = validatedVote.postId,
                pubkey = validatedVote.pubkey,
                createdAt = validatedVote.createdAt,
            )
        }
    }
}
