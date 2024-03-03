package com.dluvian.nostr_kt

import rust.nostr.protocol.Event


fun Event.isPost(): Boolean {
    return this.kind().toInt() == Kind.TEXT_NOTE
}