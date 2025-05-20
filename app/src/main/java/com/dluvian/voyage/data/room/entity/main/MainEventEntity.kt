package com.dluvian.voyage.data.room.entity.main

import androidx.room.Entity
import androidx.room.Index
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedComment
import com.dluvian.voyage.data.event.ValidatedCrossPost
import com.dluvian.voyage.data.event.ValidatedLegacyReply
import com.dluvian.voyage.data.event.ValidatedMainEvent
import com.dluvian.voyage.data.event.ValidatedPoll
import com.dluvian.voyage.data.event.ValidatedRootPost
import com.dluvian.voyage.data.nostr.RelayUrl

@Entity(
    tableName = "mainEvent",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["pubkey"], unique = false),
        Index(value = ["createdAt"], unique = false),
        Index(value = ["isMentioningMe"], unique = false),
    ],
)
data class MainEventEntity(
    val id: EventIdHex,
    val pubkey: PubkeyHex,
    val createdAt: Long,
    val content: String,
    val relayUrl: RelayUrl,
    val isMentioningMe: Boolean,
    val json: String?,
) {
    companion object {
        fun from(mainEvent: ValidatedMainEvent): MainEventEntity {
            return MainEventEntity(
                id = mainEvent.id,
                pubkey = mainEvent.pubkey,
                createdAt = mainEvent.createdAt,
                content = when (mainEvent) {
                    is ValidatedRootPost -> mainEvent.content
                    is ValidatedLegacyReply -> mainEvent.content
                    is ValidatedComment -> mainEvent.content
                    is ValidatedPoll -> mainEvent.content
                    is ValidatedCrossPost -> ""
                },
                relayUrl = mainEvent.relayUrl,
                isMentioningMe = when (mainEvent) {
                    is ValidatedRootPost -> mainEvent.isMentioningMe
                    is ValidatedLegacyReply -> mainEvent.isMentioningMe
                    is ValidatedComment -> mainEvent.isMentioningMe
                    is ValidatedPoll -> mainEvent.isMentioningMe
                    is ValidatedCrossPost -> false
                },
                json = when (mainEvent) {
                    is ValidatedRootPost -> mainEvent.json
                    is ValidatedLegacyReply -> mainEvent.json
                    is ValidatedComment -> mainEvent.json
                    is ValidatedPoll -> mainEvent.json
                    is ValidatedCrossPost -> null
                },
            )
        }
    }
}
