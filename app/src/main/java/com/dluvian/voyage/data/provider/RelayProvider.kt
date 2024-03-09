package com.dluvian.voyage.data.provider

import com.dluvian.nostr_kt.RelayUrl

class RelayProvider {

    fun getReadRelays(): List<RelayUrl> {
        // TODO: Nip65
        return defaultRelays
    }

    fun getWriteRelays(): List<RelayUrl> {
        return defaultRelays
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
