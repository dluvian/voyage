package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.model.CustomPubkeys
import com.dluvian.voyage.data.model.FriendPubkeys
import com.dluvian.voyage.data.model.Global
import com.dluvian.voyage.data.model.ListPubkeys
import com.dluvian.voyage.data.model.NoPubkeys
import com.dluvian.voyage.data.model.PubkeySelection
import com.dluvian.voyage.data.model.SingularPubkey
import com.dluvian.voyage.data.model.WebOfTrustPubkeys

class PubkeyProvider(
    private val friendProvider: FriendProvider,
    private val webOfTrustProvider: WebOfTrustProvider,
) {
    lateinit var itemSetProvider: ItemSetProvider

    suspend fun getPubkeys(selection: PubkeySelection): List<PubkeyHex> {
        return when (selection) {
            is CustomPubkeys -> selection.pubkeys.toList()
            FriendPubkeys -> friendProvider.getFriendPubkeys()
            is ListPubkeys -> itemSetProvider.getPubkeysFromList(identifier = selection.identifier)
            is SingularPubkey -> selection.asList()
            Global -> emptyList()
            NoPubkeys -> emptyList()
            WebOfTrustPubkeys -> webOfTrustProvider.getFriendsAndWebOfTrustPubkeys(includeMyself = false)
        }
    }
}
