package com.dluvian.voyage.core.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.toShortenedBech32
import com.dluvian.voyage.data.interactor.Vote
import com.dluvian.voyage.data.provider.AnnotatedStringProvider
import com.dluvian.voyage.data.room.view.ReplyView

@Immutable
data class ReplyUI(
    override val id: EventIdHex,
    val parentId: EventIdHex,
    val authorName: String,
    override val pubkey: PubkeyHex,
    val trustType: TrustType,
    val createdAt: Long,
    override val content: AnnotatedString,
    val myVote: Vote,
    val tally: Int,
    val replyCount: Int,
) : IParentUI {
    companion object {
        fun from(replyView: ReplyView, annotatedStringProvider: AnnotatedStringProvider): ReplyUI {
            return ReplyUI(
                id = replyView.id,
                parentId = replyView.parentId,
                authorName = replyView.authorName.orEmpty()
                    .ifEmpty { replyView.pubkey.toShortenedBech32() },
                pubkey = replyView.pubkey,
                trustType = TrustType.from(
                    isFriend = replyView.authorIsFriend,
                    isWebOfTrust = replyView.authorIsTrusted
                ),
                createdAt = replyView.createdAt,
                content = annotatedStringProvider.annotate(replyView.content),
                myVote = Vote.from(vote = replyView.myVote),
                tally = replyView.upvoteCount - replyView.downvoteCount,
                replyCount = replyView.replyCount,
            )
        }
    }
}
