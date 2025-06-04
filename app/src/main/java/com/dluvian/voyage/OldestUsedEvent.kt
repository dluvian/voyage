package com.dluvian.voyage

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rust.nostr.sdk.Timestamp

class OldestUsedEvent {
    private val mutex = Mutex()
    private var oldestCreatedAt = Timestamp.now()

    suspend fun createdAt(): Timestamp {
        return mutex.withLock { oldestCreatedAt }
    }

    suspend fun updateCreatedAt(createdAt: Timestamp?) {
        if (createdAt == null) return
        mutex.withLock {
            // TODO: Upstream: Wait for arithmetics
            if (createdAt.asSecs() < oldestCreatedAt.asSecs()) {
                oldestCreatedAt = createdAt
            }
        }
    }
}
