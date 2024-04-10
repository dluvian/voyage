package com.dluvian.voyage.core.model

sealed class TrustType {
    companion object {
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