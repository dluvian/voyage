package com.dluvian.nostr_kt

import cash.z.ecc.android.bip39.Mnemonics
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventBuilder
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Kind
import rust.nostr.protocol.KindEnum
import rust.nostr.protocol.Metadata
import rust.nostr.protocol.Nip19Event
import rust.nostr.protocol.Nip19Profile
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

fun createMentionTag(pubkeys: Collection<String>): Tag {
    val tag = mutableListOf("p")
    tag.addAll(pubkeys)

    return Tag.parse(data = tag)
}

private val nostrMentionPattern = Regex("(nostr:|@)(npub1|nprofile1)[a-zA-Z0-9]+")
fun extractMentions(content: String) = nostrMentionPattern.findAll(content)
    .map { it.value.removePrefix("@").removePrefix("nostr:") }
    .distinct()
    .filter {
        runCatching { PublicKey.fromBech32(it) }.isSuccess ||
                runCatching { Nip19Profile.fromBech32(it) }.isSuccess
    }
    .toList()

fun createReplyTag(parentEventId: EventId, relayHint: RelayUrl): Tag {
    return Tag.parse(listOf("e", parentEventId.toHex(), relayHint, "reply"))
}

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
    return hex.length == 64 && hex.all { it.isDigit() || it in ('a'..'f') || it in ('A'..'F') }
}

fun Event.getReplyToId(): String? {
    if (!this.isPostOrReply()) return null

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
    return this.dropLastWhile { it == '/' }
}

fun String.removeNostrUri(): String {
    return this.removePrefix(NOSTR_URI)
}

fun String.removeMentionChar(): String {
    return this.removePrefix(MENTION_CHAR)
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

fun createNprofile(hex: String, relays: List<String> = emptyList()): Nip19Profile {
    return createNprofile(pubkey = PublicKey.fromHex(hex), relays = relays)
}

fun createNprofile(pubkey: PublicKey, relays: List<String> = emptyList()): Nip19Profile {
    return Nip19Profile(publicKey = pubkey, relays = relays)
}

fun createNevent(
    hex: String,
    author: String? = null,
    relays: List<String> = emptyList()
): Nip19Event {
    return createNevent(
        eventId = EventId.fromHex(hex),
        author = author?.let { PublicKey.fromHex(it) },
        relays = relays
    )
}

fun createNevent(
    eventId: EventId,
    author: PublicKey? = null,
    relays: List<String> = emptyList()
): Nip19Event {
    return Nip19Event(eventId = eventId, author = author, relays = relays)
}

fun createReaction(
    eventId: EventId,
    mention: PublicKey,
    content: String,
    kind: Int,
    myPubkey: PublicKey
): UnsignedEvent {
    val tags = listOf(
        Tag.event(eventId),
        Tag.publicKey(mention),
        Tag.parse(listOf("k", "$kind"))
    )

    return EventBuilder(
        kind = Kind.fromEnum(KindEnum.Reaction),
        content = content,
        tags = tags
    ).toUnsignedEvent(publicKey = myPubkey)
}
