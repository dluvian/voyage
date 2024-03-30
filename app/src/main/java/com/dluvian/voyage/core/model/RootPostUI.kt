package com.dluvian.voyage.core.model

import androidx.compose.runtime.Immutable
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.toShortenedBech32
import com.dluvian.voyage.data.interactor.Vote
import com.dluvian.voyage.data.room.view.RootPostView

@Immutable
data class RootPostUI(
    override val id: EventIdHex,
    override val pubkey: PubkeyHex,
    val authorName: String,
    val trustType: TrustType,
    val myTopic: String?,
    val createdAt: Long,
    val title: String,
    override val content: String,
    val myVote: Vote,
    val tally: Int,
    val commentCount: Int,
) : IParentUI {
    companion object {
        fun from(rootPostView: RootPostView): RootPostUI {
            return RootPostUI(
                id = rootPostView.id,
                pubkey = rootPostView.pubkey,
                authorName = rootPostView.authorName
                    .orEmpty().ifEmpty { rootPostView.pubkey.toShortenedBech32() },
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
