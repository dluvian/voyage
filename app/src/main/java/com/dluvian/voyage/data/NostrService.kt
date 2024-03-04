package com.dluvian.voyage.data

import android.util.Log
import com.dluvian.nostr_kt.INostrListener
import com.dluvian.nostr_kt.NostrClient
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.SubId
import com.dluvian.nostr_kt.createHashtagTag
import com.dluvian.nostr_kt.createKindTag
import com.dluvian.nostr_kt.createTitleTag
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventBuilder
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter
import rust.nostr.protocol.Keys
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.Timestamp
import java.util.Collections

class NostrService(
    private val nostrClient: NostrClient,
    private val eventProcessor: EventProcessor,
    private val singleUseKeyManager: SingleUseKeyManager,
    private val filterCache: MutableMap<SubId, List<Filter>>,
) {
    private val tag = "NostrService"
    private val unsubOnEOSECache = Collections.synchronizedSet(mutableSetOf<SubId>())

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
                nostrClient.unsubscribe(subId)
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
        }
    }

    fun initialize(initRelayUrls: Collection<RelayUrl>) {
        nostrClient.setListener(listener)
        Log.i(tag, "Add ${initRelayUrls.size} relays: $initRelayUrls")
        nostrClient.addRelays(initRelayUrls)
    }

    fun publishPost(
        title: String,
        content: String,
        topic: String,
        relayUrls: Collection<RelayUrl>
    ): Event {
        val timestamp = Timestamp.now()
        val keys = singleUseKeyManager.getPostingKeys(timestamp)
        val tags = listOf(
            createTitleTag(title),
            createHashtagTag(hashtag = topic)
        )
        val event = EventBuilder.textNote(content, tags)
            .customCreatedAt(timestamp)
            .toEvent(keys)
        nostrClient.publishToRelays(event = event, relayUrls = relayUrls)

        return event
    }

    fun publishVote(
        eventId: EventId,
        pubkey: PublicKey,
        isPositive: Boolean,
        kind: Int,
        relayUrls: Collection<RelayUrl>,
    ): Event {
        val tags = listOf(createKindTag(kind))
        val content = if (isPositive) "+" else "-"
        val event = EventBuilder.reaction(eventId, pubkey, content)
            .toEvent(Keys.generate()) // TODO: real keys
        nostrClient.publishToRelays(event = event, relayUrls = relayUrls)

        return event
    }

    fun subscribe(filters: List<Filter>, relayUrl: RelayUrl): SubId? {
        if (filters.isEmpty()) return null

        val subId = nostrClient.subscribe(filters = filters, relayUrl = relayUrl)
        if (subId == null) {
            Log.w(tag, "Failed to create subscription ID")
            return null
        }
        filterCache[subId] = filters
        unsubOnEOSECache.add(subId)

        return subId
    }

    fun unsubscribe(subIds: Collection<SubId>) {
        subIds.forEach {
            nostrClient.unsubscribe(it)
            filterCache.remove(it)
        }
    }

    fun close() {
        unsubOnEOSECache.clear()
        filterCache.clear()
        nostrClient.close()
    }
}