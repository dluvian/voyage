package com.dluvian.voyage.data.nostr

import rust.nostr.sdk.Event
import rust.nostr.sdk.EventId

interface INostrListener {
    fun onOpen(relayUrl: RelayUrl, msg: String)
    fun onEvent(subId: SubId, event: Event, relayUrl: RelayUrl?)
    fun onError(relayUrl: RelayUrl, msg: String, throwable: Throwable? = null)
    fun onEOSE(relayUrl: RelayUrl, subId: SubId)
    fun onClosed(relayUrl: RelayUrl, subId: SubId, reason: String)
    fun onClose(relayUrl: RelayUrl, reason: String)
    fun onFailure(relayUrl: RelayUrl, msg: String?, throwable: Throwable? = null)
    fun onOk(relayUrl: RelayUrl, eventId: EventId, accepted: Boolean, msg: String)
    fun onAuth(relayUrl: RelayUrl, challenge: String)
}
