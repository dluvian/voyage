package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.MAX_CONTENT_LEN
import com.dluvian.voyage.core.MAX_TITLE_LEN
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedPost
import com.dluvian.voyage.data.event.ValidatedReplyPost
import com.dluvian.voyage.data.event.ValidatedRootPost

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
    val title: String?,
    val content: String,
    val createdAt: Long,
) {
    companion object {

        fun from(post: ValidatedPost): PostEntity {
            return when (post) {
                is ValidatedRootPost -> PostEntity(
                    id = post.id,
                    pubkey = post.pubkey,
                    replyToId = null,
                    title = post.title?.trim()?.take(MAX_TITLE_LEN),
                    content = post.content.trim().take(MAX_CONTENT_LEN),
                    createdAt = post.createdAt,
                )

                is ValidatedReplyPost -> PostEntity(
                    id = post.id,
                    pubkey = post.pubkey,
                    replyToId = post.replyToId,
                    title = null,
                    content = post.content.trim().take(MAX_CONTENT_LEN),
                    createdAt = post.createdAt,
                )
            }
        }
    }
}
