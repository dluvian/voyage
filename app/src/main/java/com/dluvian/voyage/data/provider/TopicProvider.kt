package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.room.dao.TopicDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class TopicProvider(topicDao: TopicDao) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val topics = topicDao.getTopicsFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun getTopics(): List<Topic> {
        return topics.value
    }
}
