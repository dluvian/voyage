package com.dluvian.voyage.data.room.view

import androidx.room.DatabaseView
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.data.interactor.Vote
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
            profile.name AS authorName,
            ht.hashtag AS myTopic,
            CASE WHEN account.pubkey IS NOT NULL THEN 1 ELSE 0 END AS authorIsOneself,
            CASE WHEN friend.friendPubkey IS NOT NULL THEN 1 ELSE 0 END AS authorIsFriend,
            CASE WHEN weboftrust.webOfTrustPubkey IS NOT NULL THEN 1 ELSE 0 END AS authorIsTrusted,
            vote.isPositive AS myVote,
            upvotes.upvoteCount,
            downvotes.downvoteCount,
            replies.replyCount,
            CASE WHEN cross_posted_account.pubkey IS NOT NULL THEN 1 ELSE 0 END AS crossPostedAuthorIsOneself,
            CASE WHEN cross_posted_friend.friendPubkey IS NOT NULL THEN 1 ELSE 0 END AS crossPostedAuthorIsFriend,
            CASE WHEN cross_posted_wot.webOfTrustPubkey IS NOT NULL THEN 1 ELSE 0 END AS crossPostedAuthorIsTrusted
        FROM post
        LEFT JOIN profile ON profile.pubkey = post.pubkey
        LEFT JOIN (
            SELECT hashtag.postId, hashtag.hashtag 
            FROM hashtag 
            JOIN topic ON hashtag.hashtag = topic.topic
            WHERE topic.myPubkey = (SELECT pubkey FROM account LIMIT 1)
        ) AS ht ON ht.postId = post.id
        LEFT JOIN account ON account.pubkey = post.pubkey
        LEFT JOIN friend ON friend.friendPubkey = post.pubkey
        LEFT JOIN weboftrust ON weboftrust.webOfTrustPubkey = post.pubkey
        LEFT JOIN vote ON vote.postId = IFNULL(post.crossPostedId, post.id) AND vote.pubkey = (SELECT pubkey FROM account LIMIT 1)
        LEFT JOIN (
            SELECT vote.postId, COUNT(*) AS upvoteCount 
            FROM vote 
            WHERE vote.isPositive = 1 
            GROUP BY vote.postId
        ) AS upvotes ON upvotes.postId = IFNULL(post.crossPostedId, post.id)
        LEFT JOIN (
            SELECT vote.postId, COUNT(*) AS downvoteCount 
            FROM vote 
            WHERE vote.isPositive = 0 
            GROUP BY vote.postId
        ) AS downvotes ON downvotes.postId = IFNULL(post.crossPostedId, post.id)
        LEFT JOIN (
            SELECT post2.parentId, COUNT(*) AS replyCount 
            FROM post AS post2 
            WHERE post2.parentId IS NOT NULL 
            GROUP BY post2.parentId
        ) AS replies ON replies.parentId = IFNULL(post.crossPostedId, post.id)
        LEFT JOIN account AS cross_posted_account ON cross_posted_account.pubkey = post.crossPostedPubkey
        LEFT JOIN friend AS cross_posted_friend ON cross_posted_friend.friendPubkey = post.crossPostedPubkey
        LEFT JOIN weboftrust AS cross_posted_wot ON cross_posted_wot.webOfTrustPubkey = post.crossPostedPubkey
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
    val myTopic: Topic?,
    val subject: String?,
    val content: String,
    val createdAt: Long,
    val myVote: Boolean?,
    val upvoteCount: Int,
    val downvoteCount: Int,
    val replyCount: Int,
    val relayUrl: RelayUrl,
    val crossPostedId: EventIdHex?,
    val crossPostedPubkey: PubkeyHex?,
    val crossPostedAuthorIsOneself: Boolean,
    val crossPostedAuthorIsFriend: Boolean,
    val crossPostedAuthorIsTrusted: Boolean,
) {
    fun mapToRootPostUI(
        forcedVotes: Map<EventIdHex, Vote>,
        forcedFollows: Map<PubkeyHex, Boolean>,
        annotatedStringProvider: AnnotatedStringProvider,
    ): RootPostUI {
        val rootPostUI = RootPostUI.from(
            rootPostView = this,
            annotatedStringProvider = annotatedStringProvider
        )
        val vote = forcedVotes.getOrDefault(this.id, null)
        val follow = forcedFollows.getOrDefault(this.pubkey, null)
        return if (vote != null || follow != null) rootPostUI.copy(
            myVote = vote ?: rootPostUI.myVote,
            trustType = TrustType.from(
                isOneself = this.authorIsOneself,
                isFriend = follow ?: this.authorIsFriend,
                isWebOfTrust = this.authorIsTrusted
            )
        )
        else rootPostUI
    }
}
