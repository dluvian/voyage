package com.dluvian.voyage.data.room.entity.main.poll

import androidx.room.Entity
import androidx.room.ForeignKey
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex

@Entity(
    tableName = "pollResponse",
    primaryKeys = ["pollId", "optionId", "pubkey"],
    foreignKeys = [
        ForeignKey(
            entity = PollOptionEntity::class,
            parentColumns = ["pollId"],
            childColumns = ["pollId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION
        ),
    ],
)
data class PollResponseEntity(
    val pollId: EventIdHex,
    val optionId: String,
    val pubkey: PubkeyHex,
    val createdAt: Long,
)
