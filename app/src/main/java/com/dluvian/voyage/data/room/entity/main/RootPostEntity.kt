package com.dluvian.voyage.data.room.entity.main

import androidx.room.Entity
import androidx.room.ForeignKey
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.event.ValidatedRootPost

@Entity(
    tableName = "rootPost",
    primaryKeys = ["eventId"],
    foreignKeys = [ForeignKey(
        entity = MainEventEntity::class,
        parentColumns = ["id"],
        childColumns = ["eventId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )],
)
data class RootPostEntity(
    val eventId: EventIdHex,
    val subject: String,
) {
    companion object {
        fun from(rootPost: ValidatedRootPost): RootPostEntity {
            return RootPostEntity(
                eventId = rootPost.id,
                subject = rootPost.subject
            )
        }
    }
}
