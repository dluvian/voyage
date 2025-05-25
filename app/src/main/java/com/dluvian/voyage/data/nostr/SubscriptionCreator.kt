package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.voyage.data.event.EventCounter
import rust.nostr.sdk.Filter

private const val TAG = "SubscriptionCreator"

class SubscriptionCreator(
    private val nostrClient: NostrClient,
    private val syncedFilterCache: MutableMap<SubId, List<Filter>>,
    private val eventCounter: EventCounter,
) {
    fun subscribe(relayUrl: RelayUrl, filter: Filter): SubId? {
        Log.d(TAG, "Subscribe filter in $relayUrl")

        val subId = nostrClient.subscribe(filter = filter, relayUrl = relayUrl)
        if (subId == null) {
            Log.w(TAG, "Failed to create subscription ID")
            return null
        }
        syncedFilterCache[subId] = listOf(filter) // TODO: adjust for single filter reqs
        eventCounter.registerSubscription(subId = subId, filters = listOf(filter))

        return subId
    }

    // TODO: whack
    fun subscribeMany(relayUrl: RelayUrl, filters: List<Filter>) {
        filters.forEach {
            subscribe(relayUrl = relayUrl, filter = it)
        }
    }

    fun unsubAll() = nostrClient.unsubscribeAll()
}
