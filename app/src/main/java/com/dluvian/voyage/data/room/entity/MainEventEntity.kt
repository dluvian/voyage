package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.Index
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedCrossPost
import com.dluvian.voyage.data.event.ValidatedLegacyReply
import com.dluvian.voyage.data.event.ValidatedMainEvent
import com.dluvian.voyage.data.event.ValidatedRootPost
import com.dluvian.voyage.data.nostr.RelayUrl

@Entity(
    tableName = "mainEvent",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["pubkey"], unique = false),
        Index(value = ["kind"], unique = false),
        Index(value = ["createdAt"], unique = false),
        Index(value = ["refId"], unique = false),
        Index(value = ["isMentioningMe"], unique = false),
    ],
)
data class MainEventEntity(
    val id: EventIdHex,
    val pubkey: PubkeyHex,
    val kind: Int,
    val createdAt: Long,
    val relayUrl: RelayUrl,
    val refId: EventIdHex?, // ParentId for replies, crossPostedId for cross-posts
    val subject: String?,
    val content: String?,
    val json: String?,
    val isMentioningMe: Boolean,
) {
    companion object {

        fun from(mainEvent: ValidatedMainEvent): MainEventEntity {
            return when (mainEvent) {
                is ValidatedRootPost -> MainEventEntity(
                    id = mainEvent.id,
                    pubkey = mainEvent.pubkey,
                    kind = mainEvent.kind,
                    createdAt = mainEvent.createdAt,
                    relayUrl = mainEvent.relayUrl,
                    refId = null,
                    subject = mainEvent.subject,
                    content = mainEvent.content,
                    json = mainEvent.json,
                    isMentioningMe = mainEvent.isMentioningMe
                )

                is ValidatedLegacyReply -> MainEventEntity(
                    id = mainEvent.id,
                    pubkey = mainEvent.pubkey,
                    kind = mainEvent.kind,
                    createdAt = mainEvent.createdAt,
                    relayUrl = mainEvent.relayUrl,
                    refId = mainEvent.parentId,
                    subject = null,
                    content = mainEvent.content,
                    json = mainEvent.json,
                    isMentioningMe = mainEvent.isMentioningMe
                )

                is ValidatedCrossPost -> MainEventEntity(
                    id = mainEvent.id,
                    pubkey = mainEvent.pubkey,
                    kind = mainEvent.kind,
                    createdAt = mainEvent.createdAt,
                    relayUrl = mainEvent.relayUrl,
                    refId = mainEvent.crossPostedId,
                    subject = null,
                    content = null,
                    json = null,
                    isMentioningMe = false
                )
            }
        }
    }
}
