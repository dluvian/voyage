package com.dluvian.voyage.data

import rust.nostr.protocol.Keys
import rust.nostr.protocol.Timestamp

class SingleUseKeyManager {
    private val singleUsePostingAccount = 40000u
    private val mnemonic =
        "leader monkey parrot ring guide accident before fence cannon height naive bean"

    fun getPostingKeys(timestamp: Timestamp): Keys {
        return Keys.fromMnemonic(mnemonic, passphrase = null, account = singleUsePostingAccount)
    }
}
