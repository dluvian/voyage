package com.dluvian.voyage.data.room.entity.main.poll

import androidx.room.Entity
import androidx.room.ForeignKey
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedPollResponse

@Entity(
    tableName = "pollResponse",
    primaryKeys = ["pollId", "pubkey"],
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
) {
    companion object {
        fun from(response: ValidatedPollResponse): PollResponseEntity {
            return PollResponseEntity(
                pollId = response.pollId,
                optionId = response.optionId,
                pubkey = response.pubkey,
            )
        }
    }
}
