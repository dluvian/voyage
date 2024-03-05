package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.dluvian.nostr_kt.getReplyToId
import com.dluvian.nostr_kt.getTitle
import com.dluvian.nostr_kt.getTopic
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import rust.nostr.protocol.Event

@Entity(
    tableName = "post",
    primaryKeys = ["id"],
    foreignKeys = [ForeignKey(
        entity = PostEntity::class,
        parentColumns = ["id"],
        childColumns = ["replyToId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )],
    indices = [Index(value = ["replyToId"], unique = false)], // ksp suggestion: "Highly advised"
)
data class PostEntity(
    val id: EventIdHex,
    val pubkey: PubkeyHex,
    val replyToId: EventIdHex?,
    val topic: String,
    val title: String,
    val content: String,
    val createdAt: Long,
) {
    companion object {
        fun from(event: Event): PostEntity {
            return PostEntity(
                id = event.id().toHex(),
                pubkey = event.author().toHex(),
                replyToId = event.getReplyToId(),
                topic = event.getTopic()!!, // TODO from(event: ValidEvent)
                title = event.getTitle().orEmpty(),
                content = event.content(),
                createdAt = event.createdAt().asSecs().toLong(),
            )
        }
    }
}
