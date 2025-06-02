package com.dluvian.voyage.filterSetting

sealed class PubkeySelection

data object NoPubkeys : PubkeySelection()
data object FriendPubkeys : PubkeySelection()
data object Global : PubkeySelection()
