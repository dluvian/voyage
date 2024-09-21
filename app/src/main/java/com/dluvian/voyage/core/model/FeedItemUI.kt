package com.dluvian.voyage.core.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.data.provider.AnnotatedStringProvider
import com.dluvian.voyage.data.room.view.CrossPostView
import com.dluvian.voyage.data.room.view.LegacyReplyView
import com.dluvian.voyage.data.room.view.RootPostView
import rust.nostr.protocol.Kind
import rust.nostr.protocol.KindEnum

sealed class FeedItemUI(
    open val id: EventIdHex,
    open val pubkey: PubkeyHex,
    open val content: AnnotatedString,
    open val authorName: String?,
    open val trustType: TrustType,
    open val relayUrl: RelayUrl,
    open val replyCount: Int,
    open val upvoteCount: Int,
    open val createdAt: Long,
    open val isUpvoted: Boolean,
    open val isBookmarked: Boolean,
) {
    fun getKind(): Kind {
        return when (this) {
            is RootPostUI, is LegacyReplyUI -> Kind.fromEnum(KindEnum.TextNote)
            is CrossPostUI -> Kind.fromEnum(KindEnum.Repost)
        }
    }

    fun getRelevantId() = when (this) {
        is RootPostUI -> this.id
        is LegacyReplyUI -> this.id
        is CrossPostUI -> this.crossPostedId
    }

    fun getRelevantPubkey() = when (this) {
        is RootPostUI -> this.pubkey
        is LegacyReplyUI -> this.pubkey
        is CrossPostUI -> this.crossPostedPubkey
    }

    fun getSubject() = when (this) {
        is RootPostUI -> this.subject
        is LegacyReplyUI -> null
        is CrossPostUI -> this.crossPostedSubject
    }
}

@Immutable
data class RootPostUI(
    override val id: EventIdHex,
    override val pubkey: PubkeyHex,
    override val content: AnnotatedString,
    override val authorName: String?,
    override val trustType: TrustType,
    override val createdAt: Long,
    override val upvoteCount: Int,
    override val replyCount: Int,
    override val relayUrl: RelayUrl,
    override val isUpvoted: Boolean,
    override val isBookmarked: Boolean,
    val myTopic: String?,
    val subject: AnnotatedString,
) : FeedItemUI(
    id = id,
    pubkey = pubkey,
    content = content,
    authorName = authorName,
    trustType = trustType,
    relayUrl = relayUrl,
    replyCount = replyCount,
    upvoteCount = upvoteCount,
    createdAt = createdAt,
    isUpvoted = isUpvoted,
    isBookmarked = isBookmarked,
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
                trustType = TrustType.from(rootPostView = rootPostView),
                myTopic = rootPostView.myTopic,
                createdAt = rootPostView.createdAt,
                subject = annotatedStringProvider.annotate(rootPostView.subject.orEmpty()),
                content = annotatedStringProvider.annotate(rootPostView.content),
                upvoteCount = rootPostView.upvoteCount,
                replyCount = rootPostView.replyCount,
                relayUrl = rootPostView.relayUrl,
                isUpvoted = rootPostView.isUpvoted,
                isBookmarked = rootPostView.isBookmarked,
            )
        }
    }
}

@Immutable
data class LegacyReplyUI(
    override val id: EventIdHex,
    override val pubkey: PubkeyHex,
    override val authorName: String?,
    override val trustType: TrustType,
    override val createdAt: Long,
    override val content: AnnotatedString,
    override val upvoteCount: Int,
    override val replyCount: Int,
    override val relayUrl: RelayUrl,
    override val isUpvoted: Boolean,
    override val isBookmarked: Boolean,
    val parentId: EventIdHex,
//    TODO val isOp: Boolean,
) : FeedItemUI(
    id = id,
    pubkey = pubkey,
    content = content,
    authorName = authorName,
    trustType = trustType,
    relayUrl = relayUrl,
    replyCount = replyCount,
    upvoteCount = upvoteCount,
    createdAt = createdAt,
    isUpvoted = isUpvoted,
    isBookmarked = isBookmarked,
) {
    companion object {
        fun from(
            legacyReplyView: LegacyReplyView,
            annotatedStringProvider: AnnotatedStringProvider
        ): LegacyReplyUI {
            return LegacyReplyUI(
                id = legacyReplyView.id,
                parentId = legacyReplyView.parentId,
                pubkey = legacyReplyView.pubkey,
                authorName = legacyReplyView.authorName,
                trustType = TrustType.from(legacyReplyView = legacyReplyView),
                createdAt = legacyReplyView.createdAt,
                content = annotatedStringProvider.annotate(legacyReplyView.content),
                isUpvoted = legacyReplyView.isUpvoted,
                upvoteCount = legacyReplyView.upvoteCount,
                replyCount = legacyReplyView.replyCount,
                relayUrl = legacyReplyView.relayUrl,
                isBookmarked = legacyReplyView.isBookmarked
            )
        }
    }
}

// TODO: This kinda sucks
@Immutable
data class CrossPostUI(
    override val id: EventIdHex,
    override val pubkey: PubkeyHex,
    override val authorName: String?,
    override val trustType: TrustType,
    override val createdAt: Long,
    val myTopic: String?,
    val crossPostedId: EventIdHex,
    val crossPostedPubkey: PubkeyHex,
    val crossPostedUpvoteCount: Int,
    val crossPostedReplyCount: Int,
    val crossPostedRelayUrl: RelayUrl,
    val crossPostedIsUpvoted: Boolean,
    val crossPostedIsBookmarked: Boolean,
    val crossPostedContent: AnnotatedString,
    val crossPostedSubject: AnnotatedString,
    val crossPostedTrustType: TrustType,
) : FeedItemUI(
    id = id,
    pubkey = pubkey,
    content = crossPostedContent,
    authorName = authorName,
    trustType = trustType,
    relayUrl = crossPostedRelayUrl,
    replyCount = crossPostedReplyCount,
    upvoteCount = crossPostedUpvoteCount,
    createdAt = createdAt,
    isUpvoted = crossPostedIsUpvoted,
    isBookmarked = crossPostedIsBookmarked,
) {
    companion object {
        fun from(
            crossPostView: CrossPostView,
            annotatedStringProvider: AnnotatedStringProvider
        ): CrossPostUI {
            return CrossPostUI(
                id = crossPostView.id,
                pubkey = crossPostView.pubkey,
                authorName = crossPostView.authorName,
                trustType = TrustType.fromCrossPostAuthor(crossPostView = crossPostView),
                myTopic = crossPostView.myTopic,
                createdAt = crossPostView.createdAt,
                crossPostedSubject = annotatedStringProvider.annotate(crossPostView.crossPostedSubject.orEmpty()),
                crossPostedContent = annotatedStringProvider.annotate(crossPostView.crossPostedContent),
                crossPostedIsUpvoted = crossPostView.crossPostedIsUpvoted,
                crossPostedUpvoteCount = crossPostView.crossPostedUpvoteCount,
                crossPostedReplyCount = crossPostView.crossPostedReplyCount,
                crossPostedRelayUrl = crossPostView.crossPostedRelayUrl,
                crossPostedId = crossPostView.crossPostedId,
                crossPostedPubkey = crossPostView.crossPostedPubkey,
                crossPostedTrustType = TrustType.fromCrossPostedAuthor(crossPostView = crossPostView),
                crossPostedIsBookmarked = crossPostView.crossPostedIsBookmarked,
            )
        }
    }
}
