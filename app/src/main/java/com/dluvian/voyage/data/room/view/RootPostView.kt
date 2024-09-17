package com.dluvian.voyage.data.room.view

import androidx.room.DatabaseView
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.data.provider.AnnotatedStringProvider

@DatabaseView(
    """
        SELECT 
            post.id, 
            post.pubkey, 
            post.subject, 
            post.content, 
            post.createdAt, 
            post.relayUrl, 
            post.crossPostedId, 
            post.crossPostedPubkey, 
            post.isMentioningMe, 
            profile.name AS authorName,
            ht.min_hashtag AS myTopic,
            CASE WHEN account.pubkey IS NOT NULL THEN 1 ELSE 0 END AS authorIsOneself,
            CASE WHEN friend.friendPubkey IS NOT NULL THEN 1 ELSE 0 END AS authorIsFriend,
            CASE WHEN weboftrust.webOfTrustPubkey IS NOT NULL THEN 1 ELSE 0 END AS authorIsTrusted,
            CASE WHEN mute.mutedItem IS NOT NULL THEN 1 ELSE 0 END AS authorIsMuted,
            CASE WHEN profileSetItem.pubkey IS NOT NULL THEN 1 ELSE 0 END AS authorIsInList,
            CASE WHEN lock.pubkey IS NOT NULL THEN 1 ELSE 0 END AS authorIsLocked,
            CASE WHEN vote.postId IS NOT NULL THEN 1 ELSE 0 END isUpvoted,
            upvotes.upvoteCount,
            replies.replyCount,
            CASE WHEN cross_posted_account.pubkey IS NOT NULL THEN 1 ELSE 0 END AS crossPostedAuthorIsOneself,
            CASE WHEN cross_posted_friend.friendPubkey IS NOT NULL THEN 1 ELSE 0 END AS crossPostedAuthorIsFriend,
            CASE WHEN cross_posted_wot.webOfTrustPubkey IS NOT NULL THEN 1 ELSE 0 END AS crossPostedAuthorIsTrusted,
            CASE WHEN cross_posted_mute.mutedItem IS NOT NULL THEN 1 ELSE 0 END AS crossPostedAuthorIsMuted,
            CASE WHEN cross_posted_profile_set_item.pubkey IS NOT NULL THEN 1 ELSE 0 END AS crossPostedAuthorIsInList,
            CASE WHEN cross_posted_lock.pubkey IS NOT NULL THEN 1 ELSE 0 END AS crossPostedAuthorIsLocked,
            (SELECT EXISTS(SELECT * FROM bookmark WHERE bookmark.postId = IFNULL(post.crossPostedId, post.id))) AS isBookmarked 
        FROM post
        LEFT JOIN profile ON profile.pubkey = post.pubkey
        LEFT JOIN (
            SELECT DISTINCT hashtag.eventId, MIN(hashtag.hashtag) AS min_hashtag
            FROM hashtag 
            JOIN topic ON hashtag.hashtag = topic.topic
            WHERE topic.myPubkey = (SELECT pubkey FROM account LIMIT 1)
            GROUP BY hashtag.eventId
        ) AS ht ON ht.eventId = post.id
        LEFT JOIN account ON account.pubkey = post.pubkey
        LEFT JOIN friend ON friend.friendPubkey = post.pubkey
        LEFT JOIN weboftrust ON weboftrust.webOfTrustPubkey = post.pubkey
        LEFT JOIN mute ON mute.mutedItem = post.pubkey AND mute.tag IS 'p'
        LEFT JOIN profileSetItem ON profileSetItem.pubkey = post.pubkey
        LEFT JOIN lock ON lock.pubkey = post.pubkey
        LEFT JOIN vote ON vote.postId = IFNULL(post.crossPostedId, post.id) AND vote.pubkey = (SELECT pubkey FROM account LIMIT 1)
        LEFT JOIN (
            SELECT vote.postId, COUNT(*) AS upvoteCount 
            FROM vote 
            GROUP BY vote.postId
        ) AS upvotes ON upvotes.postId = IFNULL(post.crossPostedId, post.id)
        LEFT JOIN (
            SELECT post2.parentId, COUNT(*) AS replyCount 
            FROM post AS post2 
            WHERE post2.parentId IS NOT NULL 
            AND post2.pubkey NOT IN (SELECT mutedItem FROM mute WHERE tag IS 'p')
            GROUP BY post2.parentId
        ) AS replies ON replies.parentId = IFNULL(post.crossPostedId, post.id)
        LEFT JOIN account AS cross_posted_account ON cross_posted_account.pubkey = post.crossPostedPubkey
        LEFT JOIN friend AS cross_posted_friend ON cross_posted_friend.friendPubkey = post.crossPostedPubkey
        LEFT JOIN weboftrust AS cross_posted_wot ON cross_posted_wot.webOfTrustPubkey = post.crossPostedPubkey
        LEFT JOIN mute AS cross_posted_mute ON cross_posted_mute.mutedItem = post.crossPostedPubkey AND cross_posted_mute.tag IS 'p'
        LEFT JOIN profileSetItem AS cross_posted_profile_set_item ON cross_posted_profile_set_item.pubkey = post.crossPostedPubkey
        LEFT JOIN lock AS cross_posted_lock ON cross_posted_lock.pubkey = post.crossPostedPubkey
        WHERE post.parentId IS NULL
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
    val crossPostedId: EventIdHex?,
    val crossPostedPubkey: PubkeyHex?,
    val crossPostedAuthorIsOneself: Boolean,
    val crossPostedAuthorIsFriend: Boolean,
    val crossPostedAuthorIsTrusted: Boolean,
    val crossPostedAuthorIsMuted: Boolean,
    val crossPostedAuthorIsInList: Boolean,
    val crossPostedAuthorIsLocked: Boolean,
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
