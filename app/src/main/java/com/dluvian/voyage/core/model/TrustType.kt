package com.dluvian.voyage.core.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
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
        fun from(rootPostView: RootPostView): TrustType {
            return from(
                isOneself = rootPostView.authorIsOneself,
                isFriend = rootPostView.authorIsFriend,
                isWebOfTrust = rootPostView.authorIsTrusted,
                isMuted = rootPostView.authorIsMuted,
                isInList = rootPostView.authorIsInList,
                isLocked = rootPostView.authorIsLocked,
            )
        }

        @Stable
        fun from(legacyReplyView: LegacyReplyView): TrustType {
            return from(
                isOneself = legacyReplyView.authorIsOneself,
                isFriend = legacyReplyView.authorIsFriend,
                isWebOfTrust = legacyReplyView.authorIsTrusted,
                isMuted = legacyReplyView.authorIsMuted,
                isInList = legacyReplyView.authorIsInList,
                isLocked = legacyReplyView.authorIsLocked,
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
