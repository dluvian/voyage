package com.dluvian.voyage.data.event

import java.util.concurrent.atomic.AtomicLong

class OldestUsedEvent {
    private val oldestCreatedAt = AtomicLong(Long.MAX_VALUE)

    fun getOldestCreatedAt() = oldestCreatedAt.get()

    fun updateOldestCreatedAt(createdAt: Long?) {
        if (createdAt == null) return
        oldestCreatedAt.getAndUpdate { old -> if (createdAt < old) createdAt else old }
    }

    fun reset() = oldestCreatedAt.set(Long.MAX_VALUE)
}