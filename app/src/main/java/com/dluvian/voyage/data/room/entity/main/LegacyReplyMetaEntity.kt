package com.dluvian.voyage.data.room.entity.main

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.dluvian.voyage.core.EventIdHex

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
data class LegacyReplyMetaEntity(
    val eventId: EventIdHex,
    val parentId: EventIdHex,
)
