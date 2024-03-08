package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.PubkeyHex

class WebOfTrustProvider(private val friendProvider: FriendProvider) {
    init {
        // TODO: Subscribe missing contactList and rand 25
    }

    fun getWebOfTrustPubkeys(): List<PubkeyHex> {
        // TODO: get from dao + friends
        return friendProvider.getFriendPubkeys()
    }
}
