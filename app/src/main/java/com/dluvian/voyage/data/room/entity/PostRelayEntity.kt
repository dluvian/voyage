package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.model.RelayedItem

@Entity(
    tableName = "postRelay",
    primaryKeys = ["postId", "relayUrl"],
    foreignKeys = [ForeignKey(
        entity = PostEntity::class,
        parentColumns = ["id"],
        childColumns = ["postId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )]
)
data class PostRelayEntity(
    val postId: EventIdHex,
    val relayUrl: RelayUrl
) {
    companion object {
        fun from(relayedPostEntity: RelayedItem<PostEntity>): PostRelayEntity {
            return PostRelayEntity(
                postId = relayedPostEntity.item.id,
                relayUrl = relayedPostEntity.relayUrl
            )
        }
    }
}
