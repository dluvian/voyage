package com.dluvian.voyage.data

import java.util.concurrent.atomic.AtomicLong

class OldestUsedEvent {
    private val oldestCreatedAt = AtomicLong(Long.MAX_VALUE)

    fun getOldestCreatedAt() = oldestCreatedAt.get()

    fun updateOldestCreatedAt(createdAt: Long?) {
        if (createdAt == null) return
        oldestCreatedAt.getAndUpdate { old -> if (createdAt < old) createdAt else old }
    }

    // No reset method.
    // Resetting in EventSweeper could be fatal if user closes and reopens many times
}
