package com.dluvian.voyage.nostr

import android.util.Log
import com.dluvian.voyage.model.ReceiveEvent
import com.dluvian.voyage.model.RelayClosed
import com.dluvian.voyage.model.RelayNotice
import com.dluvian.voyage.model.RelayNotificationCmd
import kotlinx.coroutines.channels.Channel
import rust.nostr.sdk.Event
import rust.nostr.sdk.HandleNotification
import rust.nostr.sdk.RelayMessage
import rust.nostr.sdk.RelayMessageEnum

class NostrHandler(private val relayChannel: Channel<RelayNotificationCmd>) : HandleNotification {
    private val logTag = "NostrHandler"

    override suspend fun handle(
        relayUrl: String,
        subscriptionId: String,
        event: Event
    ) {
        Log.d(logTag, "Received event kind ${event.kind().asU16()} from $relayUrl")
        relayChannel.send(ReceiveEvent(event))
    }

    override suspend fun handleMsg(relayUrl: String, msg: RelayMessage) {
        when (val enum = msg.asEnum()) {
            is RelayMessageEnum.Closed -> relayChannel.send(RelayClosed(relayUrl, enum.message))
            is RelayMessageEnum.Notice -> relayChannel.send(RelayNotice(relayUrl, enum.message))
            is RelayMessageEnum.Auth,
            is RelayMessageEnum.Count,
            is RelayMessageEnum.EndOfStoredEvents,
            is RelayMessageEnum.EventMsg,
            is RelayMessageEnum.NegErr,
            is RelayMessageEnum.NegMsg,
            is RelayMessageEnum.Ok -> {
            }
        }
    }
}
