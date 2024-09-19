package com.dluvian.voyage.data.room.view

import androidx.room.DatabaseView
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.model.CrossRootPostUI
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.data.provider.AnnotatedStringProvider

// TODO: Same for cross posted legacy replies
@DatabaseView(
    """
        SELECT 
            crossPost.id, 
            crossPost.pubkey, 
            crossPost.crossPostedId, 
            crossPost.createdAt, 
            rootPost.subject, 
            rootPost.content, 
            rootPost.relayUrl, 
            rootPost.pubkey AS crossPostedPubkey, 
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
            CASE WHEN cross_posted_account.pubkey IS NOT NULL THEN 1 ELSE 0 END AS crossPostedAuthorIsOneself,
            CASE WHEN cross_posted_friend.friendPubkey IS NOT NULL THEN 1 ELSE 0 END AS crossPostedAuthorIsFriend,
            CASE WHEN cross_posted_wot.webOfTrustPubkey IS NOT NULL THEN 1 ELSE 0 END AS crossPostedAuthorIsTrusted,
            CASE WHEN cross_posted_mute.mutedItem IS NOT NULL THEN 1 ELSE 0 END AS crossPostedAuthorIsMuted,
            CASE WHEN cross_posted_profile_set_item.pubkey IS NOT NULL THEN 1 ELSE 0 END AS crossPostedAuthorIsInList,
            CASE WHEN cross_posted_lock.pubkey IS NOT NULL THEN 1 ELSE 0 END AS crossPostedAuthorIsLocked,
            (SELECT EXISTS(SELECT * FROM bookmark WHERE bookmark.eventId = crossPost.crossPostedId)) AS isBookmarked 
        FROM crossPost
        LEFT JOIN profile ON profile.pubkey = (SELECT pubkey AS crossPostedPubkey FROM rootPost WHERE rootPost.id = crossPost.crossPostedId)
        LEFT JOIN rootPost ON rootPost.id = crossPost.crossPostedId
        LEFT JOIN (
            SELECT DISTINCT hashtag.eventId, MIN(hashtag.hashtag) AS min_hashtag
            FROM hashtag 
            JOIN topic ON hashtag.hashtag = topic.topic
            WHERE topic.myPubkey = (SELECT pubkey FROM account LIMIT 1)
            GROUP BY hashtag.eventId
        ) AS ht ON ht.eventId = crossPost.id
        LEFT JOIN account ON account.pubkey = crossPost.pubkey
        LEFT JOIN friend ON friend.friendPubkey = crossPost.pubkey
        LEFT JOIN weboftrust ON weboftrust.webOfTrustPubkey = crossPost.pubkey
        LEFT JOIN mute ON mute.mutedItem = crossPost.pubkey AND mute.tag IS 'p'
        LEFT JOIN profileSetItem ON profileSetItem.pubkey = crossPost.pubkey
        LEFT JOIN lock ON lock.pubkey = crossPost.pubkey
        LEFT JOIN vote ON vote.eventId = crossPost.crossPostedId AND vote.pubkey = (SELECT pubkey FROM account LIMIT 1)
        LEFT JOIN (
            SELECT vote.eventId, COUNT(*) AS upvoteCount 
            FROM vote 
            GROUP BY vote.eventId
        ) AS upvotes ON upvotes.eventId = crossPost.crossPostedId
        LEFT JOIN (
            SELECT legacyReply.parentId, COUNT(*) AS replyCount 
            FROM legacyReply AS legacyReply 
            WHERE legacyReply.pubkey NOT IN (SELECT mutedItem FROM mute WHERE tag IS 'p')
            GROUP BY legacyReply.parentId
        ) AS replies ON replies.parentId = crossPost.crossPostedId
        LEFT JOIN account AS cross_posted_account ON cross_posted_account.pubkey = (SELECT pubkey AS crossPostedPubkey FROM rootPost WHERE rootPost.id = crossPost.crossPostedId)
        LEFT JOIN friend AS cross_posted_friend ON cross_posted_friend.friendPubkey = (SELECT pubkey AS crossPostedPubkey FROM rootPost WHERE rootPost.id = crossPost.crossPostedId)
        LEFT JOIN weboftrust AS cross_posted_wot ON cross_posted_wot.webOfTrustPubkey = (SELECT pubkey AS crossPostedPubkey FROM rootPost WHERE rootPost.id = crossPost.crossPostedId)
        LEFT JOIN mute AS cross_posted_mute ON cross_posted_mute.mutedItem = (SELECT pubkey AS crossPostedPubkey FROM rootPost WHERE rootPost.id = crossPost.crossPostedId) AND cross_posted_mute.tag IS 'p'
        LEFT JOIN profileSetItem AS cross_posted_profile_set_item ON cross_posted_profile_set_item.pubkey = (SELECT pubkey AS crossPostedPubkey FROM rootPost WHERE rootPost.id = crossPost.crossPostedId)
        LEFT JOIN lock AS cross_posted_lock ON cross_posted_lock.pubkey = (SELECT pubkey AS crossPostedPubkey FROM rootPost WHERE rootPost.id = crossPost.crossPostedId)
"""
)
data class CrossRootPostView(
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
    val crossPostedId: EventIdHex?,
    val crossPostedPubkey: PubkeyHex?,
    val crossPostedAuthorIsOneself: Boolean,
    val crossPostedAuthorIsFriend: Boolean,
    val crossPostedAuthorIsTrusted: Boolean,
    val crossPostedAuthorIsMuted: Boolean,
    val crossPostedAuthorIsInList: Boolean,
    val crossPostedAuthorIsLocked: Boolean,
    val isBookmarked: Boolean,
) {
    fun mapToCrossRootPostUI(
        forcedVotes: Map<EventIdHex, Boolean>,
        forcedFollows: Map<PubkeyHex, Boolean>,
        forcedBookmarks: Map<EventIdHex, Boolean>,
        annotatedStringProvider: AnnotatedStringProvider,
    ): CrossRootPostUI {
        val crossRootPostUI = CrossRootPostUI.from(
            crossRootPostView = this,
            annotatedStringProvider = annotatedStringProvider
        )
        val vote = forcedVotes.getOrDefault(this.crossPostedId, null)
        val follow = forcedFollows.getOrDefault(this.pubkey, null)
        val crossPostedFollow = forcedFollows.getOrDefault(this.crossPostedPubkey, null)
        val bookmark = forcedBookmarks.getOrDefault(this.id, null)
        return if (vote != null || follow != null || bookmark != null) crossRootPostUI.copy(
            isUpvoted = vote ?: crossRootPostUI.isUpvoted,
            trustType = TrustType.from(
                isOneself = this.authorIsOneself,
                isFriend = follow ?: this.authorIsFriend,
                isWebOfTrust = this.authorIsTrusted,
                isMuted = this.authorIsMuted,
                isInList = this.authorIsInList,
                isLocked = this.authorIsLocked,
            ),
            crossPostedTrustType = TrustType.from(
                isOneself = this.crossPostedAuthorIsOneself,
                isFriend = crossPostedFollow ?: this.crossPostedAuthorIsFriend,
                isWebOfTrust = this.crossPostedAuthorIsTrusted,
                isMuted = this.crossPostedAuthorIsMuted,
                isInList = this.crossPostedAuthorIsInList,
                isLocked = this.crossPostedAuthorIsLocked,
            ),
            isBookmarked = bookmark ?: crossRootPostUI.isBookmarked
        )
        else crossRootPostUI
    }
}
