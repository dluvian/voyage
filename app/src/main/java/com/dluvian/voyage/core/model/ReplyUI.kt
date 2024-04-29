package com.dluvian.voyage.core.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.interactor.Vote
import com.dluvian.voyage.data.provider.AnnotatedStringProvider
import com.dluvian.voyage.data.room.view.ReplyView

@Immutable
data class ReplyUI(
    override val id: EventIdHex,
    val parentId: EventIdHex,
    override val pubkey: PubkeyHex,
    override val trustType: TrustType,
    override val createdAt: Long,
    override val content: AnnotatedString,
    val myVote: Vote,
    val upvoteCount: Int,
    val downvoteCount: Int,
    val replyCount: Int,
    override val relayUrl: RelayUrl,
    override val crossPostedId: EventIdHex? = null
) : IParentUI {
    companion object {
        fun from(replyView: ReplyView, annotatedStringProvider: AnnotatedStringProvider): ReplyUI {
            return ReplyUI(
                id = replyView.id,
                parentId = replyView.parentId,
                pubkey = replyView.pubkey,
                trustType = TrustType.from(
                    isOneself = replyView.authorIsOneself,
                    isFriend = replyView.authorIsFriend,
                    isWebOfTrust = replyView.authorIsTrusted
                ),
                createdAt = replyView.createdAt,
                content = annotatedStringProvider.annotate(replyView.content),
                myVote = Vote.from(vote = replyView.myVote),
                upvoteCount = replyView.upvoteCount,
                downvoteCount = replyView.downvoteCount,
                replyCount = replyView.replyCount,
                relayUrl = replyView.relayUrl
            )
        }
    }
}
