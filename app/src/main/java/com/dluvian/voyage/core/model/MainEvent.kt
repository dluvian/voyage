package com.dluvian.voyage.core.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.utils.commentableKinds
import com.dluvian.voyage.data.event.COMMENT_U16
import com.dluvian.voyage.data.event.POLL_U16
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.data.provider.AnnotatedStringProvider
import com.dluvian.voyage.data.room.view.CommentView
import com.dluvian.voyage.data.room.view.CrossPostView
import com.dluvian.voyage.data.room.view.LegacyReplyView
import com.dluvian.voyage.data.room.view.PollOptionView
import com.dluvian.voyage.data.room.view.PollView
import com.dluvian.voyage.data.room.view.RootPostView
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard

sealed class MainEvent(
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
    fun getRelevantKind(): Kind? {
        return when (this) {
            is RootPost, is LegacyReply -> Kind.fromStd(KindStandard.TEXT_NOTE)
            is CrossPost -> null
            is Comment -> Kind(COMMENT_U16)
            is Poll -> Kind(POLL_U16)
        }
    }

    fun getRelevantId() = when (this) {
        is RootPost, is LegacyReply, is Comment, is Poll -> this.id
        is CrossPost -> this.crossPostedId
    }

    fun getRelevantPubkey() = when (this) {
        is RootPost, is LegacyReply, is Comment, is Poll -> this.pubkey
        is CrossPost -> this.crossPostedPubkey
    }

    fun getRelevantSubject() = when (this) {
        is RootPost -> this.subject
        is LegacyReply, is Comment, is Poll -> null
        is CrossPost -> this.crossPostedSubject
    }
}

sealed class ThreadableMainEvent(
    override val id: EventIdHex,
    override val pubkey: PubkeyHex,
    override val content: AnnotatedString,
    override val authorName: String?,
    override val trustType: TrustType,
    override val relayUrl: RelayUrl,
    override val replyCount: Int,
    override val upvoteCount: Int,
    override val createdAt: Long,
    override val isUpvoted: Boolean,
    override val isBookmarked: Boolean,
) : MainEvent(
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
)

@Immutable
data class RootPost(
    override val id: EventIdHex,
    override val pubkey: PubkeyHex,
    override val content: AnnotatedString,
    override val authorName: String?,
    override val trustType: TrustType,
    override val createdAt: Long,
    override val upvoteCount: Int,
    override val relayUrl: RelayUrl,
    override val isUpvoted: Boolean,
    override val isBookmarked: Boolean,
    val myTopic: String?,
    val subject: AnnotatedString,
    val legacyReplyCount: Int,
    val commentCount: Int,
) : ThreadableMainEvent(
    id = id,
    pubkey = pubkey,
    content = content,
    authorName = authorName,
    trustType = trustType,
    relayUrl = relayUrl,
    replyCount = legacyReplyCount + commentCount,
    upvoteCount = upvoteCount,
    createdAt = createdAt,
    isUpvoted = isUpvoted,
    isBookmarked = isBookmarked,
) {
    companion object {
        fun from(
            rootPostView: RootPostView,
            annotatedStringProvider: AnnotatedStringProvider
        ): RootPost {
            return RootPost(
                id = rootPostView.id,
                pubkey = rootPostView.pubkey,
                authorName = rootPostView.authorName,
                trustType = TrustType.from(rootPostView = rootPostView),
                myTopic = rootPostView.myTopic,
                createdAt = rootPostView.createdAt,
                subject = annotatedStringProvider.annotate(rootPostView.subject),
                content = annotatedStringProvider.annotate(rootPostView.content),
                upvoteCount = rootPostView.upvoteCount,
                relayUrl = rootPostView.relayUrl,
                isUpvoted = rootPostView.isUpvoted,
                isBookmarked = rootPostView.isBookmarked,
                legacyReplyCount = rootPostView.legacyReplyCount,
                commentCount = rootPostView.commentCount,
            )
        }
    }
}

@Immutable
data class Poll(
    override val id: EventIdHex,
    override val pubkey: PubkeyHex,
    override val content: AnnotatedString,
    override val authorName: String?,
    override val trustType: TrustType,
    override val createdAt: Long,
    override val upvoteCount: Int,
    override val relayUrl: RelayUrl,
    override val isUpvoted: Boolean,
    override val isBookmarked: Boolean,
    val myTopic: String?,
    val commentCount: Int,
    val options: List<PollOptionView>,
    val latestResponse: Long?,
    val endsAt: Long?,
) : ThreadableMainEvent(
    id = id,
    pubkey = pubkey,
    content = content,
    authorName = authorName,
    trustType = trustType,
    relayUrl = relayUrl,
    replyCount = commentCount,
    upvoteCount = upvoteCount,
    createdAt = createdAt,
    isUpvoted = isUpvoted,
    isBookmarked = isBookmarked,
) {
    companion object {
        fun from(
            pollView: PollView,
            pollOptions: List<PollOptionView>,
            annotatedStringProvider: AnnotatedStringProvider
        ): Poll {
            return Poll(
                id = pollView.id,
                pubkey = pollView.pubkey,
                authorName = pollView.authorName,
                trustType = TrustType.from(pollView = pollView),
                myTopic = pollView.myTopic,
                createdAt = pollView.createdAt,
                content = annotatedStringProvider.annotate(pollView.content),
                upvoteCount = pollView.upvoteCount,
                relayUrl = pollView.relayUrl,
                isUpvoted = pollView.isUpvoted,
                isBookmarked = pollView.isBookmarked,
                commentCount = pollView.commentCount,
                options = pollOptions,
                latestResponse = pollView.latestResponse,
                endsAt = pollView.endsAt,
            )
        }
    }
}

sealed class SomeReply(
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
) : ThreadableMainEvent(
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
)

@Immutable
data class LegacyReply(
    override val id: EventIdHex,
    override val pubkey: PubkeyHex,
    override val authorName: String?,
    override val trustType: TrustType,
    override val createdAt: Long,
    override val content: AnnotatedString,
    override val upvoteCount: Int,
    override val relayUrl: RelayUrl,
    override val isUpvoted: Boolean,
    override val isBookmarked: Boolean,
    val parentId: EventIdHex,
    val legacyReplyCount: Int,
    val commentCount: Int,
) : SomeReply(
    id = id,
    pubkey = pubkey,
    content = content,
    authorName = authorName,
    trustType = trustType,
    relayUrl = relayUrl,
    replyCount = legacyReplyCount + commentCount,
    upvoteCount = upvoteCount,
    createdAt = createdAt,
    isUpvoted = isUpvoted,
    isBookmarked = isBookmarked,
) {
    companion object {
        fun from(
            legacyReplyView: LegacyReplyView,
            annotatedStringProvider: AnnotatedStringProvider
        ): LegacyReply {
            return LegacyReply(
                id = legacyReplyView.id,
                parentId = legacyReplyView.parentId,
                pubkey = legacyReplyView.pubkey,
                authorName = legacyReplyView.authorName,
                trustType = TrustType.from(legacyReplyView = legacyReplyView),
                createdAt = legacyReplyView.createdAt,
                content = annotatedStringProvider.annotate(legacyReplyView.content),
                isUpvoted = legacyReplyView.isUpvoted,
                upvoteCount = legacyReplyView.upvoteCount,
                relayUrl = legacyReplyView.relayUrl,
                isBookmarked = legacyReplyView.isBookmarked,
                legacyReplyCount = legacyReplyView.legacyReplyCount,
                commentCount = legacyReplyView.commentCount,
            )
        }
    }
}

@Immutable
data class Comment(
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
    val parentId: EventIdHex?,
    val parentKind: Int?
) : SomeReply(
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
    fun parentIsSupported(): Boolean {
        return parentId != null && commentableKinds.any { it.asU16().toInt() == parentKind }
    }

    companion object {
        fun from(
            commentView: CommentView,
            annotatedStringProvider: AnnotatedStringProvider
        ): Comment {
            return Comment(
                id = commentView.id,
                parentId = commentView.parentId,
                parentKind = commentView.parentKind,
                pubkey = commentView.pubkey,
                authorName = commentView.authorName,
                trustType = TrustType.from(commentView = commentView),
                createdAt = commentView.createdAt,
                content = annotatedStringProvider.annotate(commentView.content),
                isUpvoted = commentView.isUpvoted,
                upvoteCount = commentView.upvoteCount,
                replyCount = commentView.replyCount,
                relayUrl = commentView.relayUrl,
                isBookmarked = commentView.isBookmarked,
            )
        }
    }
}

@Immutable
data class CrossPost(
    override val id: EventIdHex,
    override val pubkey: PubkeyHex,
    override val authorName: String?,
    override val trustType: TrustType,
    override val createdAt: Long,
    val myTopic: String?,
    val crossPostedId: EventIdHex,
    val crossPostedPubkey: PubkeyHex,
    val crossPostedAuthorName: PubkeyHex?,
    val crossPostedUpvoteCount: Int,
    val crossPostedLegacyReplyCount: Int,
    val crossPostedCommentCount: Int,
    val crossPostedRelayUrl: RelayUrl,
    val crossPostedIsUpvoted: Boolean,
    val crossPostedIsBookmarked: Boolean,
    val crossPostedContent: AnnotatedString,
    val crossPostedSubject: AnnotatedString,
    val crossPostedTrustType: TrustType,
) : MainEvent(
    id = id,
    pubkey = pubkey,
    content = crossPostedContent,
    authorName = authorName,
    trustType = trustType,
    relayUrl = crossPostedRelayUrl,
    replyCount = crossPostedLegacyReplyCount + crossPostedCommentCount,
    upvoteCount = crossPostedUpvoteCount,
    createdAt = createdAt,
    isUpvoted = crossPostedIsUpvoted,
    isBookmarked = crossPostedIsBookmarked,
) {
    companion object {
        fun from(
            crossPostView: CrossPostView,
            annotatedStringProvider: AnnotatedStringProvider
        ): CrossPost {
            return CrossPost(
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
                crossPostedLegacyReplyCount = crossPostView.crossPostedLegacyReplyCount,
                crossPostedCommentCount = crossPostView.crossPostedCommentCount,
                crossPostedRelayUrl = crossPostView.crossPostedRelayUrl,
                crossPostedId = crossPostView.crossPostedId,
                crossPostedPubkey = crossPostView.crossPostedPubkey,
                crossPostedAuthorName = crossPostView.crossPostedAuthorName,
                crossPostedTrustType = TrustType.fromCrossPostedAuthor(crossPostView = crossPostView),
                crossPostedIsBookmarked = crossPostView.crossPostedIsBookmarked,
            )
        }
    }
}
