package com.dluvian.voyage.data.room.entity.main

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.event.ValidatedLegacyReply

@Entity(
    tableName = "legacyReply",
    primaryKeys = ["eventId"],
    foreignKeys = [ForeignKey(
        entity = MainEventEntity::class,
        parentColumns = ["id"],
        childColumns = ["eventId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )],
    indices = [
        Index(value = ["parentId"], unique = false),
    ],
)
data class LegacyReplyEntity(
    val eventId: EventIdHex,
    val parentId: EventIdHex,
) {
    companion object {
        fun from(legacyReply: ValidatedLegacyReply): LegacyReplyEntity {
            return LegacyReplyEntity(
                eventId = legacyReply.id,
                parentId = legacyReply.parentId
            )
        }
    }
}
