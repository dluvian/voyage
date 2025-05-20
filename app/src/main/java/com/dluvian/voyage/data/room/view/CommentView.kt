package com.dluvian.voyage.data.room.view

import androidx.room.DatabaseView
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.model.Comment
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.data.provider.AnnotatedStringProvider
import com.dluvian.voyage.ui.components.row.mainEvent.ThreadReplyCtx

@DatabaseView(
    "SELECT mainEvent.id, " +
            "comment.parentId, " +
            "comment.parentKind, " +
            "mainEvent.pubkey, " +
            "mainEvent.content, " +
            "mainEvent.createdAt, " +
            "mainEvent.relayUrl, " +
            "mainEvent.isMentioningMe, " +
            "(SELECT name FROM profile WHERE profile.pubkey = mainEvent.pubkey) AS authorName, " +
            "(SELECT EXISTS(SELECT * FROM account WHERE account.pubkey = mainEvent.pubkey)) AS authorIsOneself, " +
            "(SELECT EXISTS(SELECT * FROM friend WHERE friend.friendPubkey = mainEvent.pubkey)) AS authorIsFriend, " +
            "(SELECT EXISTS(SELECT * FROM weboftrust WHERE weboftrust.webOfTrustPubkey = mainEvent.pubkey)) AS authorIsTrusted, " +
            "(SELECT EXISTS(SELECT * FROM profileSetItem WHERE profileSetItem.pubkey = mainEvent.pubkey)) AS authorIsInList, " +
            "(SELECT EXISTS(SELECT * FROM lock WHERE lock.pubkey = mainEvent.pubkey)) AS authorIsLocked, " +
            "(SELECT EXISTS(SELECT* FROM vote WHERE vote.eventId = mainEvent.id AND vote.pubkey = (SELECT pubkey FROM account LIMIT 1))) AS isUpvoted, " +
            "(SELECT COUNT(*) FROM vote WHERE vote.eventId = mainEvent.id) AS upvoteCount, " +
            "(SELECT COUNT(*) FROM comment AS comment2 WHERE comment2.parentId = mainEvent.id) AS replyCount, " +
            "(SELECT EXISTS(SELECT * FROM bookmark WHERE bookmark.eventId = mainEvent.id)) AS isBookmarked " +
            "FROM comment " +
            "JOIN mainEvent ON mainEvent.id = comment.eventId"
)
data class CommentView(
    val id: EventIdHex,
    val parentId: EventIdHex?,
    val parentKind: Int?,
    val pubkey: PubkeyHex,
    val authorName: String?,
    val content: String,
    val createdAt: Long,
    val authorIsOneself: Boolean,
    val authorIsFriend: Boolean,
    val authorIsTrusted: Boolean,
    val authorIsInList: Boolean,
    val authorIsLocked: Boolean,
    val isUpvoted: Boolean,
    val upvoteCount: Int,
    val replyCount: Int,
    val relayUrl: RelayUrl,
    val isBookmarked: Boolean,
    val isMentioningMe: Boolean,
) {
    fun mapToThreadReplyCtx(
        level: Int,
        isOp: Boolean,
        forcedVotes: Map<EventIdHex, Boolean>,
        forcedFollows: Map<PubkeyHex, Boolean>,
        forcedBookmarks: Map<EventIdHex, Boolean>,
        collapsedIds: Set<EventIdHex>,
        parentIds: Set<EventIdHex>,
        annotatedStringProvider: AnnotatedStringProvider,
    ): ThreadReplyCtx {
        return ThreadReplyCtx(
            reply = this.mapToCommentUI(
                forcedVotes = forcedVotes,
                forcedFollows = forcedFollows,
                forcedBookmarks = forcedBookmarks,
                annotatedStringProvider = annotatedStringProvider
            ),
            isOp = isOp,
            level = level,
            isCollapsed = collapsedIds.contains(this.id),
            hasLoadedReplies = parentIds.contains(this.id)
        )
    }

    fun mapToCommentUI(
        forcedVotes: Map<EventIdHex, Boolean>,
        forcedFollows: Map<PubkeyHex, Boolean>,
        forcedBookmarks: Map<EventIdHex, Boolean>,
        annotatedStringProvider: AnnotatedStringProvider
    ): Comment {
        val comment = Comment.from(
            commentView = this,
            annotatedStringProvider = annotatedStringProvider
        )
        val vote = forcedVotes.getOrDefault(this.id, null)
        val follow = forcedFollows.getOrDefault(this.pubkey, null)
        val bookmark = forcedBookmarks.getOrDefault(this.id, null)
        return if (vote != null || follow != null || bookmark != null) comment.copy(
            isUpvoted = vote ?: comment.isUpvoted,
            trustType = TrustType.from(commentView = this, isFriend = follow),
            isBookmarked = bookmark ?: comment.isBookmarked
        ) else comment
    }
}
