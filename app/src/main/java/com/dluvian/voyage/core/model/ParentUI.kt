package com.dluvian.voyage.core.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.interactor.Vote
import com.dluvian.voyage.data.provider.AnnotatedStringProvider
import com.dluvian.voyage.data.room.view.ReplyView
import com.dluvian.voyage.data.room.view.RootPostView

sealed class ParentUI(
    open val id: EventIdHex,
    open val content: AnnotatedString,
    open val pubkey: PubkeyHex,
    open val authorName: String?,
    open val trustType: TrustType,
    open val relayUrl: RelayUrl,
    open val replyCount: Int,
    open val createdAt: Long,
) {
    fun getRelevantId() = when (this) {
        is RootPostUI -> this.crossPostedId ?: this.id
        is ReplyUI -> this.id
    }

    fun getRelevantPubkey() = when (this) {
        is RootPostUI -> this.crossPostedPubkey ?: this.pubkey
        is ReplyUI -> this.pubkey
    }
}

@Immutable
data class RootPostUI(
    override val id: EventIdHex,
    override val pubkey: PubkeyHex,
    override val authorName: String?,
    override val trustType: TrustType,
    val myTopic: String?,
    override val createdAt: Long,
    val subject: AnnotatedString,
    override val content: AnnotatedString,
    val myVote: Vote,
    val upvoteCount: Int,
    val downvoteCount: Int,
    override val replyCount: Int,
    override val relayUrl: RelayUrl,
    val crossPostedId: EventIdHex?,
    val crossPostedPubkey: PubkeyHex?,
    val crossPostedTrustType: TrustType?,
) : ParentUI(
    id = id,
    content = content,
    pubkey = pubkey,
    authorName = authorName,
    trustType = trustType,
    relayUrl = relayUrl,
    replyCount = replyCount,
    createdAt = createdAt,
) {
    companion object {
        fun from(
            rootPostView: RootPostView,
            annotatedStringProvider: AnnotatedStringProvider
        ): RootPostUI {
            return RootPostUI(
                id = rootPostView.id,
                pubkey = rootPostView.pubkey,
                authorName = rootPostView.authorName,
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

@Immutable
data class ReplyUI(
    override val id: EventIdHex,
    val parentId: EventIdHex,
    override val pubkey: PubkeyHex,
    override val authorName: String?,
    override val trustType: TrustType,
    override val createdAt: Long,
    override val content: AnnotatedString,
    val myVote: Vote,
    val upvoteCount: Int,
    val downvoteCount: Int,
    override val replyCount: Int,
    override val relayUrl: RelayUrl,
) : ParentUI(
    id = id,
    content = content,
    pubkey = pubkey,
    authorName = authorName,
    trustType = trustType,
    relayUrl = relayUrl,
    replyCount = replyCount,
    createdAt = createdAt,
) {
    companion object {
        fun from(replyView: ReplyView, annotatedStringProvider: AnnotatedStringProvider): ReplyUI {
            return ReplyUI(
                id = replyView.id,
                parentId = replyView.parentId,
                pubkey = replyView.pubkey,
                authorName = replyView.authorName,
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
