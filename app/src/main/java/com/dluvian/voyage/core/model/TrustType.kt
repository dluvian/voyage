package com.dluvian.voyage.core.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.dluvian.voyage.data.room.view.CommentView
import com.dluvian.voyage.data.room.view.CrossPostView
import com.dluvian.voyage.data.room.view.LegacyReplyView
import com.dluvian.voyage.data.room.view.PollView
import com.dluvian.voyage.data.room.view.RootPostView

@Immutable
sealed class TrustType {
    companion object {
        @Stable
        fun from(
            isOneself: Boolean,
            isFriend: Boolean,
            isWebOfTrust: Boolean,
            isMuted: Boolean,
            isInList: Boolean,
            isLocked: Boolean,
        ): TrustType {
            return if (isOneself && isLocked) LockedOneself
            else if (isOneself) Oneself
            else if (isLocked) Locked
            else if (isMuted) Muted
            else if (isFriend) FriendTrust
            else if (isInList) IsInListTrust
            else if (isWebOfTrust) WebTrust
            else NoTrust
        }

        @Stable
        fun from(
            rootPostView: RootPostView,
            isFriend: Boolean? = rootPostView.authorIsFriend
        ): TrustType {
            return from(
                isOneself = rootPostView.authorIsOneself,
                isFriend = isFriend ?: rootPostView.authorIsFriend,
                isWebOfTrust = rootPostView.authorIsTrusted,
                isMuted = rootPostView.authorIsMuted,
                isInList = rootPostView.authorIsInList,
                isLocked = rootPostView.authorIsLocked,
            )
        }


        @Stable
        fun from(
            pollView: PollView,
            isFriend: Boolean? = pollView.authorIsFriend
        ): TrustType {
            return from(
                isOneself = pollView.authorIsOneself,
                isFriend = isFriend ?: pollView.authorIsFriend,
                isWebOfTrust = pollView.authorIsTrusted,
                isMuted = pollView.authorIsMuted,
                isInList = pollView.authorIsInList,
                isLocked = pollView.authorIsLocked,
            )
        }

        @Stable
        fun from(
            legacyReplyView: LegacyReplyView,
            isFriend: Boolean? = legacyReplyView.authorIsFriend
        ): TrustType {
            return from(
                isOneself = legacyReplyView.authorIsOneself,
                isFriend = isFriend ?: legacyReplyView.authorIsFriend,
                isWebOfTrust = legacyReplyView.authorIsTrusted,
                isMuted = legacyReplyView.authorIsMuted,
                isInList = legacyReplyView.authorIsInList,
                isLocked = legacyReplyView.authorIsLocked,
            )
        }

        @Stable
        fun from(
            commentView: CommentView,
            isFriend: Boolean? = commentView.authorIsFriend
        ): TrustType {
            return from(
                isOneself = commentView.authorIsOneself,
                isFriend = isFriend ?: commentView.authorIsFriend,
                isWebOfTrust = commentView.authorIsTrusted,
                isMuted = commentView.authorIsMuted,
                isInList = commentView.authorIsInList,
                isLocked = commentView.authorIsLocked,
            )
        }

        @Stable
        fun fromCrossPostAuthor(
            crossPostView: CrossPostView,
            isFriend: Boolean? = crossPostView.authorIsFriend
        ): TrustType {
            return from(
                isOneself = crossPostView.authorIsOneself,
                isFriend = isFriend ?: crossPostView.authorIsFriend,
                isWebOfTrust = crossPostView.authorIsTrusted,
                isMuted = crossPostView.authorIsMuted,
                isInList = crossPostView.authorIsInList,
                isLocked = crossPostView.authorIsLocked,
            )
        }

        @Stable
        fun fromCrossPostedAuthor(
            crossPostView: CrossPostView,
            isFriend: Boolean? = crossPostView.crossPostedAuthorIsFriend
        ): TrustType {
            return from(
                isOneself = crossPostView.crossPostedAuthorIsOneself,
                isFriend = isFriend ?: crossPostView.crossPostedAuthorIsFriend,
                isWebOfTrust = crossPostView.crossPostedAuthorIsTrusted,
                isMuted = crossPostView.crossPostedAuthorIsMuted,
                isInList = crossPostView.crossPostedAuthorIsInList,
                isLocked = crossPostView.crossPostedAuthorIsLocked,
            )
        }
    }
}

data object LockedOneself : TrustType()
data object Oneself : TrustType()
data object FriendTrust : TrustType()
data object IsInListTrust : TrustType()
data object WebTrust : TrustType()
data object Muted : TrustType()
data object NoTrust : TrustType()
data object Locked : TrustType()
