package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.dao.MuteDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class MuteProvider(muteDao: MuteDao) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val mutedProfiles = muteDao.getMyProfileMutesFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun isMuted(pubkey: PubkeyHex): Boolean {
        return mutedProfiles.value.contains(pubkey)
    }
}
