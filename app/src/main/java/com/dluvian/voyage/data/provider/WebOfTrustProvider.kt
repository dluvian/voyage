package com.dluvian.voyage.data.provider

import com.dluvian.voyage.data.room.dao.WebOfTrustDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import rust.nostr.protocol.PublicKey

class WebOfTrustProvider(webOfTrustDao: WebOfTrustDao) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val webOfTrust = webOfTrustDao.getWebOfTrustFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun getWebOfTrustPubkeys(): List<PublicKey> {
        return webOfTrust.value.map { PublicKey.fromHex(it) }
    }
}
