package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.Index
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedRootPost
import com.dluvian.voyage.data.nostr.RelayUrl

@Entity(
    tableName = "rootPost",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["pubkey"], unique = false),
        Index(value = ["createdAt"], unique = false),
        Index(value = ["isMentioningMe"], unique = false),
    ],
)
data class RootPostEntity(
    val id: EventIdHex,
    val pubkey: PubkeyHex,
    val createdAt: Long,
    val relayUrl: RelayUrl,
    val subject: String,
    val content: String,
    val json: String,
    val isMentioningMe: Boolean,
) {
    companion object {
        fun from(validatedRootPost: ValidatedRootPost): RootPostEntity {
            return RootPostEntity(
                id = validatedRootPost.id,
                pubkey = validatedRootPost.pubkey,
                createdAt = validatedRootPost.createdAt,
                relayUrl = validatedRootPost.relayUrl,
                subject = validatedRootPost.subject,
                content = validatedRootPost.content,
                json = validatedRootPost.json,
                isMentioningMe = validatedRootPost.isMentioningMe,
            )
        }
    }
}
