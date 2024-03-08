package com.dluvian.voyage.data.provider

import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.PubkeyHex

class RelayProvider {
    init {
        // TODO: subscribe my nip65
    }

    fun getReadRelays(): List<RelayUrl> {
        return defaultRelays
    }

    fun getWriteRelays(): List<RelayUrl> {
        return defaultRelays
    }

    fun getAutopilotRelays(withWebOfTrust: Boolean): Map<RelayUrl, Set<PubkeyHex>> {
        return emptyMap()
    }
}

private val defaultRelays = listOf(
    "wss://nos.lol",
    "wss://nostr.fmt.wiz.biz",
    "wss://nostr.oxtr.dev",
    "wss://nostr.sethforprivacy.com",
    "wss://relay.nostr.wirednet.jp",
)
