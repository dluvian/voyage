package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.nostr_kt.INostrListener
import com.dluvian.nostr_kt.NostrClient
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.SubId
import com.dluvian.voyage.data.event.EventMaker
import com.dluvian.voyage.data.event.EventQueue
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter
import rust.nostr.protocol.PublicKey

class NostrService(
    private val nostrClient: NostrClient,
    private val eventQueue: EventQueue,
    private val eventMaker: EventMaker,
    private val filterCache: MutableMap<SubId, List<Filter>>,
) {
    private val tag = "NostrService"

    private val listener = object : INostrListener {
        override fun onOpen(relayUrl: RelayUrl, msg: String) {
            Log.i(tag, "OnOpen($relayUrl): $msg")
        }

        override fun onEvent(subId: SubId, event: Event, relayUrl: RelayUrl?) {
            eventQueue.submit(event = event, subId = subId, relayUrl = relayUrl)
        }

        override fun onError(relayUrl: RelayUrl, msg: String, throwable: Throwable?) {
            Log.w(tag, "OnError($relayUrl): $msg", throwable)
        }

        override fun onEOSE(relayUrl: RelayUrl, subId: SubId) {
            Log.d(tag, "OnEOSE($relayUrl): $subId")
            nostrClient.unsubscribe(subId)
        }

        override fun onClosed(relayUrl: RelayUrl, subId: SubId, reason: String) {
            Log.d(tag, "OnClosed($relayUrl): $subId, reason: $reason")
        }

        override fun onClose(relayUrl: RelayUrl, reason: String) {
            Log.i(tag, "OnClose($relayUrl): $reason")
        }

        override fun onFailure(relayUrl: RelayUrl, msg: String?, throwable: Throwable?) {
            Log.w(tag, "OnFailure($relayUrl): $msg", throwable)
        }

        override fun onOk(relayUrl: RelayUrl, eventId: EventId, accepted: Boolean, msg: String) {
            Log.d(
                tag,
                "OnOk($relayUrl): ${eventId.toHex()}, accepted=$accepted, ${msg.ifBlank { "No message" }}"
            )
        }

        override fun onAuth(relayUrl: RelayUrl, challengeString: String) {
            Log.d(tag, "OnAuth($relayUrl): challenge=$challengeString")
        }
    }

    fun initialize(initRelayUrls: Collection<RelayUrl>) {
        nostrClient.setListener(listener)
        Log.i(tag, "Add ${initRelayUrls.size} relays: $initRelayUrls")
        nostrClient.addRelays(initRelayUrls)
    }

    suspend fun publishPost(
        title: String,
        content: String,
        topic: String,
        relayUrls: Collection<RelayUrl>
    ): Result<Event> {
        return eventMaker.buildPost(title, content, topic)
            .onSuccess { nostrClient.publishToRelays(event = it, relayUrls = relayUrls) }
    }


    suspend fun publishVote(
        eventId: EventId,
        pubkey: PublicKey,
        isPositive: Boolean,
        kind: Int,
        relayUrls: Collection<RelayUrl>,
    ): Result<Event> {
        return eventMaker.buildVote(
            eventId = eventId,
            pubkey = pubkey,
            isPositive = isPositive,
            kind = kind
        )
            .onSuccess { nostrClient.publishToRelays(event = it, relayUrls = relayUrls) }
    }

    suspend fun publishDelete(eventId: EventId, relayUrls: Collection<RelayUrl>): Result<Event> {
        val allRelays = nostrClient.getAllConnectedUrls() + relayUrls
        return eventMaker.buildDelete(eventId = eventId)
            .onSuccess { nostrClient.publishToRelays(event = it, relayUrls = allRelays) }
    }

    fun subscribe(filters: List<Filter>, relayUrl: RelayUrl): SubId? {
        if (filters.isEmpty()) return null
        Log.d(tag, "Subscribe ${filters.size} in $relayUrl")

        val subId = nostrClient.subscribe(filters = filters, relayUrl = relayUrl)
        if (subId == null) {
            Log.w(tag, "Failed to create subscription ID")
            return null
        }
        filterCache[subId] = filters

        return subId
    }

    fun unsubscribe(subIds: Collection<SubId>) {
        subIds.forEach {
            nostrClient.unsubscribe(it)
            filterCache.remove(it)
        }
    }

    fun close() {
        filterCache.clear()
        nostrClient.close()
    }
}