package com.dluvian.voyage.data.model

import com.dluvian.voyage.core.PubkeyHex

sealed class PubkeySelection

sealed class HomeFeedPubkeySelection : PubkeySelection()

data object NoPubkeys : HomeFeedPubkeySelection()
data object FriendPubkeys : HomeFeedPubkeySelection()
data object WebOfTrustPubkeys : HomeFeedPubkeySelection()
data object Global : HomeFeedPubkeySelection()

data class CustomPubkeys(val pubkeys: Collection<PubkeyHex>) : PubkeySelection()
data class ListPubkeys(val identifier: String) : PubkeySelection()
data class SingularPubkey(val pubkey: PubkeyHex) : PubkeySelection() {
    fun asList() = listOf(pubkey)
}
