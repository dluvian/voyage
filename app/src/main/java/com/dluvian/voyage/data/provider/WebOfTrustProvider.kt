package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.takeRandom
import com.dluvian.voyage.data.account.IMyPubkeyProvider
import com.dluvian.voyage.data.room.dao.WebOfTrustDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class WebOfTrustProvider(
    private val myPubkeyProvider: IMyPubkeyProvider,
    private val friendProvider: FriendProvider,
    private val webOfTrustDao: WebOfTrustDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val webOfTrust = webOfTrustDao.getWebOfTrustFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun getFriendsAndWebOfTrustPubkeys(
        includeMyself: Boolean,
        max: Int = Int.MAX_VALUE
    ): List<PubkeyHex> {
        val result = if (includeMyself) mutableListOf(myPubkeyProvider.getPubkeyHex())
        else mutableListOf()

        result.addAll(friendProvider.getFriendPubkeys(max = max))
        result.addAll(webOfTrust.value.takeRandom(max))

        return result.take(max)
    }

    suspend fun getWotWithMissingProfile() = webOfTrustDao.getWotWithMissingProfile()

    suspend fun getNewestCreatedAt(): Long? {
        return webOfTrustDao.getNewestCreatedAt()
    }
}
