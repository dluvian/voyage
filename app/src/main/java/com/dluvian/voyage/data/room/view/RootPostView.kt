package com.dluvian.voyage.data.room.view

import androidx.room.DatabaseView
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.data.provider.AnnotatedStringProvider

// TODO: Exclude muted replies from replyCount
@DatabaseView(
    """
        SELECT 
            rootPost.eventId, 
            mainEvent.pubkey, 
            rootPost.subject, 
            mainEvent.content, 
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
            replies.replyCount,
            (SELECT EXISTS(SELECT * FROM bookmark WHERE bookmark.eventId = rootPost.eventId)) AS isBookmarked 
        FROM rootPost
        JOIN mainEvent ON mainEvent.id = rootPost.eventId
        LEFT JOIN profile ON profile.pubkey = mainEvent.pubkey
        LEFT JOIN (
            SELECT DISTINCT hashtag.eventId, MIN(hashtag.hashtag) AS min_hashtag
            FROM hashtag 
            JOIN topic ON hashtag.hashtag = topic.topic
            WHERE topic.myPubkey = (SELECT pubkey FROM account LIMIT 1)
            GROUP BY hashtag.eventId
        ) AS ht ON ht.eventId = rootPost.eventId
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
            SELECT legacyReply.parentId, COUNT(*) AS replyCount 
            FROM legacyReply
            GROUP BY legacyReply.parentId
        ) AS replies ON replies.parentId = rootPost.eventId
"""
)
data class RootPostView(
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
    val subject: String?,
    val content: String,
    val createdAt: Long,
    val isUpvoted: Boolean,
    val upvoteCount: Int,
    val replyCount: Int,
    val relayUrl: RelayUrl,
    val isBookmarked: Boolean,
    val isMentioningMe: Boolean,
) {
    fun mapToRootPostUI(
        forcedVotes: Map<EventIdHex, Boolean>,
        forcedFollows: Map<PubkeyHex, Boolean>,
        forcedBookmarks: Map<EventIdHex, Boolean>,
        annotatedStringProvider: AnnotatedStringProvider,
    ): RootPostUI {
        val rootPostUI = RootPostUI.from(
            rootPostView = this,
            annotatedStringProvider = annotatedStringProvider
        )
        val vote = forcedVotes.getOrDefault(this.id, null)
        val follow = forcedFollows.getOrDefault(this.pubkey, null)
        val bookmark = forcedBookmarks.getOrDefault(this.id, null)
        return if (vote != null || follow != null || bookmark != null) rootPostUI.copy(
            isUpvoted = vote ?: rootPostUI.isUpvoted,
            trustType = TrustType.from(
                isOneself = this.authorIsOneself,
                isFriend = follow ?: this.authorIsFriend,
                isWebOfTrust = this.authorIsTrusted,
                isMuted = this.authorIsMuted,
                isInList = this.authorIsInList,
                isLocked = this.authorIsLocked,
            ),
            isBookmarked = bookmark ?: rootPostUI.isBookmarked
        )
        else rootPostUI
    }
}
