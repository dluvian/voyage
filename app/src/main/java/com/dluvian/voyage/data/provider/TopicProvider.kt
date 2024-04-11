package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.room.dao.TopicDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class TopicProvider(
    private val topicDao: TopicDao,
    val forcedFollowStates: Flow<Map<Topic, Boolean>>,
) {
    suspend fun getMyTopics() = topicDao.getMyTopics()
    suspend fun getAllTopics() = topicDao.getAllTopics()

    suspend fun getPopularUnfollowedTopics(limit: Int): List<Topic> {
        return topicDao.getUnfollowedTopics(limit = limit)
            .ifEmpty { (defaultTopics - topicDao.getMyTopics().toSet()).shuffled() }
    }

    fun getIsFollowedFlow(topic: Topic): Flow<Boolean> {
        return combine(
            topicDao.getIsFollowedFlow(topic = topic),
            forcedFollowStates
        ) { db, forced ->
            forced[topic] ?: db
        }
    }

    // Not named "getMaxCreatedAt" bc there should only be one createdAt available
    suspend fun getCreatedAt() = topicDao.getMaxCreatedAt()

    val defaultTopics = listOf(
        "voyage",
        "nostr",
        "asknostr",
        "introductions",
        "foodstr",
        "food",
        "grownostr",
        "artstr",
        "art",
        "nature",
        "photography",
        "news",
        "newstr",
        "bitcoin",
        "fitness",
        "japan",
        "spain",
        "travel",
        "farmstr",
        "running",
    )
}
