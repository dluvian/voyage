package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.room.dao.TopicDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
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

    suspend fun getPopularUnfollowedTopics(limit: Int): List<Topic> {
        return topicDao.getUnfollowedTopics(limit = limit)
            .ifEmpty { (defaultTopics - myTopics.value.toSet()).shuffled() }
    }

    fun isFollowed(topic: Topic): Boolean {
        return getMyTopics().contains(topic)
    }

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
        "love",
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
        "surfing",
        "cycling",
    )
}
