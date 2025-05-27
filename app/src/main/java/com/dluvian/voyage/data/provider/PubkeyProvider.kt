package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.filterSetting.NoPubkeys
import com.dluvian.voyage.data.filterSetting.PubkeySelection
import com.dluvian.voyage.data.filterSetting.SingularPubkey
import com.dluvian.voyage.data.filterSetting.WebOfTrustPubkeys
import com.dluvian.voyage.filterSetting.CustomPubkeys
import com.dluvian.voyage.filterSetting.FriendPubkeys
import com.dluvian.voyage.filterSetting.Global
import com.dluvian.voyage.filterSetting.ListPubkeys

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
            WebOfTrustPubkeys -> webOfTrustProvider.getFriendsAndWebOfTrustPubkeys()
        }
    }
}
