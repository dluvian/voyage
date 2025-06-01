package com.dluvian.voyage.provider

import com.dluvian.voyage.NostrService
import com.dluvian.voyage.filterSetting.FeedSetting
import rust.nostr.sdk.Event
import rust.nostr.sdk.Timestamp

class FeedProvider(service: NostrService) {
    suspend fun buildFeed(
        until: Timestamp,
        setting: FeedSetting,
        hasPreviousPage: Boolean = false
    ): List<Event> {
        TODO()
    }
}
