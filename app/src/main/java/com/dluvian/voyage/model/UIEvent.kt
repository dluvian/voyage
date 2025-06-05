package com.dluvian.voyage.model

import androidx.compose.ui.text.AnnotatedString
import com.dluvian.voyage.Topic
import rust.nostr.sdk.Event

// Event with extra data for UI
data class UIEvent(
    val event: Event,
    val annotatedContent: AnnotatedString,
    val authorProfile: TrustProfile,
    val upvoted: Boolean,
    val bookmarked: Boolean,
    val myTopic: Topic?,
    val inner: UIEvent? = null // For example for reposts
)
