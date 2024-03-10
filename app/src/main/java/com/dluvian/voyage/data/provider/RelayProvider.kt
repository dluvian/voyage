package com.dluvian.voyage.data.provider

import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.MAX_RELAYS
import com.dluvian.voyage.core.PubkeyHex


// TODO: Nip65
class RelayProvider {
    fun getReadRelays(limit: Boolean = true): List<RelayUrl> {
        return if (limit && defaultRelays.size > MAX_RELAYS)
            defaultRelays.shuffled().take(MAX_RELAYS)
        else defaultRelays
    }

    fun getWriteRelays(limit: Boolean = true): List<RelayUrl> {
        return defaultRelays
    }

    fun getPublishRelays(publishTo: PubkeyHex): List<RelayUrl> {
        return getWriteRelays() // TODO: write relays + read relays of publishTo
    }

    fun getAutopilotRelays(pubkeys: Collection<PubkeyHex>): Map<RelayUrl, Set<PubkeyHex>> {
        return getReadRelays().associateWith { pubkeys.toSet() } // TODO: Autopilot implementation
    }
}

private val defaultRelays = listOf(
    "wss://nos.lol",
    "wss://nostr.einundzwanzig.space",
    "wss://relay.primal.net",
    "wss://nostr.oxtr.dev",
    "wss://relay.mutinywallet.com",
    "wss://nostr.fmt.wiz.biz",
    "wss://relay.nostr.wirednet.jp",
)
