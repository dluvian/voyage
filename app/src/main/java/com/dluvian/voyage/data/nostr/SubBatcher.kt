package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.createReplyAndVoteFilters
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.syncedPutOrAdd
import com.dluvian.voyage.data.model.FilterWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter
import rust.nostr.protocol.Kind
import rust.nostr.protocol.KindEnum
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.Timestamp
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "SubBatcher"
private const val BATCH_DELAY = 2 * DEBOUNCE

class SubBatcher(private val subCreator: SubscriptionCreator) {
    private val idQueue = mutableMapOf<RelayUrl, MutableSet<EventIdHex>>()
    private val profileQueue = mutableMapOf<RelayUrl, MutableSet<PubkeyHex>>()
    private val votePubkeyQueue = mutableSetOf<PubkeyHex>()
    private val isProcessingSubs = AtomicBoolean(false)
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        startProcessingJob()
    }

    fun submitProfile(relayUrl: RelayUrl, pubkey: PubkeyHex) {
        submitProfiles(relayUrl, listOf(pubkey))
    }

    fun submitProfiles(relayUrl: RelayUrl, pubkeys: Collection<PubkeyHex>) {
        if (pubkeys.isEmpty()) return

        synchronized(votePubkeyQueue) {
            val present = profileQueue.putIfAbsent(relayUrl, pubkeys.toMutableSet())
            present?.addAll(pubkeys)
        }
        startProcessingJob()
    }

    fun submitVotesAndReplies(
        relayUrl: RelayUrl,
        eventIds: List<EventIdHex>,
        votePubkeys: List<PubkeyHex>
    ) {
        if (eventIds.isEmpty()) return

        idQueue.syncedPutOrAdd(relayUrl, eventIds)
        synchronized(votePubkeyQueue) {
            if (votePubkeyQueue.isEmpty()) votePubkeyQueue.addAll(votePubkeys)
        }
        startProcessingJob()
    }

    private fun startProcessingJob() {
        if (!isProcessingSubs.compareAndSet(false, true)) return
        Log.i(TAG, "Start job")
        scope.launchIO {
            while (true) {
                delay(BATCH_DELAY)

                val idsByRelay = mutableMapOf<RelayUrl, Set<EventIdHex>>()
                val votePubkeys = mutableListOf<PubkeyHex>()
                val profilePubkeysByRelay = mutableMapOf<RelayUrl, Set<PubkeyHex>>()
                synchronized(idQueue) {
                    idsByRelay.putAll(idQueue)
                    idQueue.clear()
                }
                synchronized(votePubkeyQueue) {
                    votePubkeys.addAll(votePubkeyQueue)
                    votePubkeyQueue.clear()
                }
                synchronized(profileQueue) {
                    profilePubkeysByRelay.putAll(profileQueue)
                    profileQueue.clear()
                }

                val timestamp = Timestamp.now()
                val votePublicKeys = votePubkeys.map { PublicKey.fromHex(it) }

                val replyAndVoteFilters = getReplyAndVoteFilters(
                    idsByRelay = idsByRelay,
                    votePubkeys = votePublicKeys,
                    timestamp = timestamp
                )
                val profileFilters = getProfileFilters(
                    pubkeysByRelay = profilePubkeysByRelay,
                    timestamp = timestamp
                )
                merge(replyAndVoteFilters, profileFilters).forEach { (relay, filters) ->
                    Log.d(TAG, "Sub ${filters.size} filters in $relay")
                    subCreator.subscribe(relayUrl = relay, filters = filters)
                }
            }
        }.invokeOnCompletion {
            Log.w(TAG, "Processing job completed", it)
            isProcessingSubs.set(false)
        }
    }

    private fun getReplyAndVoteFilters(
        idsByRelay: Map<RelayUrl, Set<EventIdHex>>,
        votePubkeys: List<PublicKey>,
        timestamp: Timestamp,
    ): Map<RelayUrl, List<FilterWrapper>> {
        val convertedIds = mutableMapOf<EventIdHex, EventId>()

        return idsByRelay.mapValues { (_, ids) ->
            val eventIds = ids.map {
                val id = convertedIds[it] ?: EventId.fromHex(it)
                convertedIds.putIfAbsent(it, id) ?: id
            }
            createReplyAndVoteFilters(
                ids = eventIds,
                votePubkeys = votePubkeys,
                timestamp = timestamp
            )
        }
    }

    private fun getProfileFilters(
        pubkeysByRelay: Map<RelayUrl, Set<PubkeyHex>>,
        timestamp: Timestamp
    ): Map<RelayUrl, List<FilterWrapper>> {
        val convertedPubkeys = mutableMapOf<PubkeyHex, PublicKey>()

        return pubkeysByRelay.mapValues { (_, pubkeys) ->
            val publicKeys = pubkeys.map {
                val pubkey = convertedPubkeys[it] ?: PublicKey.fromHex(it)
                convertedPubkeys.putIfAbsent(it, pubkey) ?: pubkey
            }
            val filter = Filter().kind(Kind.fromEnum(KindEnum.Metadata))
                .authors(authors = publicKeys)
                .until(timestamp = timestamp)
                .limit(limit = publicKeys.size.toULong())
            listOf(FilterWrapper(filter))
        }
    }

    private fun merge(vararg maps: Map<RelayUrl, List<FilterWrapper>>): Map<RelayUrl, List<FilterWrapper>> {
        val result = mutableMapOf<RelayUrl, MutableList<FilterWrapper>>()
        for (map in maps) {
            map.forEach { (relay, filters) ->
                val present = result.putIfAbsent(relay, filters.toMutableList())
                present?.addAll(filters)
            }
        }

        return result
    }
}
