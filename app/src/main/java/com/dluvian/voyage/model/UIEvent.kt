package com.dluvian.voyage.model

import rust.nostr.sdk.Event

// Event with extra data for UI
data class UIEvent(
    val event: Event,
    val authorProfile: TrustProfile,
    val upvoted: Boolean,
    val bookmarked: Boolean,
    val inner: UIEvent? = null // For example for reposts
)
