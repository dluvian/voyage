package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.room.dao.TopicDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class TopicProvider(private val topicDao: TopicDao) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val myTopics = topicDao.getTopicsFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())
    private val allTopics = topicDao.getAllTopicsFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun getMyTopics(): List<Topic> {
        return myTopics.value
    }

    fun getAllTopics(): List<Topic> {
        return allTopics.value
    }

    fun getPopularUnfollowedTopics(limit: Int): Flow<List<Topic>> {
        return combine(
            topicDao.getUnfollowedTopicsFlow(limit = limit),
            myTopics,
        ) { unfollowed, myTopics ->
            unfollowed.ifEmpty { defaultTopics - myTopics.toSet() }
        }
    }

    fun isFollowed(topic: Topic): Boolean {
        return getMyTopics().contains(topic)
    }

    val defaultTopics = listOf(
        "voyage",
        "nostr",
        "asknostr",
        "foodstr",
        "food",
        "grownostr",
        "artstr",
        "art",
        "love",
        "nature",
        "photography",
        "news"
    )
}
