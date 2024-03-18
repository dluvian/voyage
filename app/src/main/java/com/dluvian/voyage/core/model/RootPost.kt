package com.dluvian.voyage.core.model

import androidx.compose.runtime.Immutable
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.interactor.Vote
import com.dluvian.voyage.data.room.view.RootPostView

@Immutable
data class RootPost(
    val id: EventIdHex,
    val pubkey: PubkeyHex,
    val trustType: TrustType,
    val myTopic: String?,
    val createdAt: Long,
    val title: String,
    val content: String,
    val myVote: Vote,
    val tally: Int,
    val commentCount: Int,
) {
    companion object {
        fun from(rootPostView: RootPostView): RootPost {
            return RootPost(
                id = rootPostView.id,
                pubkey = rootPostView.pubkey,
                trustType = TrustType.from(
                    isFriend = rootPostView.authorIsFriend,
                    isWebOfTrust = rootPostView.authorIsTrusted
                ),
                myTopic = rootPostView.myTopic,
                createdAt = rootPostView.createdAt,
                title = rootPostView.title.orEmpty(),
                content = rootPostView.content,
                myVote = Vote.from(vote = rootPostView.myVote),
                tally = rootPostView.upvoteCount - rootPostView.downvoteCount,
                commentCount = rootPostView.commentCount,
            )
        }
    }
}
