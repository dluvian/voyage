package com.dluvian.voyage.core.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.dluvian.voyage.data.room.view.CommentView
import com.dluvian.voyage.data.room.view.CrossPostView
import com.dluvian.voyage.data.room.view.LegacyReplyView
import com.dluvian.voyage.data.room.view.RootPostView

@Immutable
sealed class TrustType {
    companion object {
        @Stable
        fun from(
            isOneself: Boolean,
            isFriend: Boolean,
            isWebOfTrust: Boolean,
            isInList: Boolean,
        ): TrustType {
            return if (isOneself) Oneself
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
                isInList = rootPostView.authorIsInList,
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
                isInList = legacyReplyView.authorIsInList,
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
                isInList = commentView.authorIsInList,
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
                isInList = crossPostView.authorIsInList,
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
                isInList = crossPostView.crossPostedAuthorIsInList,
            )
        }
    }
}

data object Oneself : TrustType()
data object FriendTrust : TrustType()
data object IsInListTrust : TrustType()
data object WebTrust : TrustType()
data object NoTrust : TrustType()
