package com.dluvian.voyage.data.provider

import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.room.dao.FriendDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import rust.nostr.protocol.PublicKey

class FriendProvider(nostrSubscriber: NostrSubscriber, friendDao: FriendDao) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val friends = friendDao.getFriendsFlow()
        .stateIn(scope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        nostrSubscriber.subMyContacts()
    }

    fun getFriendPublicKeys(): List<PublicKey> {
        return friends.value.map { PublicKey.fromHex(it) }
    }
}
