package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.MAX_PUBKEYS
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.takeRandom
import com.dluvian.voyage.data.room.dao.WebOfTrustDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class WebOfTrustProvider(private val webOfTrustDao: WebOfTrustDao) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val webOfTrust = webOfTrustDao.getWebOfTrustFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun getWebOfTrustPubkeys(max: Int = MAX_PUBKEYS): List<PubkeyHex> {
        return webOfTrust.value.takeRandom(max)
    }

    suspend fun getWotWithMissingProfile() = webOfTrustDao.getWotWithMissingProfile()

    suspend fun getNewestCreatedAt(): Long? {
        return webOfTrustDao.getNewestCreatedAt()
    }
}
