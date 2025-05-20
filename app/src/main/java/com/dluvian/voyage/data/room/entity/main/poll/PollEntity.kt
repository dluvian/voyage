package com.dluvian.voyage.data.room.entity.main.poll

import androidx.room.Entity
import androidx.room.ForeignKey
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.event.ValidatedPoll
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.data.room.entity.main.MainEventEntity

@Entity(
    tableName = "poll",
    primaryKeys = ["eventId"],
    foreignKeys = [ForeignKey(
        entity = MainEventEntity::class,
        parentColumns = ["id"],
        childColumns = ["eventId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )],
)
data class PollEntity(
    val eventId: EventIdHex,
    val relay1: RelayUrl?,
    val relay2: RelayUrl?,
    val endsAt: Long?,
) {
    companion object {
        fun from(poll: ValidatedPoll): PollEntity {
            return PollEntity(
                eventId = poll.id,
                relay1 = poll.relays.getOrNull(0),
                relay2 = poll.relays.getOrNull(1),
                endsAt = poll.endsAt
            )
        }
    }
}
