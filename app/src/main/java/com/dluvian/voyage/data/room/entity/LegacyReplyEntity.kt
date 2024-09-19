package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.Index
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedLegacyReply
import com.dluvian.voyage.data.nostr.RelayUrl

@Entity(
    tableName = "legacyReply",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["parentId"], unique = false),
        Index(value = ["pubkey"], unique = false),
        Index(value = ["createdAt"], unique = false),
        Index(value = ["isMentioningMe"], unique = false),
    ],
)
data class LegacyReplyEntity(
    val id: EventIdHex,
    val parentId: EventIdHex,
    val pubkey: PubkeyHex,
    val createdAt: Long,
    val relayUrl: RelayUrl,
    val content: String,
    val json: String,
    val isMentioningMe: Boolean,
) {
    companion object {
        fun from(validatedLegacyReply: ValidatedLegacyReply): LegacyReplyEntity {
            return LegacyReplyEntity(
                id = validatedLegacyReply.id,
                parentId = validatedLegacyReply.parentId,
                pubkey = validatedLegacyReply.pubkey,
                createdAt = validatedLegacyReply.createdAt,
                relayUrl = validatedLegacyReply.relayUrl,
                content = validatedLegacyReply.content,
                json = validatedLegacyReply.json,
                isMentioningMe = validatedLegacyReply.isMentioningMe,
            )
        }
    }
}
