package com.dluvian.voyage.core.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
sealed class TrustType {
    companion object {
        @Stable
        fun from(isOneself: Boolean, isFriend: Boolean, isWebOfTrust: Boolean): TrustType {
            return if (isOneself) Oneself
            else if (isFriend) FriendTrust
            else if (isWebOfTrust) WebTrust
            else NoTrust
        }
    }
}

data object Oneself : TrustType()
data object FriendTrust : TrustType()
data object WebTrust : TrustType()
data object NoTrust : TrustType()