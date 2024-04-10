package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.MAX_CONTENT_LEN
import com.dluvian.voyage.core.MAX_SUBJECT_LEN
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedPost
import com.dluvian.voyage.data.event.ValidatedReply
import com.dluvian.voyage.data.event.ValidatedRootPost

@Entity(
    tableName = "post",
    primaryKeys = ["id"],
    foreignKeys = [ForeignKey(
        entity = PostEntity::class,
        parentColumns = ["id"],
        childColumns = ["parentId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )],
    indices = [
        Index(value = ["parentId"], unique = false),
        Index(value = ["createdAt"], unique = false),
        Index(value = ["pubkey"], unique = false),
    ],
)
data class PostEntity(
    val id: EventIdHex,
    val pubkey: PubkeyHex,
    val parentId: EventIdHex?,
    val subject: String?,
    val content: String,
    val createdAt: Long,
    val relayUrl: RelayUrl,
) {
    companion object {

        fun from(post: ValidatedPost): PostEntity {
            return when (post) {
                is ValidatedRootPost -> PostEntity(
                    id = post.id,
                    pubkey = post.pubkey,
                    parentId = null,
                    subject = post.subject?.trim()?.take(MAX_SUBJECT_LEN),
                    content = post.content.trim().take(MAX_CONTENT_LEN),
                    createdAt = post.createdAt,
                    relayUrl = post.relayUrl
                )

                is ValidatedReply -> PostEntity(
                    id = post.id,
                    pubkey = post.pubkey,
                    parentId = post.parentId,
                    subject = null,
                    content = post.content.trim().take(MAX_CONTENT_LEN),
                    createdAt = post.createdAt,
                    relayUrl = post.relayUrl
                )
            }
        }
    }
}
