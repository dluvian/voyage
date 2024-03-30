package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.MAX_PUBKEYS
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.takeRandom
import com.dluvian.voyage.data.room.dao.WebOfTrustDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import rust.nostr.protocol.PublicKey

class WebOfTrustProvider(private val webOfTrustDao: WebOfTrustDao) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val webOfTrust = webOfTrustDao.getWebOfTrustFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun getWebOfTrustPubkeys(limited: Boolean = true, max: Int = MAX_PUBKEYS): List<PublicKey> {
        return webOfTrust.value
            .let { if (limited) it.takeRandom(max) else it }
            .map { PublicKey.fromHex(it) }
    }

    suspend fun getWotWithMissingProfiles(): List<PubkeyHex> {
        return webOfTrustDao.getWotWithMissingProfiles().takeRandom(MAX_PUBKEYS)
    }
}
