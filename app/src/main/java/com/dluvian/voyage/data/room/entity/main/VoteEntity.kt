package com.dluvian.voyage.data.room.entity.main

import androidx.room.Entity
import androidx.room.ForeignKey
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedVote

@Entity(
    tableName = "vote",
    primaryKeys = ["eventId", "pubkey"],
    foreignKeys = [ForeignKey(
        entity = MainEventEntity::class,
        parentColumns = ["id"],
        childColumns = ["eventId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )],
)
data class VoteEntity(
    val id: EventIdHex,
    val eventId: EventIdHex,
    val pubkey: PubkeyHex,
    val createdAt: Long
) {
    companion object {
        fun from(validatedVote: ValidatedVote): VoteEntity {
            return VoteEntity(
                id = validatedVote.id,
                eventId = validatedVote.eventId,
                pubkey = validatedVote.pubkey,
                createdAt = validatedVote.createdAt,
            )
        }
    }
}
