package com.dluvian.voyage.filterSetting

import rust.nostr.sdk.PublicKey


sealed class PubkeySelection

sealed class FeedPubkeySelection : PubkeySelection()

data object NoPubkeys : FeedPubkeySelection()
data object FriendPubkeys : FeedPubkeySelection()
data object Global : FeedPubkeySelection()

data class CustomPubkeys(val pubkeys: Collection<PublicKey>) : PubkeySelection()
data class ListPubkeys(val identifier: String) : PubkeySelection()
data class SingularPubkey(val pubkey: PublicKey) : PubkeySelection() {
    fun asList() = listOf(pubkey)
}
