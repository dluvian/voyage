package com.dluvian.voyage.core.model

import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.toShortenedBech32
import com.dluvian.voyage.data.interactor.Vote
import com.dluvian.voyage.data.room.view.CommentView

data class CommentUI(
    val id: EventIdHex,
    val parentId: EventIdHex,
    val authorName: String,
    val pubkey: PubkeyHex,
    val trustType: TrustType,
    val createdAt: Long,
    val content: String,
    val myVote: Vote,
    val tally: Int,
    val commentCount: Int,
    val isCollapsed: Boolean,
) {
    companion object {
        fun from(commentView: CommentView, isCollapsed: Boolean): CommentUI {
            return CommentUI(
                id = commentView.id,
                parentId = commentView.parentId,
                authorName = commentView.authorName.orEmpty()
                    .ifEmpty { commentView.pubkey.toShortenedBech32() },
                pubkey = commentView.pubkey,
                trustType = TrustType.from(
                    isFriend = commentView.authorIsFriend,
                    isWebOfTrust = commentView.authorIsTrusted
                ),
                createdAt = commentView.createdAt,
                content = commentView.content,
                myVote = Vote.from(vote = commentView.myVote),
                tally = commentView.upvoteCount - commentView.downvoteCount,
                commentCount = commentView.commentCount,
                isCollapsed = isCollapsed,
            )
        }
    }
}
