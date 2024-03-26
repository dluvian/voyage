package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.model.ThreadUI
import kotlinx.coroutines.flow.Flow
import rust.nostr.protocol.Nip19Event

class ThreadProvider {
    fun getThread(nip19Event: Nip19Event): Flow<ThreadUI> {
        TODO()
    }
}
