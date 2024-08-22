package com.dluvian.voyage.data.provider

import android.util.Log
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.data.inMemory.MetadataInMemory
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.room.dao.ProfileDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import rust.nostr.protocol.Nip19Profile
import rust.nostr.protocol.PublicKey
import java.util.Collections

private const val TAG = "NameProvider"

class NameProvider(
    private val profileDao: ProfileDao,
    private val metadataInMemory: MetadataInMemory,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val nameCache = Collections.synchronizedMap(mutableMapOf<PublicKey, String?>())
    private val subCache = Collections.synchronizedSet(mutableSetOf<PubkeyHex>())
    private val jobs = Collections.synchronizedMap(mutableMapOf<PublicKey, Job>())
    lateinit var lazyNostrSubscriber: LazyNostrSubscriber


    fun getName(nprofile: Nip19Profile): String? {
        val name = nameCache[nprofile.publicKey()]
        if (!name.isNullOrEmpty()) return name

        val hex = nprofile.publicKey().toHex()
        val inMemoryName = metadataInMemory.getMetadata(pubkey = hex)?.name
        if (!inMemoryName.isNullOrEmpty()) {
            Log.d(TAG, "Found profile $hex in memory")
            nameCache[nprofile.publicKey()] = inMemoryName
            return inMemoryName
        }

        if (jobs[nprofile.publicKey()]?.isActive == true) return name
        jobs[nprofile.publicKey()] = scope.launchIO {
            val dbName = profileDao.getName(pubkey = hex)
            if (!dbName.isNullOrEmpty()) {
                Log.d(TAG, "Found profile $hex in database")
                nameCache[nprofile.publicKey()] = dbName
                return@launchIO
            }

            if (subCache.add(hex)) {
                Log.d(TAG, "Sub unknown profile $hex")
                lazyNostrSubscriber.semiLazySubProfile(nprofile = nprofile)
            }
        }

        return name
    }
}
