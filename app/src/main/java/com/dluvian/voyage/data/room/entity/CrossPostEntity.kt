package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.Index
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedCrossPost

@Entity(
    tableName = "crossPost",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["crossPostedId"], unique = false),
        Index(value = ["pubkey"], unique = false),
        Index(value = ["createdAt"], unique = false),
    ],
)
data class CrossPostEntity(
    val id: EventIdHex,
    val crossPostedId: EventIdHex,
    val pubkey: PubkeyHex,
    val createdAt: Long,
) {
    companion object {
        fun from(validatedCrossPost: ValidatedCrossPost): CrossPostEntity {
            return CrossPostEntity(
                id = validatedCrossPost.id,
                crossPostedId = validatedCrossPost.crossPostedId,
                pubkey = validatedCrossPost.pubkey,
                createdAt = validatedCrossPost.createdAt,
            )
        }
    }
}
