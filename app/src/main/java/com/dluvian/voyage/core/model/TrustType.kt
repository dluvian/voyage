package com.dluvian.voyage.core.model

sealed class TrustType {
    companion object {
        fun from(isFriend: Boolean, isWebOfTrust: Boolean): TrustType {
            return if (isFriend) FriendTrust
            else if (isWebOfTrust) WebTrust
            else NoTrust
        }
    }
}

data object FriendTrust : TrustType()
data object WebTrust : TrustType()
data object NoTrust : TrustType()