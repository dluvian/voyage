package com.dluvian.nostr_kt

import rust.nostr.protocol.Event
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter
import rust.nostr.protocol.RelayMessage

// File for functions that should have been exposed in the kotlin bindings
// TODO: Remove functions once they're exposed in the bindings

fun getRelayMessagefromJson(json: String): Result<RelayMessage> {
    val items = json.trim()
        .removeSurrounding(prefix = "[", suffix = "]")
        .split(',', limit = 3)
        .map { it.trim().removeSurrounding("\"") }
    if (items.size != 4) return Result.failure(IllegalArgumentException("Json array is not of size 4, it's ${items.size}"))

    return kotlin.runCatching {
        when (items.first()) {
            "EVENT" -> Result.success(
                RelayMessage.EventMsg(
                    subscriptionId = items[1],
                    event = Event.fromJson(items[2])
                )
            )

            "OK" -> Result.success(
                RelayMessage.Ok(
                    eventId = EventId.fromHex(items[1]),
                    status = items[2].toBoolean(),
                    message = items[3]
                )
            )

            "NOTICE" -> Result.success(RelayMessage.Notice(message = items[1]))
            "EOSE" -> Result.success(RelayMessage.EndOfStoredEvents(subscriptionId = items[1]))
            "CLOSED" -> Result.success(
                RelayMessage.Closed(
                    subscriptionId = items[1],
                    message = items[2]
                )
            )

            "AUTH" -> Result.success(RelayMessage.Auth(challenge = items[1]))
            else -> Result.failure(IllegalArgumentException("Unknown type ${items.first()}"))
        }
    }.getOrElse { exception -> Result.failure(exception) }


}

fun createSubscriptionRequest(
    subId: SubId,
    filters: List<Filter>
): String {
    // TODO: ClientMessage.Req(subId, filters).asJson()
    return """["REQ","$subId",${filters.joinToString(",") { it.asJson() }}]"""
}

fun createCloseRequest(subId: SubId): String {
    return """["CLOSE","$subId"]"""
}

fun createEventRequest(event: Event): String {
    return """["EVENT",${event.asJson()}]"""
}

fun createAuthRequest(authEvent: Event): String {
    return """["AUTH",${authEvent.asJson()}]"""
}

fun getCurrentTimeInSeconds(): Long {
    return System.currentTimeMillis() / 1000
}

fun Filter.matches(event: Event): Boolean {
    // TODO: This is not complete
    return true
}

object Kind {
    const val METADATA = 0
    const val TEXT_NOTE = 1
    const val CONTACT_LIST = 3
    const val DELETE = 5
    const val REPOST = 6
    const val REACTION = 7
    const val NIP65 = 10002
    const val AUTH = 22242
}