package com.dluvian.nostr_kt

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import rust.nostr.protocol.Event
import rust.nostr.protocol.Filter
import rust.nostr.protocol.RelayMessage
import java.util.Collections
import java.util.UUID

private const val TAG = "Client"

class NostrClient(private val httpClient: OkHttpClient) {
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

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            val url = getRelayUrl(webSocket).orEmpty()
            removeSocket(socket = webSocket)
            nostrListener?.onFailure(relayUrl = url, msg = response?.message, throwable = t)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val relayUrl = getRelayUrl(webSocket).orEmpty()
            val relayMessage = getRelayMessageFromJson(text)
            relayMessage
                .onFailure {
                    nostrListener?.onError(
                        relayUrl = relayUrl,
                        msg = "Problem with $text",
                        throwable = it
                    )
                }
                .onSuccess {
                    when (it) {
                        is RelayMessage.EventMsg -> {
                            nostrListener?.onEvent(
                                subId = it.subscriptionId,
                                event = it.event,
                                relayUrl = relayUrl
                            )
                        }

                        is RelayMessage.Ok -> nostrListener?.onOk(
                            relayUrl = relayUrl,
                            eventId = it.eventId,
                            accepted = it.status,
                            msg = it.message
                        )

                        is RelayMessage.Notice -> nostrListener?.onError(
                            relayUrl = relayUrl,
                            msg = "onNotice: $it.message"
                        )

                        is RelayMessage.EndOfStoredEvents -> nostrListener?.onEOSE(
                            relayUrl = relayUrl,
                            subId = it.subscriptionId
                        )

                        is RelayMessage.Closed -> nostrListener?.onClosed(
                            relayUrl = relayUrl,
                            subId = it.subscriptionId,
                            reason = it.message
                        )

                        is RelayMessage.Auth -> nostrListener?.onAuth(
                            relayUrl = relayUrl,
                            challengeString = it.challenge,
                        )

                        else -> nostrListener?.onError(
                            relayUrl = relayUrl,
                            msg = "Unknown type ${it}. Msg was $text"
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
        val request = createSubscriptionRequest(subId = subId, filters = filters)
        Log.d(TAG, "Subscribe in $relayUrl: $request")
        socket.send(request)

        return subId
    }

    fun unsubscribe(subId: SubId) {
        subscriptions[subId]?.let { socket ->
            Log.d(TAG, "Unsubscribe from $subId")
            socket.send(createCloseRequest(subId))
            subscriptions.remove(subId)
        }
    }

    fun publishToRelays(event: Event, relayUrls: Collection<RelayUrl>) {
        addRelays(relayUrls)
        val filteredRelays = filterSocketsByRelays(relays = relayUrls)
        val request = createEventRequest(event)
        Log.i(TAG, "Publish to ${filteredRelays.size} relays: $request")
        filteredRelays.forEach { it.value.send(request) }
    }

    fun addRelays(relayUrls: Collection<RelayUrl>) {
        relayUrls.forEach { addRelay(it) }
    }

    private fun addRelay(relayUrl: RelayUrl) {
        if (sockets.containsKey(relayUrl)) return
        if (!relayUrl.startsWith(WEBSOCKET_PREFIX)) return

        Log.i(TAG, "Add relay $relayUrl")
        runCatching {
            val request = Request.Builder().url(relayUrl).build()
            val socket = httpClient.newWebSocket(request = request, listener = baseListener)
            sockets[relayUrl] = socket
        }.onFailure {
            Log.e(TAG, "Failed to connect to $relayUrl", it)
        }
    }

    fun close() {
        Log.i(TAG, "Close connections")
        val sockets = sockets.keys.toSet()
        sockets.forEach { removeRelay(it) }
        httpClient.dispatcher.executorService.shutdown()
    }

    private fun removeRelay(relayUrl: RelayUrl) {
        Log.i(TAG, "Remove relay $relayUrl")
        val removedSocket = sockets.remove(relayUrl)
        removedSocket?.close(1000, "Normal closure")
    }

    private fun getRelayUrl(webSocket: WebSocket): RelayUrl? {
        val snapshot = sockets.entries.toSet()
        return snapshot.find { it.value == webSocket }?.key
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

    fun getAllConnectedUrls(): List<RelayUrl> {
        return sockets.keys.toList()
    }

    private fun filterSocketsByRelays(relays: Collection<RelayUrl>): List<Map.Entry<RelayUrl, WebSocket>> {
        val snapshot = sockets.entries.toList()
        return snapshot.filter { relays.contains(it.key) }
    }
}
