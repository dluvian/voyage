package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.nostr_kt.NostrClient
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.SubId
import rust.nostr.protocol.Filter

private const val TAG = "SubscriptionCreator"

class SubscriptionCreator(
    private val nostrClient: NostrClient,
    private val syncedFilterCache: MutableMap<SubId, List<Filter>>,
) {
    fun subscribe(relayUrl: RelayUrl, filters: List<Filter>): SubId? {
        if (filters.isEmpty()) return null
        Log.d(TAG, "Subscribe ${filters.size} in $relayUrl")

        val subId = nostrClient.subscribe(filters = filters, relayUrl = relayUrl)
        if (subId == null) {
            Log.w(TAG, "Failed to create subscription ID")
            return null
        }
        syncedFilterCache[subId] = filters

        return subId
    }

    fun unsubAll() = nostrClient.unsubscribeAll()
}
