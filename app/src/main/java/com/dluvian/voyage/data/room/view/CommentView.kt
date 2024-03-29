package com.dluvian.voyage.data.room.view

import androidx.room.DatabaseView
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.model.CommentUI
import com.dluvian.voyage.core.model.LeveledCommentUI
import com.dluvian.voyage.data.interactor.Vote


@DatabaseView(
    "SELECT post.id, " +
            "post.parentId, " +
            "post.pubkey, " +
            "post.content, " +
            "post.createdAt, " +
            "(SELECT name FROM profile WHERE profile.pubkey = post.pubkey) AS authorName, " +
            "(SELECT EXISTS(SELECT * FROM account WHERE account.pubkey = post.pubkey)) AS authorIsMe, " +
            "(SELECT EXISTS(SELECT * FROM friend WHERE friend.friendPubkey = post.pubkey)) AS authorIsFriend, " +
            "(SELECT EXISTS(SELECT * FROM weboftrust WHERE weboftrust.webOfTrustPubkey = post.pubkey)) AS authorIsTrusted, " +
            "(SELECT isPositive FROM vote WHERE vote.postId = post.id AND vote.pubkey = (SELECT pubkey FROM account LIMIT 1)) AS myVote, " +
            "(SELECT COUNT(*) FROM vote WHERE vote.postId = post.id AND vote.isPositive = 1) AS upvoteCount, " +
            "(SELECT COUNT(*) FROM vote WHERE vote.postId = post.id AND vote.isPositive = 0) AS downvoteCount, " +
            "(SELECT COUNT(*) FROM post AS post2 WHERE post2.parentId = post.id) AS commentCount " +
            "FROM post " +
            "WHERE post.parentId IS NOT NULL"
)
data class CommentView(
    val id: EventIdHex,
    val parentId: EventIdHex,
    val authorName: String?,
    val pubkey: PubkeyHex,
    val content: String,
    val createdAt: Long,
    val authorIsMe: Boolean,
    val authorIsFriend: Boolean,
    val authorIsTrusted: Boolean,
    val myVote: Boolean?,
    val upvoteCount: Int,
    val downvoteCount: Int,
    val commentCount: Int,
) {
    fun mapToCommentUI(forcedVotes: Map<EventIdHex, Vote>): CommentUI {
        val commentUI = CommentUI.from(commentView = this)
        val vote = forcedVotes.getOrDefault(this.id, null)
        return if (vote != null) commentUI.copy(myVote = vote) else commentUI
    }

    fun mapToLeveledCommentUI(
        level: Int,
        forcedVotes: Map<EventIdHex, Vote>,
        collapsedIds: Set<EventIdHex>,
        parentIds: Set<EventIdHex>
    ): LeveledCommentUI {
        return LeveledCommentUI(
            level = level,
            comment = this.mapToCommentUI(forcedVotes = forcedVotes),
            isCollapsed = collapsedIds.contains(this.id),
            hasLoadedReplies = parentIds.contains(this.id)
        )
    }
}
