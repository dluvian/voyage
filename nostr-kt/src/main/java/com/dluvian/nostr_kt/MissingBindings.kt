package com.dluvian.nostr_kt

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import org.bitcoinj.crypto.MnemonicCode
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.RelayMessage
import rust.nostr.protocol.Tag
import rust.nostr.protocol.TagEnum
import rust.nostr.protocol.TagKind
import rust.nostr.protocol.Timestamp
import java.security.SecureRandom

// File for functions that should have been exposed in the kotlin bindings
// TODO: Remove functions once they're exposed in the bindings

// TODO: Remove after new rust-nostr release
val gson = GsonBuilder().disableHtmlEscaping().create()
fun getRelayMessageFromJson(json: String): Result<RelayMessage> {
    val msg = gson.fromJson(json, JsonElement::class.java).asJsonArray
    val type = msg[0].asString

    return runCatching {
        when (type) {
            "EVENT" -> {
                val str = msg[2].toString()
                Result.success(
                    RelayMessage.EventMsg(
                        subscriptionId = msg[1].asString,
                        event = Event.fromJson(str)
                    )
                )
            }

            "OK" -> Result.success(
                RelayMessage.Ok(
                    eventId = EventId.fromHex(msg[1].asString),
                    status = msg[2].asString.toBoolean(),
                    message = msg[3].asString
                )
            )

            "NOTICE" -> Result.success(RelayMessage.Notice(message = msg[1].asString))
            "EOSE" -> Result.success(RelayMessage.EndOfStoredEvents(subscriptionId = msg[1].asString))
            "CLOSED" -> Result.success(
                RelayMessage.Closed(
                    subscriptionId = msg[1].asString,
                    message = msg[2].asString
                )
            )

            "AUTH" -> Result.success(RelayMessage.Auth(challenge = msg[1].asString))
            else -> Result.failure(IllegalArgumentException("Unknown type $type"))
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

fun createLabelTag(label: String) = Tag.parse(listOf("l", label))

fun createReplyTag(parentEventId: EventId, relayHint: RelayUrl, parentIsRoot: Boolean) =
    Tag.parse(
        listOf(
            "e",
            parentEventId.toHex(),
            relayHint,
            if (parentIsRoot) "root" else "reply"
        )
    )

fun generateMnemonic(): String {
    val random = SecureRandom()
    val bytes = ByteArray(16)
    random.nextBytes(bytes)

    return MnemonicCode()
        .toMnemonic(bytes)
        .joinToString(separator = " ")
}

fun Timestamp.secs(): Long {
    return this.asSecs().toLong()
}

fun getCurrentSecs() = System.currentTimeMillis() / 1000

fun Event.isPostOrReply(): Boolean {
    return this.kind().toInt() == Kind.TEXT_NOTE
}

fun Event.isRootPost(): Boolean {
    return this.isPostOrReply() && this.getReplyToId() == null
}

fun Event.isReplyPost(): Boolean {
    return this.isPostOrReply() && this.getReplyToId() != null
}

// TODO: Write issue to prefer Result over throwing exceptions
fun isValidEventId(hex: String): Boolean {
    return runCatching { EventId.fromHex(hex) }.isSuccess
}

fun Event.getReplyToId(): String? {
    val replyTags = this.tags()
        .filter { it.kind() == TagKind.E }
        .map { it.asVec() }
        .filter { it.size >= 2 }

    return if (replyTags.size == 1) replyTags.first()[1]
    else replyTags.find { it.getOrNull(3) == "reply" }?.get(1)
}

fun Event.getHashtags(): List<String> {
    return this.tags()
        .filter { it.kind() == TagKind.T }
        .mapNotNull { it.asVec().getOrNull(1) }
        .distinct()
}

fun String.removeTrailingSlashes(): String {
    return this.removeSuffix("/")
}

fun Event.getNip65s(): List<Nip65Relay> {
    return this.tags().asSequence().filter { it.kind() == TagKind.R }
        .map { it.asVec() }
        .filter { it.size >= 2 && it[1].startsWith(WEBSOCKET_PREFIX) && it[1].trim().length >= 10 }
        .map {
            val restriction = it.getOrNull(2)
            Nip65Relay(
                url = it[1].trim().removeTrailingSlashes(),
                isRead = restriction == null || restriction == "read",
                isWrite = restriction == null || restriction == "write",
            )
        }
        .distinctBy { it.url }.toList()
}

fun Event.getTitle(): String? {
    return this.tags().firstOrNull { it.kind() == TagKind.Title }?.asVec()?.getOrNull(1)

}

fun Event.isContactList(): Boolean {
    return this.kind().toInt() == Kind.CONTACT_LIST
}

fun Event.isTopicList(): Boolean {
    return this.kind().toInt() == Kind.TOPIC_LIST
}

fun Event.isNip65(): Boolean {
    return this.kind().toInt() == Kind.NIP65
}

fun Event.isVote(): Boolean {
    return this.kind().toInt() == Kind.REACTION
}

fun createFriendFilter(pubkeys: List<PublicKey>, until: ULong, limit: ULong): Filter {
    return Filter().kind(kind = Kind.TEXT_NOTE.toULong()) // TODO: Support reposts
        .authors(authors = pubkeys)
        .until(timestamp = Timestamp.fromSecs(until))
        .limit(limit = limit)
}


object Kind {
    const val TEXT_NOTE = 1
    const val CONTACT_LIST = 3
    const val REACTION = 7
    const val NIP65 = 10002
    const val TOPIC_LIST = 10015
}