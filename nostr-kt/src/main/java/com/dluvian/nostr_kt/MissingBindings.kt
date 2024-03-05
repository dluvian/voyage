package com.dluvian.nostr_kt

import rust.nostr.protocol.Event
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter
import rust.nostr.protocol.RelayMessage
import rust.nostr.protocol.Tag
import rust.nostr.protocol.TagEnum
import rust.nostr.protocol.TagKind

// File for functions that should have been exposed in the kotlin bindings
// TODO: Remove functions once they're exposed in the bindings

fun getRelayMessagefromJson(json: String): Result<RelayMessage> {
    val items = json.trim()
        .removeSurrounding(prefix = "[", suffix = "]")
        .split(',', limit = 3)
        .map { it.trim().removeSurrounding("\"") }
    if (items.size != 4) return Result.failure(IllegalArgumentException("Json array is not of size 4, it's ${items.size}"))

    return runCatching {
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

fun Filter.matches(event: Event): Boolean {
    // TODO: This is not complete
    return true
}

fun createTitleTag(title: String) = Tag.fromEnum(TagEnum.Title(title))

fun createHashtagTag(hashtag: String) = Tag.fromEnum(TagEnum.Hashtag(hashtag))

fun createLabelTag(label: String) = Tag.parse(listOf("l", "$label"))

fun createKindTag(kind: Int) = Tag.parse(listOf("k", "$kind"))

fun createReplyTag(parentEventId: EventId, relayHint: RelayUrl, parentIsRoot: Boolean) =
    Tag.parse(
        listOf(
            "e",
            parentEventId.toHex(),
            relayHint,
            if (parentIsRoot) "root" else "reply"
        )
    )

fun generateMnemonic() =
    "leader monkey parrot ring guide accident before fence cannon height naive bean"

fun Event.isPostOrReply(): Boolean {
    return this.kind().toInt() == Kind.TEXT_NOTE
}

fun Event.isReply(): Boolean {
    return this.isPostOrReply() &&
            this.tags()
                .filter { it.kind() == TagKind.E }
                .map { it.asVec() }
                .any { it.getOrNull(3).let { marker -> marker == "root" || marker == "reply" } }
}

fun Tag.isReplyTag(): Boolean {
    TODO()
}

fun Event.isContactList(): Boolean {
    return this.kind().toInt() == Kind.CONTACT_LIST
}

fun Event.isTopicList(): Boolean {
    return this.kind().toInt() == Kind.TOPIC_LIST
}

fun Event.isVote(): Boolean {
    return this.kind().toInt() == Kind.REACTION
}

object Kind {
    const val TEXT_NOTE = 1
    const val CONTACT_LIST = 3
    const val REACTION = 7
    const val TOPIC_LIST = 10015
}