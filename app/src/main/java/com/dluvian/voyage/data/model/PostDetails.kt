package com.dluvian.voyage.data.model

import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.nostr.RelayUrl

data class PostDetails(
    val indexedTopics: List<Topic>,
    val client: String?,
    val pollEndsAt: Long?,
    val base: PostDetailsBase,
)

data class PostDetailsBase(
    val id: EventIdHex,
    val firstSeenIn: RelayUrl,
    val createdAt: Long,
    val json: String,
)
