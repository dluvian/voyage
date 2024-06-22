package com.dluvian.voyage.data.nostr

import android.util.Log
import androidx.compose.runtime.MutableState
import com.dluvian.nostr_kt.INostrListener
import com.dluvian.nostr_kt.Nip65Relay
import com.dluvian.nostr_kt.NostrClient
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.SubId
import com.dluvian.voyage.core.AUTH_TIMEOUT
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.model.BadConnection
import com.dluvian.voyage.core.model.Connected
import com.dluvian.voyage.core.model.ConnectionStatus
import com.dluvian.voyage.core.model.Spam
import com.dluvian.voyage.core.model.Waiting
import com.dluvian.voyage.data.event.EventCounter
import com.dluvian.voyage.data.event.EventMaker
import com.dluvian.voyage.data.event.EventQueue
import com.dluvian.voyage.data.preferences.RelayPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter
import rust.nostr.protocol.Kind
import rust.nostr.protocol.Metadata
import rust.nostr.protocol.PublicKey

private const val TAG = "NostrService"

class NostrService(
    private val nostrClient: NostrClient,
    private val eventQueue: EventQueue,
    private val eventMaker: EventMaker,
    private val filterCache: MutableMap<SubId, List<Filter>>,
    private val relayPreferences: RelayPreferences,
    private val connectionStatuses: MutableState<Map<RelayUrl, ConnectionStatus>>,
    private val eventCounter: EventCounter
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val listener = object : INostrListener {
        override fun onOpen(relayUrl: RelayUrl, msg: String) {
            Log.i(TAG, "OnOpen($relayUrl): $msg")
            addConnectionStatus(relayUrl = relayUrl, status = Connected)
        }

        override fun onEvent(subId: SubId, event: Event, relayUrl: RelayUrl?) {
            if (!relayUrl.isNullOrEmpty() && eventCounter.isExceedingLimit(subId = subId)) {
                nostrClient.removeRelay(
                    relayUrl = relayUrl,
                    reason = "You're sending me more than I requested"
                )
                addConnectionStatus(relayUrl = relayUrl, status = Spam)
                Log.w(TAG, "$relayUrl sends more events than requested in $subId")
                return
            }

            eventQueue.submit(event = event, subId = subId, relayUrl = relayUrl)
        }

        override fun onError(relayUrl: RelayUrl, msg: String, throwable: Throwable?) {
            Log.w(TAG, "OnError($relayUrl): $msg", throwable)
        }

        override fun onEOSE(relayUrl: RelayUrl, subId: SubId) {
            Log.d(TAG, "OnEOSE($relayUrl): $subId")
            nostrClient.unsubscribe(subId)
        }

        override fun onClosed(relayUrl: RelayUrl, subId: SubId, reason: String) {
            Log.d(TAG, "OnClosed($relayUrl): $subId, reason: $reason")
        }

        override fun onClose(relayUrl: RelayUrl, reason: String) {
            Log.i(TAG, "OnClose($relayUrl): $reason")
            addConnectionStatus(relayUrl = relayUrl, status = BadConnection)
        }

        override fun onFailure(relayUrl: RelayUrl, msg: String?, throwable: Throwable?) {
            Log.w(TAG, "OnFailure($relayUrl): $msg", throwable)
            addConnectionStatus(relayUrl = relayUrl, status = BadConnection)
        }

        override fun onOk(relayUrl: RelayUrl, eventId: EventId, accepted: Boolean, msg: String) {
            Log.d(
                TAG,
                "OnOk($relayUrl): ${eventId.toHex()}, accepted=$accepted, ${msg.ifBlank { "No message" }}"
            )
        }

        override fun onAuth(relayUrl: RelayUrl, challenge: String) {
            Log.d(TAG, "OnAuth($relayUrl): challenge=$challenge")

            if (!relayPreferences.getSendAuth()) {
                Log.i(TAG, "Reject AUTH from $relayUrl")
                return
            }

            scope.launchIO {
                sendAuth(relayUrl = relayUrl, challenge = challenge)
            }
        }
    }

    fun initialize(initRelayUrls: Collection<RelayUrl>) {
        nostrClient.setListener(listener)
        Log.i(TAG, "Add ${initRelayUrls.size} relays: $initRelayUrls")
        nostrClient.addRelays(initRelayUrls)
        initRelayUrls.forEach {
            addConnectionStatus(relayUrl = it, status = Waiting)
        }
    }

    fun publishJson(eventJson: String, relayUrls: Collection<RelayUrl>): Result<Event> {
        return runCatching { Event.fromJson(json = eventJson) }
            .onSuccess { event ->
                nostrClient.publishToRelays(event = event, relayUrls = relayUrls)
            }
            .onFailure {
                Log.w(TAG, "Failed to deserialize $eventJson")
            }
    }

    suspend fun publishPost(
        subject: String,
        content: String,
        topics: List<Topic>,
        mentions: List<PubkeyHex>,
        relayUrls: Collection<RelayUrl>,
    ): Result<Event> {
        return eventMaker.buildPost(
            subject = subject,
            content = content,
            topics = topics,
            mentions = mentions,
        )
            .onSuccess { nostrClient.publishToRelays(event = it, relayUrls = relayUrls) }
    }

    suspend fun publishReply(
        content: String,
        parentId: EventIdHex,
        mentions: List<PubkeyHex>,
        relayHint: RelayUrl,
        pubkeyHint: PubkeyHex,
        relayUrls: Collection<RelayUrl>,
    ): Result<Event> {
        return eventMaker.buildReply(
            parentId = EventId.fromHex(parentId),
            mentions = mentions.map { PublicKey.fromHex(it) },
            relayHint = relayHint,
            pubkeyHint = pubkeyHint,
            content = content,
        )
            .onSuccess { nostrClient.publishToRelays(event = it, relayUrls = relayUrls) }
    }

    suspend fun publishCrossPost(
        crossPostedEvent: Event,
        topics: List<Topic>,
        relayHint: RelayUrl,
        relayUrls: Collection<RelayUrl>,
    ): Result<Event> {
        return eventMaker.buildCrossPost(
            crossPostedEvent = crossPostedEvent,
            topics = topics,
            relayHint = relayHint,
        )
            .onSuccess { nostrClient.publishToRelays(event = it, relayUrls = relayUrls) }
    }

    suspend fun publishUpvote(
        eventId: EventId,
        mention: PublicKey,
        relayUrls: Collection<RelayUrl>,
    ): Result<Event> {
        return eventMaker.buildVote(
            eventId = eventId,
            mention = mention,
        )
            .onSuccess { nostrClient.publishToRelays(event = it, relayUrls = relayUrls) }
    }

    suspend fun publishDelete(
        eventId: EventId,
        relayUrls: Collection<RelayUrl>,
    ): Result<Event> {
        return eventMaker.buildDelete(eventId = eventId)
            .onSuccess { nostrClient.publishToRelays(event = it, relayUrls = relayUrls) }
    }

    suspend fun publishListDeletion(
        kind: Kind,
        identifier: String,
        relayUrls: Collection<RelayUrl>,
    ): Result<Event> {
        return eventMaker.buildListDelete(kind = kind, identifier = identifier)
            .onSuccess { nostrClient.publishToRelays(event = it, relayUrls = relayUrls) }
    }

    suspend fun publishTopicList(
        topics: List<Topic>,
        relayUrls: Collection<RelayUrl>,
    ): Result<Event> {
        return eventMaker.buildTopicList(topics = topics)
            .onSuccess { nostrClient.publishToRelays(event = it, relayUrls = relayUrls) }
    }

    suspend fun publishBookmarkList(
        postIds: List<EventIdHex>,
        relayUrls: Collection<RelayUrl>,
    ): Result<Event> {
        return eventMaker.buildBookmarkList(postIds = postIds)
            .onSuccess { nostrClient.publishToRelays(event = it, relayUrls = relayUrls) }
    }

    suspend fun publishContactList(
        pubkeys: List<PubkeyHex>,
        relayUrls: Collection<RelayUrl>,
    ): Result<Event> {
        return eventMaker.buildContactList(pubkeys = pubkeys)
            .onSuccess { nostrClient.publishToRelays(event = it, relayUrls = relayUrls) }
    }

    suspend fun publishNip65(
        relays: List<Nip65Relay>,
        relayUrls: Collection<RelayUrl>,
    ): Result<Event> {
        return eventMaker.buildNip65(relays = relays)
            .onSuccess { nostrClient.publishToRelays(event = it, relayUrls = relayUrls) }
    }

    suspend fun publishProfileSet(
        identifier: String,
        title: String,
        pubkeys: List<PublicKey>,
        relayUrls: Collection<RelayUrl>,
    ): Result<Event> {
        return eventMaker.buildProfileSet(identifier = identifier, title = title, pubkeys = pubkeys)
            .onSuccess { nostrClient.publishToRelays(event = it, relayUrls = relayUrls) }
    }

    suspend fun publishTopicSet(
        identifier: String,
        title: String,
        topics: List<Topic>,
        relayUrls: Collection<RelayUrl>,
    ): Result<Event> {
        return eventMaker.buildTopicSet(identifier = identifier, title = title, topics = topics)
            .onSuccess { nostrClient.publishToRelays(event = it, relayUrls = relayUrls) }
    }

    suspend fun publishProfile(
        metadata: Metadata,
        relayUrls: Collection<RelayUrl>,
    ): Result<Event> {
        return eventMaker.buildProfile(metadata = metadata)
            .onSuccess { nostrClient.publishToRelays(event = it, relayUrls = relayUrls) }
    }

    fun addRelay(relayUrl: String) {
        nostrClient.addRelay(relayUrl = relayUrl)
    }

    fun close() {
        filterCache.clear()
        eventCounter.clear()
        nostrClient.close()
    }

    private val lastAuths = mutableMapOf<RelayUrl, Long>()
    private suspend fun sendAuth(relayUrl: RelayUrl, challenge: String) {
        val current = System.currentTimeMillis()
        synchronized(lastAuths) {
            val last = lastAuths.putIfAbsent(relayUrl, current)
            if (last != null) {
                lastAuths[relayUrl] = current
                if (current - last < AUTH_TIMEOUT) {
                    Log.i(TAG, "$relayUrl is spamming AUTH")
                    return
                }
            }
        }
        eventMaker.buildAuth(relayUrl = relayUrl, challenge = challenge)
            .onSuccess { event ->
                nostrClient.publishAuth(authEvent = event, relayUrl = relayUrl)
            }
            .onFailure { Log.w(TAG, "Failed to sign AUTH event for $relayUrl") }
    }

    private fun addConnectionStatus(relayUrl: RelayUrl, status: ConnectionStatus) {
        synchronized(connectionStatuses) {
            connectionStatuses.value = connectionStatuses.value.let {
                val mutable = it.toMutableMap()
                mutable[relayUrl] = status
                mutable
            }
        }
    }
}
