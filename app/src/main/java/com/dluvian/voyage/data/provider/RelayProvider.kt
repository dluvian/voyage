package com.dluvian.voyage.data.provider

import android.util.Log
import androidx.compose.runtime.State
import com.dluvian.nostr_kt.Nip65Relay
import com.dluvian.nostr_kt.NostrClient
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.removeTrailingSlashes
import com.dluvian.voyage.core.MAX_KEYS
import com.dluvian.voyage.core.MAX_KEYS_SQL
import com.dluvian.voyage.core.MAX_POPULAR_RELAYS
import com.dluvian.voyage.core.MAX_RELAYS
import com.dluvian.voyage.core.MAX_RELAYS_PER_PUBKEY
import com.dluvian.voyage.core.MAX_RELAY_CONNECTIONS
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.model.ConnectionStatus
import com.dluvian.voyage.core.model.Disconnected
import com.dluvian.voyage.core.model.Spam
import com.dluvian.voyage.core.putOrAdd
import com.dluvian.voyage.core.takeRandom
import com.dluvian.voyage.data.model.CustomPubkeys
import com.dluvian.voyage.data.model.FriendPubkeys
import com.dluvian.voyage.data.model.ListPubkeys
import com.dluvian.voyage.data.model.PubkeySelection
import com.dluvian.voyage.data.room.dao.EventRelayDao
import com.dluvian.voyage.data.room.dao.Nip65Dao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import rust.nostr.protocol.Nip19Event
import rust.nostr.protocol.Nip19Profile


private const val TAG = "RelayProvider"

class RelayProvider(
    private val nip65Dao: Nip65Dao,
    private val eventRelayDao: EventRelayDao,
    private val nostrClient: NostrClient,
    private val connectionStatuses: State<Map<RelayUrl, ConnectionStatus>>,
    private val friendProvider: FriendProvider,
    private val itemSetProvider: ItemSetProvider,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val myNip65 =
        nip65Dao.getMyNip65Flow().stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun getReadRelays(limit: Int = MAX_RELAYS, includeConnected: Boolean = false): List<RelayUrl> {
        return myNip65.value
            .filter { it.nip65Relay.isRead }
            .map { it.nip65Relay.url }
            .ifEmpty { defaultRelays }
            .preferConnected(limit = limit)
            .let {
                if (includeConnected) (it + nostrClient.getAllConnectedUrls()).distinct() else it
            }
    }

    private fun getWriteRelays(limit: Int = MAX_RELAYS): List<RelayUrl> {
        return myNip65.value
            .filter { it.nip65Relay.isWrite }
            .map { it.nip65Relay.url }
            .ifEmpty { defaultRelays }
            .preferConnected(limit)
    }

    fun getPublishRelays(addConnected: Boolean = true): List<RelayUrl> {
        val relays = getWriteRelays().toMutableSet()
        if (addConnected) relays.addAll(nostrClient.getAllConnectedUrls())

        return relays.toList()
    }

    suspend fun getPublishRelays(
        publishTo: List<PubkeyHex>,
        addConnected: Boolean = true
    ): List<RelayUrl> {
        val relays = if (publishTo.isEmpty()) mutableSetOf()
        else nip65Dao.getReadRelays(pubkeys = publishTo)
            .groupBy { it.pubkey }
            .flatMap { (_, nip65s) ->
                nip65s.map { it.nip65Relay.url }.preferConnected(MAX_RELAYS_PER_PUBKEY)
            }.toMutableSet()
        relays.addAll(getPublishRelays(addConnected = addConnected))

        return relays.toList()
    }

    suspend fun getObserveRelays(
        pubkey: PubkeyHex,
        limit: Int = MAX_RELAYS,
        includeConnected: Boolean = false
    ): List<RelayUrl> {
        val relays = nip65Dao.getWriteRelays(pubkeys = listOf(pubkey))
            .map { it.nip65Relay.url }
            .preferConnected(limit)
            .toMutableSet()
        relays.addAll(getReadRelays(limit = limit))
        if (includeConnected) relays.addAll(nostrClient.getAllConnectedUrls())

        return relays.toList()
    }

    suspend fun getObserveRelays(
        nprofile: Nip19Profile,
        limit: Int = MAX_RELAYS,
        includeConnected: Boolean = false
    ): List<RelayUrl> {
        val foreignRelays = nprofile.relays().normalize().preferConnected(limit = limit)
        val nip65 = getObserveRelays(
            pubkey = nprofile.publicKey().toHex(),
            limit = limit,
            includeConnected = includeConnected
        )

        return (foreignRelays + nip65).distinct()
    }

    suspend fun getObserveRelays(
        nevent: Nip19Event,
        limit: Int = MAX_RELAYS,
        includeConnected: Boolean = false
    ): List<RelayUrl> {
        val foreignRelays = nevent.relays().normalize(limit = limit)
        val pubkey = nevent.author()?.toHex()
        val nip65 = if (pubkey != null) getObserveRelays(
            pubkey = pubkey,
            limit = limit,
            includeConnected = includeConnected
        ) else getReadRelays(includeConnected = includeConnected)

        return (foreignRelays + nip65).distinct()
    }

    suspend fun getObserveRelays(selection: PubkeySelection): Map<RelayUrl, Set<PubkeyHex>> {
        when (selection) {
            is FriendPubkeys, is ListPubkeys -> {}
            is CustomPubkeys -> {
                val pubkeys = selection.pubkeys
                if (pubkeys.isEmpty()) {
                    return emptyMap()
                } else if (pubkeys.size == 1) {
                    val pubkey = pubkeys.first()
                    return getObserveRelays(pubkey = pubkey).associateWith { setOf(pubkey) }
                }
            }
        }

        val result = mutableMapOf<RelayUrl, MutableSet<PubkeyHex>>()
        val connectedRelays = nostrClient.getAllConnectedUrls().toSet()

        val eventRelaysView = when (selection) {
            is FriendPubkeys -> eventRelayDao.getFriendsEventRelayAuthorView()
            is CustomPubkeys -> eventRelayDao.getEventRelayAuthorView(
                authors = selection.pubkeys.takeRandom(MAX_KEYS_SQL)
            )

            is ListPubkeys -> eventRelayDao.getEventRelayAuthorViewFromList(
                identifier = selection.identifier
            )
        }

        val eventRelays = eventRelaysView.map { it.relayUrl }.toSet()

        val writeRelays = when (selection) {
            is FriendPubkeys -> nip65Dao.getFriendsWriteRelays()
            is CustomPubkeys -> nip65Dao.getWriteRelays(
                pubkeys = selection.pubkeys.takeRandom(MAX_KEYS_SQL)
            )

            is ListPubkeys -> nip65Dao.getWriteRelaysFromList(identifier = selection.identifier)
        }

        // Cover pubkey-write-relay pairing
        val pubkeyCache = mutableSetOf<PubkeyHex>()
        writeRelays
            .groupBy { it.nip65Relay.url }
            .asSequence()
            .filter { (relay, _) -> connectionStatuses.value[relay] !is Spam }
            .sortedByDescending { (_, pubkeys) -> pubkeys.size }
            .sortedByDescending { (relay, _) -> eventRelays.contains(relay) }
            .sortedByDescending { (relay, _) -> connectedRelays.contains(relay) }
            .sortedByDescending { (relay, _) -> connectionStatuses.value[relay] !is Disconnected }
            .take(MAX_RELAY_CONNECTIONS)
            .forEach { (relay, nip65Entities) ->
                val maxToAdd = maxOf(0, MAX_KEYS - result[relay].orEmpty().size)
                val newPubkeys = nip65Entities
                    .filterNot { pubkeyCache.contains(it.pubkey) }
                    .takeRandom(maxToAdd)
                    .map { it.pubkey }
                if (newPubkeys.isNotEmpty()) {
                    result.putIfAbsent(relay, newPubkeys.toMutableSet())
                    pubkeyCache.addAll(newPubkeys)
                }
            }

        // Cover most useful relays
        eventRelaysView
            .asSequence()
            .filter { connectionStatuses.value[it.relayUrl] !is Disconnected }
            .sortedByDescending { it.relayCount }
            .sortedByDescending { connectedRelays.contains(it.relayUrl) }
            .distinctBy { it.pubkey }
            .groupBy(keySelector = { it.relayUrl }, valueTransform = { it.pubkey })
            .forEach { (relay, pubkeys) ->
                if (pubkeys.isNotEmpty() && (result.containsKey(relay) || result.size < MAX_RELAY_CONNECTIONS)) {
                    result.putOrAdd(relay, pubkeys)
                    pubkeyCache.addAll(pubkeys)
                }
            }

        // Cover rest with already selected relays and read relays for initial start up
        val restPubkeys = when (selection) {
            is FriendPubkeys -> friendProvider.getFriendPubkeys()
            is CustomPubkeys -> selection.pubkeys
            is ListPubkeys -> itemSetProvider.getPubkeysFromList(identifier = selection.identifier)
        } - pubkeyCache
        if (restPubkeys.isNotEmpty()) {
            Log.w(TAG, "Default to read relays for ${restPubkeys.size} pubkeys")
            getReadRelays()
                .plus(result.keys)
                .distinct()
                .forEach { relay ->
                    val present = result[relay].orEmpty()
                    val maxKeys = MAX_KEYS - present.size
                    result.putOrAdd(relay, restPubkeys.takeRandom(maxKeys))
                }
        }

        Log.i(TAG, "Selected ${result.size} autopilot relays ${result.keys}")

        nostrClient.addRelays(relayUrls = result.keys)

        return result
    }

    suspend fun getNewestCreatedAt() = nip65Dao.getNewestCreatedAt()

    suspend fun getCreatedAt(pubkey: PubkeyHex) = nip65Dao.getNewestCreatedAt(pubkey = pubkey)

    fun getMyNip65(): List<Nip65Relay> {
        return myNip65.value.map { it.nip65Relay }
            .ifEmpty { defaultRelays.map { Nip65Relay(url = it) } }
    }

    suspend fun getPopularRelays() = nip65Dao.getPopularRelays(limit = MAX_POPULAR_RELAYS)

    private fun List<RelayUrl>.preferConnected(limit: Int): List<RelayUrl> {
        if (this.size <= limit) return this

        val connected = nostrClient.getAllConnectedUrls().toSet()
        return this.shuffled().sortedByDescending { connected.contains(it) }.take(limit)
    }

    private fun List<RelayUrl>.normalize(limit: Int = Int.MAX_VALUE): List<RelayUrl> {
        return this.map { it.removeTrailingSlashes() }
            .distinct()
            .take(limit)
    }

    private val defaultRelays = listOf(
        "wss://nos.lol",
        "wss://nostr.einundzwanzig.space",
        "wss://relay.mutinywallet.com",
        "wss://nostr.fmt.wiz.biz",
        "wss://relay.nostr.wirednet.jp",
    )
}
