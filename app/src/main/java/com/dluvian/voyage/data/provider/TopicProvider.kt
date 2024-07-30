package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.model.TopicFollowState
import com.dluvian.voyage.core.model.TopicMuteState
import com.dluvian.voyage.core.utils.takeRandom
import com.dluvian.voyage.data.model.ListTopics
import com.dluvian.voyage.data.model.MyTopics
import com.dluvian.voyage.data.model.TopicSelection
import com.dluvian.voyage.data.room.dao.MuteDao
import com.dluvian.voyage.data.room.dao.TopicDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class TopicProvider(
    val forcedFollowStates: Flow<Map<Topic, Boolean>>,
    val forcedMuteStates: Flow<Map<Topic, Boolean>>,
    private val topicDao: TopicDao,
    private val muteDao: MuteDao,
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

    suspend fun getMutedTopicsFlow(): Flow<List<TopicMuteState>> {
        // We want to be able to unmute on the same list
        val mutedTopics = muteDao.getMyTopicMutes()

        return forcedMuteStates.map { forcedMutes ->
            mutedTopics.map { topic ->
                TopicMuteState(
                    topic = topic,
                    isMuted = forcedMutes[topic] ?: true
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

    fun getIsMutedFlow(topic: Topic): Flow<Boolean> {
        return combine(
            muteDao.getTopicIsMutedFlow(topic = topic),
            forcedMuteStates
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
