package com.dluvian.voyage.data.room.view

import androidx.room.DatabaseView
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic

@DatabaseView(
    "SELECT post.id, " +
            "post.pubkey, " +
            "(SELECT hashtag FROM hashtag WHERE hashtag.postId = post.id AND hashtag IN (SELECT topic FROM topic WHERE myPubkey = (SELECT pubkey FROM account LIMIT 1)) LIMIT 1) AS myTopic, " +
            "post.title, " +
            "post.content, " +
            "post.createdAt, " +
            "(SELECT EXISTS(SELECT * FROM friend WHERE friend.friendPubkey = post.pubkey)) AS authorIsFriend, " +
            "(SELECT EXISTS(SELECT * FROM weboftrust WHERE weboftrust.webOfTrustPubkey = post.pubkey)) AS authorIsTrusted, " +
            "(SELECT isPositive FROM vote WHERE vote.postId = post.id AND vote.pubkey = (SELECT pubkey FROM account LIMIT 1)) AS myVote, " +
            "(SELECT COUNT(*) FROM vote WHERE vote.postId = post.id AND vote.isPositive = 1) AS upvoteCount, " +
            "(SELECT COUNT(*) FROM vote WHERE vote.postId = post.id AND vote.isPositive = 0) AS downvoteCount, " +
            "(SELECT COUNT(*) FROM post AS post2 WHERE post2.replyToId = post.id) AS commentCount " +
            "FROM post " +
            "WHERE post.replyToId IS NULL"
)
data class RootPostView(
    val id: EventIdHex,
    val pubkey: PubkeyHex,
    val authorIsFriend: Boolean,
    val authorIsTrusted: Boolean,
    val myTopic: Topic?,
    val title: String?,
    val content: String,
    val createdAt: Long,
    val myVote: Boolean?,
    val upvoteCount: Int,
    val downvoteCount: Int,
    val commentCount: Int,
)
