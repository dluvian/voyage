package com.dluvian.voyage.provider

import android.util.Log
import com.dluvian.voyage.MAX_PUBKEYS
import com.dluvian.voyage.NostrService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rust.nostr.sdk.Event
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.Metadata
import rust.nostr.sdk.PublicKey
import rust.nostr.sdk.Timestamp

class NameProvider(private val service: NostrService) {
    private val logTag = "NameProvider"
    private val mutex = Mutex()
    private val names = mutableMapOf<PublicKey, Pair<Timestamp, String>>()

    suspend fun init() {
        val profileFilter = Filter()
            .kind(Kind.fromStd(KindStandard.METADATA))
            .limit(MAX_PUBKEYS.toULong())

        val dbResult = service.dbQuery(profileFilter).toVec()
        if (dbResult.isEmpty()) {
            Log.i(logTag, "No profiles in database")
            return
        }

        val entries = dbResult.map { event ->
            val value = Pair(event.createdAt(), parseName(event))
            Pair(event.author(), value)
        }
        mutex.withLock {
            names.putAll(entries)
        }
    }

    suspend fun update(event: Event) {
        if (event.kind() != Kind.fromStd(KindStandard.METADATA)) return

        val currentTime = mutex.withLock { names[event.author()]?.first?.asSecs() ?: 0u }
        if (event.createdAt().asSecs() <= currentTime) return

        val name = parseName(event)
        mutex.withLock {
            names[event.author()] = Pair(event.createdAt(), name)
        }
    }

    suspend fun names(pubkeys: Collection<PublicKey>): Map<PublicKey, String> {
        return mutex.withLock { names.filter { (pk, _) -> pubkeys.contains(pk) } }
            .map { (pk, timedName) -> Pair(pk, timedName.second) }
            .toMap()
    }

    suspend fun reserve(pubkeys: Collection<PublicKey>, dbOnly: Boolean) {
        val cachedPubkeys = mutex.withLock { names.keys.toSet() }
        val missing = pubkeys.filterNot { cachedPubkeys.contains(it) }
        if (missing.isEmpty()) return

        val profileFilter = Filter()
            .kind(Kind.fromStd(KindStandard.METADATA))
            .authors(missing)
            .limit(missing.size.toULong())
        val dbResult = service.dbQuery(profileFilter).toVec()
        dbResult.forEach { event -> update(event) }
        val dbPubkeys = dbResult.map { it.author() }.toSet()

        val dbMissing = missing.filterNot { dbPubkeys.contains(it) }
        if (dbMissing.isEmpty()) return

        if (!dbOnly) {
            val subFilter = Filter()
                .kind(Kind.fromStd(KindStandard.METADATA))
                .authors(dbMissing)
                .limit(dbMissing.size.toULong())
            service.subscribe(subFilter)
        }
    }

    private fun parseName(event: Event): String {
        val result = runCatching { Metadata.fromJson(event.content()) }
        if (result.isFailure) {
            Log.i(logTag, "Found invalid metadata event from ${event.author()}")
            return ""
        }

        return result.getOrNull()?.getName().orEmpty()
    }
}