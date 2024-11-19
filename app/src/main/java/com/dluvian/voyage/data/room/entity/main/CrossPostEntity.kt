package com.dluvian.voyage.data.room.entity.main

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.event.ValidatedCrossPost

@Entity(
    tableName = "crossPost",
    primaryKeys = ["eventId"],
    foreignKeys = [ForeignKey(
        entity = MainEventEntity::class,
        parentColumns = ["id"],
        childColumns = ["eventId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )],
    indices = [
        Index(value = ["crossPostedId"], unique = false),
    ],
)
data class CrossPostEntity(
    val eventId: EventIdHex,
    val crossPostedId: EventIdHex,
    @ColumnInfo(defaultValue = "1")
    val crossPostedKind: Int,
) {
    companion object {
        fun from(crossPost: ValidatedCrossPost): CrossPostEntity {
            return CrossPostEntity(
                eventId = crossPost.id,
                crossPostedId = crossPost.crossPostedId,
                crossPostedKind = crossPost.crossPostedKind.toInt(),
            )
        }
    }
}
