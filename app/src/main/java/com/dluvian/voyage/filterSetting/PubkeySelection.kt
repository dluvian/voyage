package com.dluvian.voyage.data.filterSetting

import com.dluvian.voyage.core.PubkeyHex

sealed class PubkeySelection

sealed class FeedPubkeySelection : PubkeySelection()

data object NoPubkeys : FeedPubkeySelection()
data object FriendPubkeys : FeedPubkeySelection()
data object WebOfTrustPubkeys : FeedPubkeySelection()
data object Global : FeedPubkeySelection()

data class CustomPubkeys(val pubkeys: Collection<PubkeyHex>) : PubkeySelection()
data class ListPubkeys(val identifier: String) : PubkeySelection()
data class SingularPubkey(val pubkey: PubkeyHex) : PubkeySelection() {
    fun asList() = listOf(pubkey)
}
