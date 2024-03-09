package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.Topic

class TopicProvider {

    init {
        // TODO: sub topics
    }

    fun getTopics(): List<Topic> {
        return defaultTopics
    }
}

private val defaultTopics = listOf("asknostr", "nostr", "bitcoin")
