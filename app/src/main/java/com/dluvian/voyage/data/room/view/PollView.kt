package com.dluvian.voyage.data.room.view

import androidx.room.DatabaseView
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.model.Poll
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.data.provider.AnnotatedStringProvider

@DatabaseView(
    """
        SELECT 
            mainEvent.id, 
            mainEvent.pubkey, 
            mainEvent.content, 
            poll.endsAt,
            mainEvent.createdAt, 
            mainEvent.relayUrl, 
            mainEvent.isMentioningMe, 
            profile.name AS authorName,
            ht.min_hashtag AS myTopic,
            CASE WHEN account.pubkey IS NOT NULL THEN 1 ELSE 0 END AS authorIsOneself,
            CASE WHEN friend.friendPubkey IS NOT NULL THEN 1 ELSE 0 END AS authorIsFriend,
            CASE WHEN weboftrust.webOfTrustPubkey IS NOT NULL THEN 1 ELSE 0 END AS authorIsTrusted,
            CASE WHEN mute.mutedItem IS NOT NULL THEN 1 ELSE 0 END AS authorIsMuted,
            CASE WHEN profileSetItem.pubkey IS NOT NULL THEN 1 ELSE 0 END AS authorIsInList,
            CASE WHEN lock.pubkey IS NOT NULL THEN 1 ELSE 0 END AS authorIsLocked,
            CASE WHEN vote.eventId IS NOT NULL THEN 1 ELSE 0 END isUpvoted,
            upvotes.upvoteCount,
            comments.commentCount,
            (SELECT EXISTS(SELECT * FROM bookmark WHERE bookmark.eventId = mainEvent.id)) AS isBookmarked 
        FROM poll
        JOIN mainEvent ON mainEvent.id = poll.eventId
        LEFT JOIN profile ON profile.pubkey = mainEvent.pubkey
        LEFT JOIN (
            SELECT DISTINCT hashtag.eventId, MIN(hashtag.hashtag) AS min_hashtag
            FROM hashtag 
            JOIN topic ON hashtag.hashtag = topic.topic
            WHERE topic.myPubkey = (SELECT pubkey FROM account LIMIT 1)
            GROUP BY hashtag.eventId
        ) AS ht ON ht.eventId = mainEvent.id
        LEFT JOIN account ON account.pubkey = mainEvent.pubkey
        LEFT JOIN friend ON friend.friendPubkey = mainEvent.pubkey
        LEFT JOIN weboftrust ON weboftrust.webOfTrustPubkey = mainEvent.pubkey
        LEFT JOIN mute ON mute.mutedItem = mainEvent.pubkey AND mute.tag IS 'p'
        LEFT JOIN profileSetItem ON profileSetItem.pubkey = mainEvent.pubkey
        LEFT JOIN lock ON lock.pubkey = mainEvent.pubkey
        LEFT JOIN vote ON vote.eventId = mainEvent.id AND vote.pubkey = (SELECT pubkey FROM account LIMIT 1)
        LEFT JOIN (
            SELECT vote.eventId, COUNT(*) AS upvoteCount 
            FROM vote 
            GROUP BY vote.eventId
        ) AS upvotes ON upvotes.eventId = mainEvent.id
        LEFT JOIN (
            SELECT comment.parentId, COUNT(*) AS commentCount 
            FROM comment
            GROUP BY comment.parentId
        ) AS comments ON comments.parentId = mainEvent.id
"""
)
data class PollView(
    val id: EventIdHex,
    val pubkey: PubkeyHex,
    val authorName: String?,
    val authorIsOneself: Boolean,
    val authorIsFriend: Boolean,
    val authorIsTrusted: Boolean,
    val authorIsMuted: Boolean,
    val authorIsInList: Boolean,
    val authorIsLocked: Boolean,
    val myTopic: Topic?,
    val content: String,
    val createdAt: Long,
    val endsAt: Long?,
    val isUpvoted: Boolean,
    val upvoteCount: Int,
    val commentCount: Int, // no legacy
    val relayUrl: RelayUrl,
    val isBookmarked: Boolean,
    val isMentioningMe: Boolean,
) {
    fun mapToPollUI(
        forcedVotes: Map<EventIdHex, Boolean>,
        forcedFollows: Map<PubkeyHex, Boolean>,
        forcedBookmarks: Map<EventIdHex, Boolean>,
        annotatedStringProvider: AnnotatedStringProvider,
    ): Poll {
        val pollUI = Poll.from(
            pollView = this,
            annotatedStringProvider = annotatedStringProvider
        )
        val vote = forcedVotes.getOrDefault(this.id, null)
        val follow = forcedFollows.getOrDefault(this.pubkey, null)
        val bookmark = forcedBookmarks.getOrDefault(this.id, null)
        return if (vote != null || follow != null || bookmark != null) pollUI.copy(
            isUpvoted = vote ?: pollUI.isUpvoted,
            trustType = TrustType.from(pollView = this, isFriend = follow),
            isBookmarked = bookmark ?: pollUI.isBookmarked
        )
        else pollUI
    }
}
