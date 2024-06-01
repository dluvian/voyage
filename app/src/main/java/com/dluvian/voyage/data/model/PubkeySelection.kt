package com.dluvian.voyage.data.model

import com.dluvian.voyage.core.PubkeyHex

sealed class PubkeySelection

data object FriendPubkeys : PubkeySelection()
data class CustomPubkeys(val pubkeys: Collection<PubkeyHex>) : PubkeySelection()
