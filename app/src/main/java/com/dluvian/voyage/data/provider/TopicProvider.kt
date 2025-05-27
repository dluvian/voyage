package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.model.TopicFollowState
import com.dluvian.voyage.core.utils.takeRandom
import com.dluvian.voyage.data.filterSetting.ListTopics
import com.dluvian.voyage.data.filterSetting.MyTopics
import com.dluvian.voyage.data.filterSetting.NoTopics
import com.dluvian.voyage.data.filterSetting.TopicSelection
import com.dluvian.voyage.data.room.dao.TopicDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class TopicProvider(
    val forcedFollowStates: Flow<Map<Topic, Boolean>>,
    private val topicDao: TopicDao,
    private val itemSetProvider: ItemSetProvider,
) {
    suspend fun getMyTopics(limit: Int = Int.MAX_VALUE): List<Topic> {
        return topicDao.getMyTopics().takeRandom(limit)
    }

    suspend fun getTopicSelection(
        topicSelection: TopicSelection,
        limit: Int = Int.MAX_VALUE
    ): List<Topic> {
        return when (topicSelection) {
            NoTopics -> emptyList()
            MyTopics -> getMyTopics(limit = limit)
            is ListTopics -> itemSetProvider.getTopicsFromList(
                identifier = topicSelection.identifier,
                limit = limit
            )
        }
    }

    suspend fun getAllTopics(): List<Topic> {
        return topicDao.getAllTopics()
    }

    suspend fun getPopularUnfollowedTopics(limit: Int): List<Topic> {
        return topicDao.getUnfollowedTopics(limit = limit)
            .ifEmpty { (defaultTopics - topicDao.getMyTopics().toSet()).shuffled() }
    }

    suspend fun getMyTopicsFlow(): Flow<List<TopicFollowState>> {
        // We want to be able to unfollow on the same list
        val myTopics = getMyTopics()

        return forcedFollowStates.map { forcedFollows ->
            myTopics.map { topic ->
                TopicFollowState(
                    topic = topic,
                    isFollowed = forcedFollows[topic] ?: true
                )
            }
        }
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

    private val defaultTopics = listOf(
        "voyage",
        "nostr",
        "asknostr",
        "introductions",
        "grownostr",
        "newstr",
        "bitcoin",
        "runstr",
        "bookstr",
        "devstr",
        "releastr"
    )
}
