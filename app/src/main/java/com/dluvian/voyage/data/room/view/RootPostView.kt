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
    "SELECT post.id, " +
            "post.pubkey, " +
            "post.subject, " +
            "post.content, " +
            "post.createdAt, " +
            "post.relayUrl, " +
            "post.crossPostedId, " +
            "post.crossPostedPubkey, " +
            "(SELECT hashtag FROM hashtag WHERE hashtag.postId = post.id AND hashtag IN (SELECT topic FROM topic WHERE myPubkey = (SELECT pubkey FROM account LIMIT 1)) LIMIT 1) AS myTopic, " +
            "(SELECT EXISTS(SELECT * FROM account WHERE account.pubkey = post.pubkey)) AS authorIsOneself, " +
            "(SELECT EXISTS(SELECT * FROM friend WHERE friend.friendPubkey = post.pubkey)) AS authorIsFriend, " +
            "(SELECT EXISTS(SELECT * FROM weboftrust WHERE weboftrust.webOfTrustPubkey = post.pubkey)) AS authorIsTrusted, " +
            "(SELECT isPositive FROM vote WHERE vote.postId = IFNULL(post.crossPostedId, post.id) AND vote.pubkey = (SELECT pubkey FROM account LIMIT 1)) AS myVote, " +
            "(SELECT COUNT(*) FROM vote WHERE vote.postId = IFNULL(post.crossPostedId, post.id) AND vote.isPositive = 1) AS upvoteCount, " +
            "(SELECT COUNT(*) FROM vote WHERE vote.postId = IFNULL(post.crossPostedId, post.id) AND vote.isPositive = 0) AS downvoteCount, " +
            "(SELECT COUNT(*) FROM post AS post2 WHERE post2.parentId = IFNULL(post.crossPostedId, post.id)) AS replyCount, " +
            "(SELECT EXISTS(SELECT * FROM account WHERE account.pubkey = post.crossPostedPubkey)) AS crossPostedAuthorIsOneself, " +
            "(SELECT EXISTS(SELECT * FROM friend WHERE friend.friendPubkey = post.crossPostedPubkey)) AS crossPostedAuthorIsFriend, " +
            "(SELECT EXISTS(SELECT * FROM weboftrust WHERE weboftrust.webOfTrustPubkey = post.crossPostedPubkey)) AS crossPostedAuthorIsTrusted " +
            "FROM post " +
            "WHERE post.parentId IS NULL"
)
data class RootPostView(
    val id: EventIdHex,
    val pubkey: PubkeyHex,
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
