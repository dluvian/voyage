package com.dluvian.voyage.data.room.view

import androidx.room.DatabaseView
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.data.interactor.Vote

@DatabaseView(
    "SELECT post.id, " +
            "post.pubkey, " +
            "post.title, " +
            "post.content, " +
            "post.createdAt, " +
            "(SELECT name FROM profile WHERE profile.pubkey = post.pubkey) AS authorName, " +
            "(SELECT hashtag FROM hashtag WHERE hashtag.postId = post.id AND hashtag IN (SELECT topic FROM topic WHERE myPubkey = (SELECT pubkey FROM account LIMIT 1)) LIMIT 1) AS myTopic, " +
            "(SELECT EXISTS(SELECT * FROM account WHERE account.pubkey = post.pubkey)) AS authorIsMe, " +
            "(SELECT EXISTS(SELECT * FROM friend WHERE friend.friendPubkey = post.pubkey)) AS authorIsFriend, " +
            "(SELECT EXISTS(SELECT * FROM weboftrust WHERE weboftrust.webOfTrustPubkey = post.pubkey)) AS authorIsTrusted, " +
            "(SELECT isPositive FROM vote WHERE vote.postId = post.id AND vote.pubkey = (SELECT pubkey FROM account LIMIT 1)) AS myVote, " +
            "(SELECT COUNT(*) FROM vote WHERE vote.postId = post.id AND vote.isPositive = 1) AS upvoteCount, " +
            "(SELECT COUNT(*) FROM vote WHERE vote.postId = post.id AND vote.isPositive = 0) AS downvoteCount, " +
            "(SELECT COUNT(*) FROM post AS post2 WHERE post2.parentId = post.id) AS replyCount " +
            "FROM post " +
            "WHERE post.parentId IS NULL"
)
data class RootPostView(
    val id: EventIdHex,
    val pubkey: PubkeyHex,
    val authorName: String?,
    val authorIsMe: Boolean,
    val authorIsFriend: Boolean,
    val authorIsTrusted: Boolean,
    val myTopic: Topic?,
    val title: String?,
    val content: String,
    val createdAt: Long,
    val myVote: Boolean?,
    val upvoteCount: Int,
    val downvoteCount: Int,
    val replyCount: Int,
) {
    fun mapToRootPostUI(forcedVotes: Map<EventIdHex, Vote>): RootPostUI {
        val rootPostUI = RootPostUI.from(this)
        val vote = forcedVotes.getOrDefault(this.id, null)
        return if (vote != null) rootPostUI.copy(myVote = vote) else rootPostUI
    }
}
