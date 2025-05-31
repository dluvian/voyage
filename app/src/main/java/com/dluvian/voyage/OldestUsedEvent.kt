package com.dluvian.voyage

import rust.nostr.sdk.Timestamp

class OldestUsedEvent {
    private var oldestCreatedAt = Timestamp.now()

    fun createdAt() = oldestCreatedAt

    fun updateCreatedAt(createdAt: Timestamp?) {
        if (createdAt == null) return

        if (createdAt.asSecs() < oldestCreatedAt.asSecs()) {
            oldestCreatedAt = createdAt
        }
    }
}
