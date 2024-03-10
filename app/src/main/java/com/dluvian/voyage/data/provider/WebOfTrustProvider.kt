package com.dluvian.voyage.data.provider

import rust.nostr.protocol.PublicKey

class WebOfTrustProvider(private val friendProvider: FriendProvider) {
    init {
        // TODO: Subscribe missing contactList and rand 25
    }

    fun getWebOfTrustPubkeys(): List<PublicKey> {
        // TODO: get from dao + friends
        return friendProvider.getFriendPublicKeys()
    }
}
