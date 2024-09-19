package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedBookmarkList

@Entity(
    tableName = "bookmark",
    primaryKeys = ["eventId"],
    foreignKeys = [ForeignKey(
        entity = AccountEntity::class,
        parentColumns = ["pubkey"],
        childColumns = ["myPubkey"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )],
    indices = [Index(value = ["myPubkey"], unique = false)], // ksp suggestion: "Highly advised"
)
data class BookmarkEntity(
    val myPubkey: PubkeyHex,
    val eventId: EventIdHex,
    val createdAt: Long,
) {
    companion object {
        fun from(validatedBookmarkList: ValidatedBookmarkList): List<BookmarkEntity> {
            return validatedBookmarkList.eventIds.map { eventId ->
                BookmarkEntity(
                    myPubkey = validatedBookmarkList.myPubkey,
                    eventId = eventId,
                    createdAt = validatedBookmarkList.createdAt
                )
            }
        }
    }
}
