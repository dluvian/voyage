package com.dluvian.voyage.data.nostr

import androidx.core.text.isDigitsOnly
import cash.z.ecc.android.bip39.Mnemonics
import com.dluvian.voyage.core.Label
import com.dluvian.voyage.core.OptionId
import rust.nostr.protocol.Coordinate
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Kind
import rust.nostr.protocol.KindEnum
import rust.nostr.protocol.Metadata
import rust.nostr.protocol.Nip19Event
import rust.nostr.protocol.Nip19Profile
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.Tag
import rust.nostr.protocol.TagKind
import rust.nostr.protocol.TagStandard
import rust.nostr.protocol.Timestamp
import java.security.SecureRandom

fun createSubjectTag(subject: String): Tag {
    return Tag.fromStandardized(TagStandard.Subject(subject = subject))
}

fun createTitleTag(title: String): Tag {
    return Tag.fromStandardized(TagStandard.Title(title = title))
}

fun createKindTag(kind: Kind): Tag {
    // fromStandardized sucks hard
    return Tag.parse(listOf("k", kind.asU16().toString()))
}

fun createDescriptionTag(description: String): Tag {
    return Tag.fromStandardized(TagStandard.Description(desc = description))
}

fun createMentionTags(pubkeys: Collection<String>): List<Tag> {
    return pubkeys.map { Tag.parse(listOf("p", it)) }
}

fun createQuoteTags(eventIdHexOrCoordinates: List<String>): List<Tag> {
    return eventIdHexOrCoordinates.map { Tag.parse(listOf("q", it)) }
}

private val nostrMentionPattern = Regex("(nostr:|@)(npub1|nprofile1)[a-zA-Z0-9]+")
fun extractMentions(content: String) = nostrMentionPattern.findAll(content)
    .map { it.value.removePrefix("@").removePrefix(NOSTR_URI) }
    .distinct()
    .filter {
        runCatching { PublicKey.fromBech32(it) }.isSuccess ||
                runCatching { Nip19Profile.fromBech32(it) }.isSuccess
    }
    .toList()

private val nostrQuotePattern = Regex("(nostr:|@)(nevent1|naddr1|note1)[a-zA-Z0-9]+")
fun extractQuotes(content: String) = nostrQuotePattern.findAll(content)
    .map { it.value.removePrefix("@").removePrefix(NOSTR_URI) }
    .distinct()
    .filter {
        runCatching { Nip19Event.fromBech32(it) }.isSuccess ||
                runCatching { EventId.fromBech32(it) }.isSuccess ||
                runCatching { Coordinate.fromBech32(it) }.isSuccess
    }
    .toList()

fun createLegacyReplyTag(
    parentEventId: EventId,
    relayHint: RelayUrl,
    pubkeyHint: String,
): Tag {
    return Tag.parse(listOf("e", parentEventId.toHex(), relayHint, "reply", pubkeyHint))
}

fun createCommentETag(
    parentEventId: EventId,
    relayHint: RelayUrl,
    pubkeyHint: String,
): Tag {
    return Tag.parse(listOf("e", parentEventId.toHex(), relayHint, pubkeyHint))
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

fun Event.isTextNote(): Boolean {
    return this.kind().asEnum() == KindEnum.TextNote
}

fun Event.getClientTag(): String? {
    return this.tags()
        .map { it.asVec() }
        .find { it.size >= 2 && it.first() == "client" }
        ?.getOrNull(1)
}

fun Event.getKindTag(): UShort? {
    return this.tags()
        .map { it.asVec() }
        .find { it.size >= 2 && it.first() == "k" }
        ?.getOrNull(1)
        ?.toUShortOrNull()
}

fun Event.getParentId(): String? {
    val parentId = this.tags()
        .map { it.asVec() }
        .find { it.size >= 2 && it.first() == "e" }
        ?.getOrNull(1) ?: return null

    return if (isValidEventId(hex = parentId)) parentId else null
}

fun Event.getMuteWords(): List<String> {
    return this.tags()
        .filter { it.kind() == TagKind.Word }
        .mapNotNull { it.asVec().getOrNull(1) }
}

fun Event.getLegacyReplyToId(): String? {
    if (!this.isTextNote()) return null

    val nip10Tags = this.tags()
        .map { it.asVec() }
        .filter {
            it.size >= 2 &&
                    it[0] == "e" &&
                    (it.getOrNull(3).isNullOrEmpty() ||
                            it.getOrNull(3) == "reply" ||
                            it.getOrNull(3) == "root") &&
                    isValidEventId(it[1])
        }

    if (nip10Tags.isEmpty()) return null

    return nip10Tags.find { it.getOrNull(3) == "reply" }?.get(1)
        ?: nip10Tags.find { it.getOrNull(3) == "root" }?.get(1)
        ?: nip10Tags.last()[1] // Deprecated but still used by Damus. First is root, not reply
}

private fun isValidEventId(hex: String): Boolean {
    return hex.length == 64 && hex.all { it.isDigit() || it in ('a'..'f') || it in ('A'..'F') }
}

fun String.removeTrailingSlashes(): String {
    return this.dropLastWhile { it == '/' || it == ' ' }
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
        .filter { it.firstOrNull() == "r" && it.size >= 2 }
        .filter { it[1].startsWith(WEBSOCKET_URI) && it[1].trim().length >= 9 }
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

fun Event.getPollRelays(): List<RelayUrl> {
    return this.tags().asSequence()
        .map { it.asVec() }
        .filter { it.firstOrNull() == "relay" && it.size >= 2 }
        .map { it[1].trim() }
        .filter { it.startsWith(WEBSOCKET_URI) && it.length >= 9 }
        .distinct()
        .toList()
}

fun Event.getSubject() = this.getValue(kind = TagKind.Subject)
fun Event.getTitle() = this.getValue(kind = TagKind.Title)
fun Event.getDescription() = this.getValue(kind = TagKind.Description)

private fun Event.getValue(kind: TagKind): String? {
    return this.tags()
        .firstOrNull { it.kind() == kind }
        ?.asVec()
        ?.getOrNull(1)
}

fun Event.getPollOptions(): List<Pair<OptionId, Label>> {
    return this.tags()
        .map { it.asVec() }
        .filter { it.firstOrNull() == "option" && !it.getOrNull(1).isNullOrBlank() }
        .distinctBy { it[1].trim() }
        .map { Pair(it[1].trim(), it.getOrNull(2).orEmpty().trim()) }
}

fun Event.getPollResponse(): OptionId? {
    return this.tags()
        .map { it.asVec() }
        .find { it.firstOrNull() == "response" && !it.getOrNull(1).isNullOrBlank() }
        ?.getOrNull(1)
        ?.trim()
}

fun Event.getEndsAt(): Long? {
    return this.tags()
        .map { it.asVec() }
        .find {
            it.firstOrNull() == "endsAt"
                    && !it.getOrNull(1).isNullOrBlank()
                    && it[1].isDigitsOnly()
        }
        ?.let { it[1].toLongOrNull() }
}

fun Event.getMetadata(): Metadata? {
    if (!this.isProfile()) return null

    return runCatching { Metadata.fromJson(json = this.content()) }.getOrNull()
}

fun Event.isProfile(): Boolean {
    return this.kind().asEnum() == KindEnum.Metadata
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
    relays: List<String> = emptyList(),
    kind: Kind? = null
): Nip19Event {
    return createNevent(
        eventId = EventId.fromHex(hex),
        author = author?.let { PublicKey.fromHex(it) },
        relays = relays,
        kind = kind
    )
}

fun createNeventUri(
    hex: String,
    author: String? = null,
    relays: List<String> = emptyList(),
    kind: Kind? = null
): String {
    return "$NOSTR_URI${createNeventStr(hex = hex, author = author, relays = relays, kind = kind)}"
}

fun createNeventStr(
    hex: String,
    author: String? = null,
    relays: List<String> = emptyList(),
    kind: Kind? = null
): String {
    return createNevent(
        eventId = EventId.fromHex(hex),
        author = author?.let { PublicKey.fromHex(it) },
        relays = relays,
        kind = kind
    ).toBech32()
}

fun createNevent(
    eventId: EventId,
    author: PublicKey? = null,
    relays: List<String> = emptyList(),
    kind: Kind? = null
): Nip19Event {
    return Nip19Event(eventId = eventId, author = author, relays = relays, kind = kind)
}
