package com.dluvian.nostr_kt

import android.util.Log
import okhttp3.OkHttpClient
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter
import java.util.Collections

class NostrService(eventProcessor: EventProcessor) {
    private val tag = "NostrService"
    private val client = NostrClient(httpClient = OkHttpClient())
    private val unsubOnEOSECache = Collections.synchronizedSet(mutableSetOf<String>())

    private val listener = object : INostrListener {
        override fun onOpen(relayUrl: RelayUrl, msg: String) {
            Log.i(tag, "OnOpen($relayUrl): $msg")
        }

        override fun onEvent(subId: SubId, event: Event, relayUrl: RelayUrl?) {
            eventProcessor.submit(event = event, subId = subId, relayUrl = relayUrl)
        }

        override fun onError(relayUrl: RelayUrl, msg: String, throwable: Throwable?) {
            Log.w(tag, "OnError($relayUrl): $msg", throwable)
        }

        override fun onEOSE(relayUrl: RelayUrl, subId: SubId) {
            Log.d(tag, "OnEOSE($relayUrl): $subId")
            if (unsubOnEOSECache.remove(subId)) {
                Log.d(tag, "Unsubscribe onEOSE($relayUrl) $subId")
                client.unsubscribe(subId)
            }
        }

        override fun onClosed(relayUrl: RelayUrl, subId: SubId, reason: String) {
            Log.d(tag, "OnClosed($relayUrl): $subId, reason: $reason")
            unsubOnEOSECache.remove(subId)
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
            sendAuthentication(relayUrl = relayUrl, challengeString = challengeString)
        }
    }

    fun initialize(initRelayUrls: Collection<RelayUrl>) {
        client.setListener(listener)
        Log.i(tag, "Add ${initRelayUrls.size} relays: $initRelayUrls")
        client.addRelays(initRelayUrls)
    }

    fun sendPost(): Event {
        TODO()
    }

    fun sendVote(isPositive: Boolean): Event {
        TODO()
    }

    fun sendReply(): Event {
        TODO()
    }

    fun deleteEvent() {
        TODO()
    }

    fun subscribe(filters: List<Filter>, relayUrl: RelayUrl): SubId? {
        val subId = client.subscribe(filters = filters, relayUrl = relayUrl)
        if (subId == null) {
            Log.w(tag, "Failed to create subscription ID")
            return null
        }
        unsubOnEOSECache.add(subId)

        return subId
    }

    fun unsubscribe(subIds: Collection<SubId>) {
        if (subIds.isEmpty()) return

        subIds.forEach {
            client.unsubscribe(it)
        }
    }

    fun getActiveRelays(): List<RelayUrl> {
        return client.getAllConnectedUrls()
    }

    fun close() {
        Log.i(tag, "Close connections")
        client.close()
    }

    private fun sendAuthentication(relayUrl: RelayUrl, challengeString: String) {
        TODO()
    }
}