package com.dluvian.voyage.data.room.view

import androidx.room.DatabaseView
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.model.LegacyReplyUI
import com.dluvian.voyage.core.model.LeveledReplyUI
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.data.provider.AnnotatedStringProvider


@DatabaseView(
    "SELECT legacyReply.id, " +
            "legacyReply.parentId, " +
            "legacyReply.pubkey, " +
            "legacyReply.content, " +
            "legacyReply.createdAt, " +
            "legacyReply.relayUrl, " +
            "legacyReply.isMentioningMe, " +
            "(SELECT name FROM profile WHERE profile.pubkey = legacyReply.pubkey) AS authorName, " +
            "(SELECT EXISTS(SELECT * FROM account WHERE account.pubkey = legacyReply.pubkey)) AS authorIsOneself, " +
            "(SELECT EXISTS(SELECT * FROM friend WHERE friend.friendPubkey = legacyReply.pubkey)) AS authorIsFriend, " +
            "(SELECT EXISTS(SELECT * FROM weboftrust WHERE weboftrust.webOfTrustPubkey = legacyReply.pubkey)) AS authorIsTrusted, " +
            "(SELECT EXISTS(SELECT * FROM mute WHERE mute.mutedItem = legacyReply.pubkey AND mute.tag IS 'p')) AS authorIsMuted, " +
            "(SELECT EXISTS(SELECT * FROM profileSetItem WHERE profileSetItem.pubkey = legacyReply.pubkey)) AS authorIsInList, " +
            "(SELECT EXISTS(SELECT * FROM lock WHERE lock.pubkey = legacyReply.pubkey)) AS authorIsLocked, " +
            "(SELECT EXISTS(SELECT* FROM vote WHERE vote.eventId = legacyReply.id AND vote.pubkey = (SELECT pubkey FROM account LIMIT 1))) AS isUpvoted, " +
            "(SELECT COUNT(*) FROM vote WHERE vote.eventId = legacyReply.id) AS upvoteCount, " +
            "(SELECT COUNT(*) FROM legacyReply AS legacyReply2 WHERE legacyReply2.parentId = legacyReply.id AND legacyReply2.pubkey NOT IN (SELECT mutedItem FROM mute WHERE tag IS 'p')) AS replyCount, " +
            "(SELECT EXISTS(SELECT * FROM bookmark WHERE bookmark.eventId = legacyReply.id)) AS isBookmarked " +
            "FROM legacyReply "
)
data class LegacyReplyView(
    val id: EventIdHex,
    val parentId: EventIdHex,
    val pubkey: PubkeyHex,
    val authorName: String?,
    val content: String,
    val createdAt: Long,
    val authorIsOneself: Boolean,
    val authorIsFriend: Boolean,
    val authorIsTrusted: Boolean,
    val authorIsMuted: Boolean,
    val authorIsInList: Boolean,
    val authorIsLocked: Boolean,
    val isUpvoted: Boolean,
    val upvoteCount: Int,
    val replyCount: Int,
    val relayUrl: RelayUrl,
    val isBookmarked: Boolean,
    val isMentioningMe: Boolean,
) {
    fun mapToLeveledReplyUI(
        level: Int,
        forcedVotes: Map<EventIdHex, Boolean>,
        forcedFollows: Map<PubkeyHex, Boolean>,
        forcedBookmarks: Map<EventIdHex, Boolean>,
        collapsedIds: Set<EventIdHex>,
        parentIds: Set<EventIdHex>,
        annotatedStringProvider: AnnotatedStringProvider,
    ): LeveledReplyUI {
        return LeveledReplyUI(
            level = level,
            reply = this.mapToLegacyReplyUI(
                forcedVotes = forcedVotes,
                forcedFollows = forcedFollows,
                forcedBookmarks = forcedBookmarks,
                annotatedStringProvider = annotatedStringProvider
            ),
            isCollapsed = collapsedIds.contains(this.id),
            hasLoadedReplies = parentIds.contains(this.id)
        )
    }

    fun mapToLegacyReplyUI(
        forcedVotes: Map<EventIdHex, Boolean>,
        forcedFollows: Map<PubkeyHex, Boolean>,
        forcedBookmarks: Map<EventIdHex, Boolean>,
        annotatedStringProvider: AnnotatedStringProvider
    ): LegacyReplyUI {
        val reply = LegacyReplyUI.from(
            legacyReplyView = this,
            annotatedStringProvider = annotatedStringProvider
        )
        val vote = forcedVotes.getOrDefault(this.id, null)
        val follow = forcedFollows.getOrDefault(this.pubkey, null)
        val bookmark = forcedBookmarks.getOrDefault(this.id, null)
        return if (vote != null || follow != null || bookmark != null) reply.copy(
            isUpvoted = vote ?: reply.isUpvoted,
            trustType = TrustType.from(
                isOneself = this.authorIsOneself,
                isFriend = follow ?: this.authorIsFriend,
                isWebOfTrust = this.authorIsTrusted,
                isMuted = this.authorIsMuted,
                isInList = this.authorIsInList,
                isLocked = this.authorIsLocked,
            ),
            isBookmarked = bookmark ?: reply.isBookmarked
        ) else reply
    }
}
