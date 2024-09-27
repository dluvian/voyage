package com.dluvian.voyage.core.model

import com.dluvian.voyage.core.Bech32
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.nostr.removeMentionChar
import com.dluvian.voyage.data.nostr.removeNostrUri
import rust.nostr.protocol.Coordinate
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Nip19Event
import rust.nostr.protocol.Nip19Profile
import rust.nostr.protocol.PublicKey

sealed class NostrMention(open val bech32: Bech32, open val hex: String) {
    companion object {
        fun from(text: String): NostrMention? {
            val trimmed = text.removeNostrUri().removeMentionChar()
            return if (trimmed.startsWith("nprofile1")) {
                val result = runCatching {
                    Nip19Profile.fromBech32(bech32 = trimmed)
                }.getOrNull() ?: return null
                NprofileMention(
                    bech32 = trimmed,
                    hex = result.publicKey().toHex(),
                    nprofile = result
                )
            } else if (trimmed.startsWith("nevent1")) {
                val result = runCatching {
                    Nip19Event.fromBech32(bech32 = trimmed)
                }.getOrNull() ?: return null
                NeventMention(bech32 = trimmed, hex = result.eventId().toHex())
            } else if (trimmed.startsWith("npub1")) {
                val result = runCatching {
                    PublicKey.fromBech32(bech32 = trimmed)
                }.getOrNull() ?: return null
                NpubMention(bech32 = trimmed, hex = result.toHex())
            } else if (trimmed.startsWith("note1")) {
                val result = runCatching {
                    EventId.fromBech32(bech32 = trimmed)
                }.getOrNull() ?: return null
                NoteMention(bech32 = trimmed, hex = result.toHex())
            } else if (trimmed.startsWith("naddr1")) {
                val result = runCatching {
                    Coordinate.fromBech32(bech32 = trimmed)
                }.getOrNull() ?: return null
                CoordinateMention(
                    bech32 = trimmed,
                    hex = result.publicKey().toHex(),
                    identifier = result.identifier().trim()
                )
            } else null
        }
    }
}

data class NpubMention(override val bech32: Bech32, override val hex: PubkeyHex) :
    NostrMention(bech32 = bech32, hex = hex)

data class NprofileMention(
    override val bech32: Bech32,
    override val hex: PubkeyHex,
    val nprofile: Nip19Profile
) :
    NostrMention(bech32 = bech32, hex = hex)

data class NoteMention(override val bech32: Bech32, override val hex: EventIdHex) :
    NostrMention(bech32 = bech32, hex = hex)

data class NeventMention(override val bech32: Bech32, override val hex: EventIdHex) :
    NostrMention(bech32 = bech32, hex = hex)

data class CoordinateMention(
    override val bech32: Bech32,
    override val hex: EventIdHex,
    val identifier: String
) : NostrMention(bech32 = bech32, hex = hex)
