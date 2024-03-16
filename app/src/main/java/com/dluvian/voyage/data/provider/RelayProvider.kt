package com.dluvian.voyage.data.provider

import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.MAX_RELAYS
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.dao.Nip65Dao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn


class RelayProvider(private val nip65Dao: Nip65Dao) {
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

    suspend fun getObserveRelays(observeFrom: Collection<PubkeyHex>): Map<RelayUrl, Set<PubkeyHex>> {
        if (observeFrom.isEmpty()) return emptyMap()

        val result = mutableMapOf<RelayUrl, MutableSet<PubkeyHex>>()

        // Cover all pubkeys with my (limited) read relays
        getReadRelays().forEach { relay -> result[relay] = observeFrom.toMutableSet() }

        // Cover pubkey-write-relay pairing if not already covered by my read relays
        // TODO: Filter bad relays / blacklisted relays
        val pubkeyCache = mutableSetOf<PubkeyHex>()
        nip65Dao
            .getNip65WriteRelays(pubkeys = observeFrom)
            .groupBy { it.nip65Relay.url }
            .toList()
            .shuffled()
            .sortedByDescending { (_, pubkeys) -> pubkeys.size }
            .sortedBy { (relay, _) -> avoidRelays.contains(relay) } // Avoid centralizing relays
            .forEach { (relay, nip65Entities) ->
                val newPubkeys = nip65Entities.map { it.pubkey }.toSet() - pubkeyCache
                if (newPubkeys.isNotEmpty()) {
                    result.putIfAbsent(relay, newPubkeys.toMutableSet())
                    pubkeyCache.addAll(newPubkeys)
                }
            }

        return result
    }

    fun getAutopilotRelays(pubkeys: Collection<PubkeyHex>): Map<RelayUrl, Set<PubkeyHex>> {
        if (pubkeys.isEmpty()) return emptyMap()

        return getReadRelays().associateWith { pubkeys.toSet() } // TODO: Autopilot implementation
    }

    private fun List<RelayUrl>.limit(): List<RelayUrl> {
        return if (this.size > MAX_RELAYS) this.shuffled().take(MAX_RELAYS)
        else this
    }

    private val defaultRelays = listOf(
        "wss://nos.lol",
        "wss://nostr.einundzwanzig.space",
        "wss://relay.primal.net",
        "wss://nostr.oxtr.dev",
        "wss://relay.mutinywallet.com",
        "wss://nostr.fmt.wiz.biz",
        "wss://relay.nostr.wirednet.jp",
    )

    // Avoid big relays. Don't be reliable on central hubs
    private val avoidRelays = listOf(
        "wss://nos.lol",
        "wss://relay.damus.io"
    )
}
