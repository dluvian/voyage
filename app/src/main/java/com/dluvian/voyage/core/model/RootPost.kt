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
    val topic: String,
    val createdAt: Long,
    val title: String,
    val content: String,
    val myVote: Vote,
    val tally: Int,
    val ratioInPercent: Int,
    val commentCount: Int,
) {
    companion object {
        fun from(rootPostView: RootPostView): RootPost {
            return RootPost(
                id = rootPostView.id,
                pubkey = rootPostView.pubkey,
                topic = rootPostView.topic.orEmpty(),
                createdAt = rootPostView.createdAt,
                title = rootPostView.title.orEmpty(),
                content = rootPostView.content,
                myVote = Vote.from(vote = rootPostView.myVote),
                tally = rootPostView.upvoteCount - rootPostView.downvoteCount,
                ratioInPercent = (rootPostView.upvoteCount / (rootPostView.upvoteCount + rootPostView.downvoteCount)) * 100,
                commentCount = rootPostView.commentCount,
            )
        }
    }
}
