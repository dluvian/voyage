package com.dluvian.nostr_kt

import cash.z.ecc.android.bip39.Mnemonics
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventBuilder
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Kind
import rust.nostr.protocol.KindEnum
import rust.nostr.protocol.Metadata
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.Tag
import rust.nostr.protocol.TagEnum
import rust.nostr.protocol.TagKind
import rust.nostr.protocol.Timestamp
import rust.nostr.protocol.UnsignedEvent
import java.security.SecureRandom


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
    val entropy = ByteArray(16)
    random.nextBytes(entropy)

    return Mnemonics.MnemonicCode(entropy).words.joinToString(separator = " ") { String(it) }
}

fun Timestamp.secs(): Long {
    return this.asSecs().toLong()
}

fun getCurrentSecs() = System.currentTimeMillis() / 1000

fun Event.isPostOrReply(): Boolean {
    return this.kind().matchEnum(KindEnum.TextNote)
}

fun Event.isRootPost(): Boolean {
    return this.isPostOrReply() && this.getReplyToId() == null
}

fun Event.isReplyPost(): Boolean {
    return this.isPostOrReply() && this.getReplyToId() != null
}

fun isValidEventId(hex: String): Boolean {
    return runCatching { EventId.fromHex(hex) }.isSuccess
}

fun Event.getReplyToId(): String? {
    val nip10Tags = this.tags()
        .map { it.asVec() }
        .filter { it.size >= 2 && it[0] == "e" }

    if (nip10Tags.isEmpty()) return null

    return nip10Tags.find { it.getOrNull(3) == "reply" }?.get(1)
        ?: nip10Tags.find { it.getOrNull(3) == "root" }?.get(1)
        ?: nip10Tags.find { it.getOrNull(1) != null }?.get(1)
}

fun Event.getHashtags(): List<String> {
    return this.tags()
        .map { it.asVec() }
        .filter { it.firstOrNull() == "t" }
        .mapNotNull { it.getOrNull(1) }
        .distinct()
}

fun String.removeTrailingSlashes(): String {
    return this.removeSuffix("/")
}

fun Event.getNip65s(): List<Nip65Relay> {
    return this.tags().asSequence()
        .map { it.asVec() }
        .filter { it.firstOrNull() == "r" }
        .filter { it.size >= 2 }
        .filter { it[1].startsWith(WEBSOCKET_PREFIX) }
        .filter { it[1].trim().length >= 10 }
        .map {
            val restriction = it.getOrNull(2)
            Nip65Relay(
                url = it[1].trim().removeTrailingSlashes(),
                isRead = restriction.isNullOrEmpty() || restriction == "read",
                isWrite = restriction.isNullOrEmpty() || restriction == "write",
            )
        }
        .distinctBy { it.url }.toList()
}

fun Event.getTitle(): String? {
    return this.tags()
        .firstOrNull { it.kind() == TagKind.Title }
        ?.asVec()
        ?.getOrNull(1)
}

fun Event.getMetadata(): Metadata? {
    if (!this.isProfile()) return null

    return runCatching { Metadata.fromJson(json = this.content()) }.getOrNull()
}

fun Event.isContactList(): Boolean {
    return this.kind().matchEnum(KindEnum.ContactList)
}

fun Event.isTopicList(): Boolean {
    return this.kind().matchEnum(KindEnum.Interests)
}

fun Event.isNip65(): Boolean {
    return this.kind().matchEnum(KindEnum.RelayList)
}

fun Event.isVote(): Boolean {
    return this.kind().matchEnum(KindEnum.Reaction)
}

fun Event.isProfile(): Boolean {
    return this.kind().matchEnum(KindEnum.Metadata)
}

fun createReaction(
    eventId: EventId,
    pubkey: PublicKey,
    content: String,
    kind: Int,
): UnsignedEvent {
    val tags = listOf(
        Tag.event(eventId),
        Tag.publicKey(pubkey),
        Tag.parse(listOf("k", "$kind"))
    )

    return EventBuilder(
        kind = Kind.fromEnum(KindEnum.Reaction),
        content = content,
        tags = tags
    ).toUnsignedEvent(publicKey = pubkey)
}
