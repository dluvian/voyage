package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.model.ValidatedPost
import com.dluvian.voyage.data.model.ValidatedReplyPost
import com.dluvian.voyage.data.model.ValidatedRootPost

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
    val topic: String?,
    val title: String?,
    val content: String,
    val createdAt: Long,
) {
    companion object {
        fun from(post: ValidatedPost): PostEntity {
            return when (post) {
                is ValidatedRootPost -> PostEntity(
                    id = post.id.toHex(),
                    pubkey = post.pubkey.toHex(),
                    replyToId = null,
                    topic = post.topic,
                    title = post.title,
                    content = post.content,
                    createdAt = post.createdAt,
                )

                is ValidatedReplyPost -> PostEntity(
                    id = post.id.toHex(),
                    pubkey = post.pubkey.toHex(),
                    replyToId = post.replyToId,
                    topic = null,
                    title = null,
                    content = post.content,
                    createdAt = post.createdAt,
                )
            }
        }
    }
}
