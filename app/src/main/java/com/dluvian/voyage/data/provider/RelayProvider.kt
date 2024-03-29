package com.dluvian.voyage.data.provider

import android.util.Log
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.removeTrailingSlashes
import com.dluvian.voyage.core.MAX_RELAYS
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.putOrAdd
import com.dluvian.voyage.data.room.dao.EventRelayDao
import com.dluvian.voyage.data.room.dao.Nip65Dao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import rust.nostr.protocol.Nip19Profile


class RelayProvider(
    private val nip65Dao: Nip65Dao,
    private val eventRelayDao: EventRelayDao,
) {
    private val tag = "RelayProvider"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val myNip65 = nip65Dao.getMyNip65().stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun getReadRelays(limit: Boolean = true): List<RelayUrl> {
        return myNip65.value
            .filter { it.nip65Relay.isRead }
            .map { it.nip65Relay.url }
            .ifEmpty { defaultRelays }
            .let { if (limit) it.limit() else it }
    }

    fun getWriteRelays(limit: Boolean = true): List<RelayUrl> {
        return myNip65.value
            .filter { it.nip65Relay.isWrite }
            .map { it.nip65Relay.url }
            .ifEmpty { defaultRelays }
            .let { if (limit) it.limit() else it }
    }

    suspend fun getPublishRelays(publishTo: PubkeyHex): List<RelayUrl> {
        val foreignRelays = nip65Dao.getReadRelays(pubkey = publishTo).limit()
        return (getWriteRelays(limit = true) + foreignRelays).distinct()
    }

    private suspend fun getObserveRelays(pubkey: PubkeyHex, limit: Boolean = true): List<RelayUrl> {
        val relays = nip65Dao.getNip65WriteRelays(pubkeys = listOf(pubkey))
            .map { it.nip65Relay.url }
            .let { if (limit) it.limit() else it }
            .toMutableSet()
        relays.addAll(getReadRelays(limit = limit))

        return relays.toList()
    }

    suspend fun getObserveRelays(nprofile: Nip19Profile, limit: Boolean = true): List<RelayUrl> {
        val foreignRelays = nprofile.relays()
            .let { if (limit) it.limit() else it }
            .map { it.removeTrailingSlashes() }
        val nip65 = getObserveRelays(pubkey = nprofile.publicKey().toHex(), limit = limit)

        return (foreignRelays + nip65).distinct()
    }

    // TODO: Cache result and use it when applicable
    suspend fun getAutopilotRelays(pubkeys: Collection<PubkeyHex>): Map<RelayUrl, Set<PubkeyHex>> {
        if (pubkeys.isEmpty()) return emptyMap()

        val result = mutableMapOf<RelayUrl, MutableSet<PubkeyHex>>()

        // Cover pubkey-write-relay pairing
        // TODO: Filter bad relays / blacklisted relays
        val pubkeyCache = mutableSetOf<PubkeyHex>()
        nip65Dao
            .getNip65WriteRelays(pubkeys = pubkeys)
            .groupBy { it.nip65Relay.url }
            .toList()
            .shuffled()
            .sortedByDescending { (_, pubkeys) -> pubkeys.size }
            .forEach { (relay, nip65Entities) ->
                val newPubkeys = nip65Entities.map { it.pubkey }.toSet() - pubkeyCache
                if (newPubkeys.isNotEmpty()) {
                    result.putIfAbsent(relay, newPubkeys.toMutableSet())
                    pubkeyCache.addAll(newPubkeys)
                }
            }

        // Cover most useful relays
        eventRelayDao.getEventRelays(authors = pubkeys)
            .sortedByDescending { it.relayCount }
            .distinctBy { it.pubkey }
            .groupBy(keySelector = { it.relay }, valueTransform = { it.pubkey })
            .toList()
            .forEach { (relay, pubkeys) ->
                if (pubkeys.isNotEmpty()) result.putOrAdd(relay, pubkeys)
                pubkeyCache.addAll(pubkeys)
            }

        // Cover rest with my read relays
        val restPubkeys = pubkeys - pubkeyCache
        if (restPubkeys.isNotEmpty()) {
            Log.i(tag, "Default to read relays for ${restPubkeys.size} pubkeys")
            getReadRelays().forEach { relay -> result.putOrAdd(relay, restPubkeys) }
        }

        Log.i(tag, "Selected ${result.size} observe relays")

        return result
    }

    private fun List<RelayUrl>.limit(): List<RelayUrl> {
        return if (this.size > MAX_RELAYS) this.shuffled().take(MAX_RELAYS)
        else this
    }

    private val defaultRelays = listOf(
        "wss://nostr.einundzwanzig.space",
        "wss://nostr.oxtr.dev",
        "wss://relay.mutinywallet.com",
        "wss://nostr.fmt.wiz.biz",
        "wss://relay.nostr.wirednet.jp",
    )
}
