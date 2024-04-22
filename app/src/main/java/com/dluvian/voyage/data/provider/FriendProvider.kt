package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.takeRandom
import com.dluvian.voyage.data.account.IPubkeyProvider
import com.dluvian.voyage.data.room.dao.FriendDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class FriendProvider(
    private val friendDao: FriendDao,
    private val pubkeyProvider: IPubkeyProvider,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val friends = friendDao.getFriendsFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun getFriendPubkeys(maxPercentage: Float? = null): List<PubkeyHex> {
        val adjustedMax = if (maxPercentage == null) null
        else maxOf(1, (maxPercentage * friends.value.size).toInt())

        return (friends.value - pubkeyProvider.getPubkeyHex())
            .let { if (adjustedMax != null) it.takeRandom(adjustedMax) else it }
    }

    suspend fun getFriendsWithMissingContactList() = friendDao.getFriendsWithMissingContactList()

    suspend fun getFriendsWithMissingNip65() = friendDao.getFriendsWithMissingNip65()

    // Not named "getMaxCreatedAt" bc there should only be one createdAt available
    suspend fun getCreatedAt() = friendDao.getMaxCreatedAt()

    fun isFriend(pubkey: PubkeyHex): Boolean {
        return getFriendPubkeys(maxPercentage = null).contains(pubkey)
    }
}
