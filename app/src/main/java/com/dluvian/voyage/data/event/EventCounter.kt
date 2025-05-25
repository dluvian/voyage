package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.voyage.data.nostr.SubId
import rust.nostr.sdk.Filter

private const val TAG = "EventCounter"

class EventCounter {
    private val countdownCache = mutableMapOf<SubId, Int>()

    fun registerSubscription(subId: SubId, filters: Collection<Filter>) {
        if (filters.isEmpty()) return

        synchronized(countdownCache) {
            if (countdownCache.containsKey(subId)) {
                Log.w(TAG, "Subscription $subId is already registered")
                return
            }
        }

        val limits = filters.mapNotNull { it.asRecord().limit?.toInt() }
        if (limits.size < filters.size) {
            Log.w(TAG, "Subscription $subId has filters without limit")
            return
        }

        val count = limits.sum()
        synchronized(countdownCache) {
            val alreadyPresent = countdownCache.putIfAbsent(subId, count)
            if (alreadyPresent != null) {
                countdownCache[subId] = alreadyPresent - 1
            }
        }
    }

    fun isExceedingLimit(subId: SubId): Boolean {
        synchronized(countdownCache) {
            val currentCount = countdownCache[subId]
            if (currentCount == null || currentCount <= 0) return true

            countdownCache[subId] = currentCount - 1
        }

        return false
    }

    fun clear() {
        synchronized(countdownCache) {
            countdownCache.clear()
        }
    }
}
