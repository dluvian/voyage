package com.dluvian.voyage.data.provider

import android.util.Log
import com.dluvian.nostr_kt.createNprofile
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.SHORT_DEBOUNCE
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.room.dao.ProfileDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "NameProvider"

class NameProvider(
    private val profileDao: ProfileDao,
    private val nameCache: MutableMap<PubkeyHex, String?>,
    private val nostrSubscriber: NostrSubscriber,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val subCache = Collections.synchronizedSet(mutableSetOf<PubkeyHex>())
    private val isActive = AtomicBoolean(false)

    fun getName(pubkey: PubkeyHex): String? {
        val name = nameCache[pubkey]
        if (!name.isNullOrEmpty()) return name

        if (isActive.compareAndSet(false, true)) {
            scope.launchIO {
                val dbName = profileDao.getName(pubkey = pubkey)
                if (dbName.isNullOrEmpty()) {
                    if (subCache.add(pubkey)) {
                        Log.d(TAG, "Sub unknown profile $pubkey")
                        nostrSubscriber.subProfile(nprofile = createNprofile(hex = pubkey))
                    }
                } else {
                    Log.d(TAG, "Found profile $pubkey in database")
                    nameCache[pubkey] = dbName
                }
                delay(SHORT_DEBOUNCE)
            }.invokeOnCompletion { isActive.set(false) }
        }

        return name
    }
}
