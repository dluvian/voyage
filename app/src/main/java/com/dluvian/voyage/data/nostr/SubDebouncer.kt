package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.LONG_DEBOUNCE
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.data.model.FilterWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "SubDebouncer"

class SubDebouncer(private val subCreator: SubscriptionCreator) {
    private val queue = mutableMapOf<RelayUrl, List<FilterWrapper>>()
    private val isProcessingSubs = AtomicBoolean(false)
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        startProcessingJob()
    }

    fun submit(relayUrl: RelayUrl, filters: List<FilterWrapper>) {
        synchronized(queue) {
            queue[relayUrl] = filters
        }
        startProcessingJob()
    }

    private fun startProcessingJob() {
        if (!isProcessingSubs.compareAndSet(false, true)) return
        Log.i(TAG, "Start job")
        scope.launchIO {
            while (true) {
                delay(LONG_DEBOUNCE)
                val subs = mutableMapOf<RelayUrl, List<FilterWrapper>>()
                synchronized(queue) {
                    subs.putAll(queue)
                    queue.clear()
                }
                subs.forEach { (relay, filters) ->
                    subCreator.subscribe(relayUrl = relay, filters = filters)
                }
            }
        }.invokeOnCompletion {
            Log.w(TAG, "Processing job completed", it)
            isProcessingSubs.set(false)
        }
    }
}
