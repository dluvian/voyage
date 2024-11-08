package com.dluvian.voyage.data.nostr

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import rust.nostr.sdk.ClientMessage
import rust.nostr.sdk.Event
import rust.nostr.sdk.Filter
import rust.nostr.sdk.RelayMessage
import rust.nostr.sdk.RelayMessageEnum
import java.util.Collections
import java.util.UUID

private const val TAG = "Client"

class NostrClient {
    private val httpClient = OkHttpClient()
    private val sockets: MutableMap<RelayUrl, WebSocket> =
        Collections.synchronizedMap(mutableMapOf())
    private val subscriptions: MutableMap<SubId, WebSocket> =
        Collections.synchronizedMap(mutableMapOf())

    private var nostrListener: INostrListener? = null

    private val baseListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            nostrListener?.onOpen(
                relayUrl = getRelayUrl(webSocket).orEmpty(),
                msg = response.message
            )
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            val url = getRelayUrl(webSocket).orEmpty()
            removeSocket(socket = webSocket)
            nostrListener?.onClose(relayUrl = url, reason = reason)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            onClosing(webSocket = webSocket, code = code, reason = reason)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            val url = getRelayUrl(webSocket).orEmpty()
            removeSocket(socket = webSocket)
            nostrListener?.onFailure(relayUrl = url, msg = response?.message, throwable = t)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val relayUrl = getRelayUrl(webSocket).orEmpty()
            val relayMessage = runCatching { RelayMessage.fromJson(json = text) }
            relayMessage
                .onFailure {
                    nostrListener?.onError(
                        relayUrl = relayUrl,
                        msg = "Problem with $text",
                        throwable = it
                    )
                }
                .onSuccess { relayMsg ->
                    when (val enum = relayMsg.asEnum()) {
                        is RelayMessageEnum.EventMsg -> {
                            nostrListener?.onEvent(
                                subId = enum.subscriptionId,
                                event = enum.event,
                                relayUrl = relayUrl
                            )
                        }

                        is RelayMessageEnum.Ok -> nostrListener?.onOk(
                            relayUrl = relayUrl,
                            eventId = enum.eventId,
                            accepted = enum.status,
                            msg = enum.message
                        )

                        is RelayMessageEnum.Notice -> nostrListener?.onError(
                            relayUrl = relayUrl,
                            msg = "onNotice: ${enum.message}"
                        )

                        is RelayMessageEnum.EndOfStoredEvents -> nostrListener?.onEOSE(
                            relayUrl = relayUrl,
                            subId = enum.subscriptionId
                        )

                        is RelayMessageEnum.Closed -> nostrListener?.onClosed(
                            relayUrl = relayUrl,
                            subId = enum.subscriptionId,
                            reason = enum.message
                        )

                        is RelayMessageEnum.Auth -> nostrListener?.onAuth(
                            relayUrl = relayUrl,
                            challenge = enum.challenge,
                        )

                        else -> nostrListener?.onError(
                            relayUrl = relayUrl,
                            msg = "Unknown type ${enum}. Msg was $text"
                        )
                    }
                }
        }
    }

    fun setListener(listener: INostrListener) {
        nostrListener = listener
    }

    fun subscribe(filters: List<Filter>, relayUrl: RelayUrl): SubId? {
        if (filters.isEmpty()) return null

        addRelay(relayUrl)
        val socket = sockets[relayUrl]
        if (socket == null) {
            Log.w(TAG, "Failed to sub ${filters.size} filters. Relay $relayUrl is not registered")
            return null
        }
        val subId = UUID.randomUUID().toString()
        subscriptions[subId] = socket
        val request = ClientMessage.req(subscriptionId = subId, filters = filters).asJson()
        Log.d(TAG, "Subscribe $subId in $relayUrl: $request")
        socket.send(request)

        return subId
    }

    fun unsubscribe(subId: SubId) {
        subscriptions[subId]?.let { socket ->
            Log.d(TAG, "Unsubscribe from $subId")
            val closeRequest = ClientMessage.close(subscriptionId = subId).asJson()
            socket.send(closeRequest)
            subscriptions.remove(subId)
        }
    }

    fun unsubscribeAll() {
        synchronized(subscriptions) {
            subscriptions.entries.forEach { (subId, socket) ->
                Log.d(TAG, "(all) Unsubscribe from $subId")
                val closeRequest = ClientMessage.close(subscriptionId = subId).asJson()
                socket.send(closeRequest)
            }
            subscriptions.clear()
        }
    }

    fun publishToRelays(event: Event, relayUrls: Collection<RelayUrl>) {
        addRelays(relayUrls)
        val filteredRelays = filterSocketsByRelays(relays = relayUrls)
        val eventMessage = ClientMessage.event(event = event).asJson()
        Log.i(TAG, "Publish to ${filteredRelays.size} relays: $eventMessage")
        filteredRelays.forEach { it.value.send(eventMessage) }
    }

    fun addRelays(relayUrls: Collection<RelayUrl>) {
        relayUrls.forEach { addRelay(it) }
    }

    fun getAllConnectedUrls(): List<RelayUrl> {
        val allUrls = sockets.keys
        synchronized(allUrls) {
            return allUrls.toList()
        }
    }

    fun publishAuth(authEvent: Event, relayUrl: RelayUrl) {
        addRelay(relayUrl)
        val socket = sockets.entries.find { (relay, _) -> relay == relayUrl }?.value
        if (socket == null) {
            Log.e(TAG, "Failed to send AUTH. Socket for $relayUrl is not registered")
            return
        }

        val authMsg = ClientMessage.auth(event = authEvent).asJson()
        Log.i(TAG, "Publish AUTH to $relayUrl: $authMsg")
        socket.send(authMsg)
    }

    fun addRelay(relayUrl: RelayUrl) {
        if (sockets.containsKey(relayUrl)) return

        Log.i(TAG, "Add relay $relayUrl")
        runCatching {
            val request = Request.Builder().url(relayUrl).build()
            val socket = httpClient.newWebSocket(request = request, listener = baseListener)
            sockets[relayUrl] = socket
        }.onFailure {
            Log.e(TAG, "Failed to connect to $relayUrl", it)
        }
    }

    fun removeRelay(relayUrl: RelayUrl, reason: String = "Normal closure") {
        Log.i(TAG, "Remove relay $relayUrl")
        val removedSocket = sockets.remove(relayUrl)
        removedSocket?.close(code = 1000, reason = reason)
    }

    fun close() {
        Log.i(TAG, "Close connections")
        val sockets = sockets.keys.toSet()
        sockets.forEach { removeRelay(it) }
        httpClient.dispatcher.executorService.shutdown()
    }

    private fun getRelayUrl(webSocket: WebSocket): RelayUrl? {
        val snapshot: List<Pair<RelayUrl, WebSocket>>
        synchronized(sockets) {
            snapshot = sockets.entries.map { Pair(it.key, it.value) }
        }
        return snapshot.find { it.second == webSocket }?.first
    }

    private fun removeSocket(socket: WebSocket) {
        synchronized(subscriptions) {
            synchronized(sockets) {
                sockets.filter { it.value == socket }.forEach {
                    sockets.remove(it.key)
                }
            }
            subscriptions.filter { it.value == socket }.forEach {
                subscriptions.remove(it.key)
            }
        }
    }

    private fun filterSocketsByRelays(relays: Collection<RelayUrl>): List<Map.Entry<RelayUrl, WebSocket>> {
        val snapshot = sockets.entries.toList()
        return snapshot.filter { relays.contains(it.key) }
    }
}
