package com.dluvian.voyage.data.room.entity.main

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.event.ValidatedComment

@Entity(
    tableName = "comment",
    primaryKeys = ["eventId"],
    foreignKeys = [ForeignKey(
        entity = MainEventEntity::class,
        parentColumns = ["id"],
        childColumns = ["eventId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )],
    indices = [
        Index(value = ["parentRef"], unique = false),
        Index(value = ["parentKind"], unique = false),
    ],
)
data class CommentMetaEntity(
    val eventId: EventIdHex,
    val parentRef: String,
    val parentKind: Int,
) {
    companion object {
        fun from(comment: ValidatedComment): CommentMetaEntity {
            return CommentMetaEntity(
                eventId = comment.id,
                parentRef = comment.parentRef,
                parentKind = comment.parentKind
            )
        }
    }
}
