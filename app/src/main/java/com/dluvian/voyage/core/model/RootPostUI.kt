package com.dluvian.voyage.core.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.interactor.Vote
import com.dluvian.voyage.data.provider.AnnotatedStringProvider
import com.dluvian.voyage.data.room.view.RootPostView

@Immutable
data class RootPostUI(
    override val id: EventIdHex,
    override val pubkey: PubkeyHex,
    override val trustType: TrustType,
    val myTopic: String?,
    override val createdAt: Long,
    val subject: AnnotatedString,
    override val content: AnnotatedString,
    val myVote: Vote,
    val upvoteCount: Int,
    val downvoteCount: Int,
    val replyCount: Int,
    override val relayUrl: RelayUrl,
    val crossPostedId: EventIdHex?,
    val crossPostedPubkey: PubkeyHex?,
    val crossPostedTrustType: TrustType?
) : IParentUI {
    companion object {
        fun from(
            rootPostView: RootPostView,
            annotatedStringProvider: AnnotatedStringProvider
        ): RootPostUI {
            return RootPostUI(
                id = rootPostView.id,
                pubkey = rootPostView.pubkey,
                trustType = TrustType.from(
                    isOneself = rootPostView.authorIsOneself,
                    isFriend = rootPostView.authorIsFriend,
                    isWebOfTrust = rootPostView.authorIsTrusted,
                ),
                myTopic = rootPostView.myTopic,
                createdAt = rootPostView.createdAt,
                subject = annotatedStringProvider.annotate(rootPostView.subject.orEmpty()),
                content = annotatedStringProvider.annotate(rootPostView.content),
                myVote = Vote.from(vote = rootPostView.myVote),
                upvoteCount = rootPostView.upvoteCount,
                downvoteCount = rootPostView.downvoteCount,
                replyCount = rootPostView.replyCount,
                relayUrl = rootPostView.relayUrl,
                crossPostedId = rootPostView.crossPostedId,
                crossPostedPubkey = rootPostView.crossPostedPubkey,
                crossPostedTrustType = TrustType.from(
                    isOneself = rootPostView.crossPostedAuthorIsOneself,
                    isFriend = rootPostView.crossPostedAuthorIsFriend,
                    isWebOfTrust = rootPostView.crossPostedAuthorIsTrusted,
                ),
            )
        }
    }
}
