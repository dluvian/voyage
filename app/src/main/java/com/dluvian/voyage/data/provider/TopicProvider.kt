package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.room.dao.TopicDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class TopicProvider(nostrSubscriber: NostrSubscriber, topicDao: TopicDao) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val topics = topicDao.getTopicsFlow()
        .stateIn(scope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        nostrSubscriber.subMyTopics()
    }

    fun getTopics(): List<Topic> {
        return topics.value.ifEmpty { defaultTopics }
    }
}

// TODO: Remove default topics
private val defaultTopics = listOf("asknostr", "nostr", "bitcoin")
