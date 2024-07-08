package com.dluvian.voyage.core.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.data.provider.AnnotatedStringProvider
import com.dluvian.voyage.data.room.view.ReplyView
import com.dluvian.voyage.data.room.view.RootPostView
import rust.nostr.protocol.Kind
import rust.nostr.protocol.KindEnum

sealed class ParentUI(
    open val id: EventIdHex,
    open val content: AnnotatedString,
    open val pubkey: PubkeyHex,
    open val authorName: String?,
    open val trustType: TrustType,
    open val relayUrl: RelayUrl,
    open val replyCount: Int,
    open val createdAt: Long,
    open val isBookmarked: Boolean,
) {
    fun getRelevantId() = when (this) {
        is RootPostUI -> this.crossPostedId ?: this.id
        is ReplyUI -> this.id
    }

    fun getRelevantPubkey() = when (this) {
        is RootPostUI -> this.crossPostedPubkey ?: this.pubkey
        is ReplyUI -> this.pubkey
    }

    fun getKind(): Kind {
        return when (this) {
            is RootPostUI -> {
                if (this.crossPostedId != null) Kind.fromEnum(KindEnum.Repost)
                else Kind.fromEnum(KindEnum.TextNote)
            }

            is ReplyUI -> Kind.fromEnum(KindEnum.TextNote)
        }
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
    val isUpvoted: Boolean,
    val upvoteCount: Int,
    override val replyCount: Int,
    override val relayUrl: RelayUrl,
    val crossPostedId: EventIdHex?,
    val crossPostedPubkey: PubkeyHex?,
    val crossPostedTrustType: TrustType?,
    override val isBookmarked: Boolean,
) : ParentUI(
    id = id,
    content = content,
    pubkey = pubkey,
    authorName = authorName,
    trustType = trustType,
    relayUrl = relayUrl,
    replyCount = replyCount,
    createdAt = createdAt,
    isBookmarked = isBookmarked
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
                    isMuted = rootPostView.authorIsMuted
                ),
                myTopic = rootPostView.myTopic,
                createdAt = rootPostView.createdAt,
                subject = annotatedStringProvider.annotate(rootPostView.subject.orEmpty()),
                content = annotatedStringProvider.annotate(rootPostView.content),
                isUpvoted = rootPostView.isUpvoted,
                upvoteCount = rootPostView.upvoteCount,
                replyCount = rootPostView.replyCount,
                relayUrl = rootPostView.relayUrl,
                crossPostedId = rootPostView.crossPostedId,
                crossPostedPubkey = rootPostView.crossPostedPubkey,
                crossPostedTrustType = TrustType.from(
                    isOneself = rootPostView.crossPostedAuthorIsOneself,
                    isFriend = rootPostView.crossPostedAuthorIsFriend,
                    isWebOfTrust = rootPostView.crossPostedAuthorIsTrusted,
                    isMuted = false // False bc this will never be in UI
                ),
                isBookmarked = rootPostView.isBookmarked,
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
    val isUpvoted: Boolean,
    val upvoteCount: Int,
    override val replyCount: Int,
    override val relayUrl: RelayUrl,
    override val isBookmarked: Boolean,
) : ParentUI(
    id = id,
    content = content,
    pubkey = pubkey,
    authorName = authorName,
    trustType = trustType,
    relayUrl = relayUrl,
    replyCount = replyCount,
    createdAt = createdAt,
    isBookmarked = isBookmarked,
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
                    isWebOfTrust = replyView.authorIsTrusted,
                    isMuted = replyView.authorIsMuted,
                ),
                createdAt = replyView.createdAt,
                content = annotatedStringProvider.annotate(replyView.content),
                isUpvoted = replyView.isUpvoted,
                upvoteCount = replyView.upvoteCount,
                replyCount = replyView.replyCount,
                relayUrl = replyView.relayUrl,
                isBookmarked = replyView.isBookmarked
            )
        }
    }
}
