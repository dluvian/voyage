package com.dluvian.voyage.data.room.view

import androidx.room.DatabaseView
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.model.LeveledReplyUI
import com.dluvian.voyage.core.model.ReplyUI
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.data.interactor.Vote
import com.dluvian.voyage.data.provider.AnnotatedStringProvider


@DatabaseView(
    "SELECT post.id, " +
            "post.parentId, " +
            "post.pubkey, " +
            "post.content, " +
            "post.createdAt, " +
            "post.relayUrl, " +
            "(SELECT name FROM profile WHERE profile.pubkey = post.pubkey) AS authorName, " +
            "(SELECT EXISTS(SELECT * FROM account WHERE account.pubkey = post.pubkey)) AS authorIsOneself, " +
            "(SELECT EXISTS(SELECT * FROM friend WHERE friend.friendPubkey = post.pubkey)) AS authorIsFriend, " +
            "(SELECT EXISTS(SELECT * FROM weboftrust WHERE weboftrust.webOfTrustPubkey = post.pubkey)) AS authorIsTrusted, " +
            "(SELECT isPositive FROM vote WHERE vote.postId = post.id AND vote.pubkey = (SELECT pubkey FROM account LIMIT 1)) AS myVote, " +
            "(SELECT COUNT(*) FROM vote WHERE vote.postId = post.id AND vote.isPositive = 1) AS upvoteCount, " +
            "(SELECT COUNT(*) FROM vote WHERE vote.postId = post.id AND vote.isPositive = 0) AS downvoteCount, " +
            "(SELECT COUNT(*) FROM post AS post2 WHERE post2.parentId = post.id) AS replyCount, " +
            "(SELECT EXISTS(SELECT * FROM bookmark WHERE bookmark.postId = IFNULL(post.crossPostedId, post.id))) AS isBookmarked " +
            "FROM post " +
            "WHERE post.parentId IS NOT NULL"
)
data class ReplyView(
    val id: EventIdHex,
    val parentId: EventIdHex,
    val pubkey: PubkeyHex,
    val authorName: String?,
    val content: String,
    val createdAt: Long,
    val authorIsOneself: Boolean,
    val authorIsFriend: Boolean,
    val authorIsTrusted: Boolean,
    val myVote: Boolean?,
    val upvoteCount: Int,
    val downvoteCount: Int,
    val replyCount: Int,
    val relayUrl: RelayUrl,
    val isBookmarked: Boolean,
) {
    fun mapToLeveledReplyUI(
        level: Int,
        forcedVotes: Map<EventIdHex, Vote>,
        forcedFollows: Map<PubkeyHex, Boolean>,
        forcedBookmarks: Map<EventIdHex, Boolean>,
        collapsedIds: Set<EventIdHex>,
        parentIds: Set<EventIdHex>,
        annotatedStringProvider: AnnotatedStringProvider,
    ): LeveledReplyUI {
        return LeveledReplyUI(
            level = level,
            reply = this.mapToReplyUI(
                forcedVotes = forcedVotes,
                forcedFollows = forcedFollows,
                forcedBookmarks = forcedBookmarks,
                annotatedStringProvider = annotatedStringProvider
            ),
            isCollapsed = collapsedIds.contains(this.id),
            hasLoadedReplies = parentIds.contains(this.id)
        )
    }

    fun mapToReplyUI(
        forcedVotes: Map<EventIdHex, Vote>,
        forcedFollows: Map<PubkeyHex, Boolean>,
        forcedBookmarks: Map<EventIdHex, Boolean>,
        annotatedStringProvider: AnnotatedStringProvider
    ): ReplyUI {
        val reply = ReplyUI.from(
            replyView = this,
            annotatedStringProvider = annotatedStringProvider
        )
        val vote = forcedVotes.getOrDefault(this.id, null)
        val follow = forcedFollows.getOrDefault(this.pubkey, null)
        val bookmark = forcedBookmarks.getOrDefault(this.id, null)
        return if (vote != null || follow != null || bookmark != null) reply.copy(
            myVote = vote ?: reply.myVote,
            trustType = TrustType.from(
                isOneself = this.authorIsOneself,
                isFriend = follow ?: this.authorIsFriend,
                isWebOfTrust = this.authorIsTrusted
            ),
            isBookmarked = bookmark ?: reply.isBookmarked
        ) else reply
    }
}
