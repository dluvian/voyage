package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.utils.takeRandom
import com.dluvian.voyage.data.account.IMyPubkeyProvider
import com.dluvian.voyage.data.room.dao.FriendDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class FriendProvider(
    private val friendDao: FriendDao,
    private val myPubkeyProvider: IMyPubkeyProvider,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val friendsNoLock = friendDao.getFriendsNoLockFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun getFriendPubkeysNoLock(max: Int = Int.MAX_VALUE): List<PubkeyHex> {
        return (friendsNoLock.value - myPubkeyProvider.getPubkeyHex()).takeRandom(max)
    }

    suspend fun getFriendsWithMissingContactList() = friendDao.getFriendsWithMissingContactList()

    suspend fun getFriendsWithMissingNip65() = friendDao.getFriendsWithMissingNip65()

    suspend fun getFriendsWithMissingProfile() = friendDao.getFriendsWithMissingProfile()

    // Not named "getMaxCreatedAt" bc there should only be one createdAt available
    suspend fun getCreatedAt() = friendDao.getMaxCreatedAt()

    fun isFriend(pubkey: PubkeyHex): Boolean {
        return getFriendPubkeysNoLock().contains(pubkey)
    }
}
