package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.utils.takeRandom
import com.dluvian.voyage.data.room.dao.WebOfTrustDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class WebOfTrustProvider(
    private val friendProvider: FriendProvider,
    private val webOfTrustDao: WebOfTrustDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val webOfTrust = webOfTrustDao.getWebOfTrustFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun getFriendsAndWebOfTrustPubkeys(
        max: Int = Int.MAX_VALUE,
        friendsFirst: Boolean = true,
    ): List<PubkeyHex> {
        val result = mutableListOf<PubkeyHex>()

        result.addAll(friendProvider.getFriendPubkeys(max = max))
        result.addAll(webOfTrust.value.minus(result.toSet()).takeRandom(max))

        return if (friendsFirst) {
            result.take(max)
        } else {
            result.takeRandom(max)
        }
    }

    suspend fun getWotWithMissingProfile() = webOfTrustDao.getWotWithMissingProfile()

    suspend fun getNewestCreatedAt(): Long? {
        return webOfTrustDao.getNewestCreatedAt()
    }
}
