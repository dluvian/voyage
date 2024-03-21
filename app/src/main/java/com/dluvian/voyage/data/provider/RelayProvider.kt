package com.dluvian.voyage.data.provider

import android.util.Log
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.WEBSOCKET_PREFIX
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

    fun getObserveRelays(nip19Profile: Nip19Profile): List<RelayUrl> {
        val encodedRelays = nip19Profile.relays()
            .filter { it.startsWith(WEBSOCKET_PREFIX) }
            .map { it.removeTrailingSlashes() }
            .distinct()
            .shuffled()
            .take(MAX_RELAYS)
        return (encodedRelays + getReadRelays()).distinct()
    }


    // TODO: Cache result and use it when applicable
    suspend fun getObserveRelays(observeFrom: Collection<PubkeyHex>): Map<RelayUrl, Set<PubkeyHex>> {
        if (observeFrom.isEmpty()) return emptyMap()

        val result = mutableMapOf<RelayUrl, MutableSet<PubkeyHex>>()

        // Cover pubkey-write-relay pairing
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

        // Cover most useful relays
        eventRelayDao.getEventRelays(authors = observeFrom)
            .sortedByDescending { it.relayCount }
            .distinctBy { it.pubkey }
            .groupBy(keySelector = { it.relay }, valueTransform = { it.pubkey })
            .toList()
            .forEach { (relay, pubkeys) ->
                if (pubkeys.isNotEmpty()) result.putOrAdd(relay, pubkeys)
                pubkeyCache.addAll(pubkeys)
            }

        // Cover rest with my read relays
        val restPubkeys = observeFrom - pubkeyCache
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
