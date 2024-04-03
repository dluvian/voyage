package com.dluvian.voyage.core.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.toShortenedBech32
import com.dluvian.voyage.data.interactor.Vote
import com.dluvian.voyage.data.provider.AnnotatedStringProvider
import com.dluvian.voyage.data.room.view.RootPostView

@Immutable
data class RootPostUI(
    override val id: EventIdHex,
    override val pubkey: PubkeyHex,
    val authorName: String,
    val trustType: TrustType,
    val myTopic: String?,
    val createdAt: Long,
    val title: AnnotatedString,
    override val content: AnnotatedString,
    val myVote: Vote,
    val tally: Int,
    val replyCount: Int,
) : IParentUI {
    companion object {
        fun from(
            rootPostView: RootPostView,
            annotatedStringProvider: AnnotatedStringProvider
        ): RootPostUI {
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
                title = annotatedStringProvider.annotate(rootPostView.title.orEmpty()),
                content = annotatedStringProvider.annotate(rootPostView.content),
                myVote = Vote.from(vote = rootPostView.myVote),
                tally = rootPostView.upvoteCount - rootPostView.downvoteCount,
                replyCount = rootPostView.replyCount,
            )
        }
    }
}
