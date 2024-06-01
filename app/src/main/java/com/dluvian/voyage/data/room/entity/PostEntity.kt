package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.Index
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedCrossPost
import com.dluvian.voyage.data.event.ValidatedPost
import com.dluvian.voyage.data.event.ValidatedReply
import com.dluvian.voyage.data.event.ValidatedRootPost

@Entity(
    tableName = "post",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["parentId"], unique = false),
        Index(value = ["createdAt"], unique = false),
        Index(value = ["pubkey"], unique = false),
        Index(value = ["crossPostedId"], unique = false),
        Index(value = ["crossPostedPubkey"], unique = false),
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
    val crossPostedId: EventIdHex?,
    val crossPostedPubkey: PubkeyHex?,
    val json: String?
) {
    companion object {

        fun from(post: ValidatedPost): PostEntity {
            return when (post) {
                is ValidatedRootPost -> PostEntity(
                    id = post.id,
                    pubkey = post.pubkey,
                    parentId = null,
                    subject = post.subject,
                    content = post.content,
                    createdAt = post.createdAt,
                    relayUrl = post.relayUrl,
                    crossPostedId = null,
                    crossPostedPubkey = null,
                    json = post.json
                )

                is ValidatedReply -> PostEntity(
                    id = post.id,
                    pubkey = post.pubkey,
                    parentId = post.parentId,
                    subject = null,
                    content = post.content,
                    createdAt = post.createdAt,
                    relayUrl = post.relayUrl,
                    crossPostedId = null,
                    crossPostedPubkey = null,
                    json = post.json
                )

                is ValidatedCrossPost -> PostEntity(
                    id = post.id,
                    pubkey = post.pubkey,
                    parentId = null,
                    subject = post.crossPosted.subject,
                    content = when (post.crossPosted) {
                        is ValidatedRootPost -> post.crossPosted.content
                        is ValidatedReply -> post.crossPosted.content
                    },
                    createdAt = post.createdAt,
                    relayUrl = post.relayUrl,
                    crossPostedId = post.crossPosted.id,
                    crossPostedPubkey = post.crossPosted.pubkey,
                    json = null
                )
            }
        }
    }
}
