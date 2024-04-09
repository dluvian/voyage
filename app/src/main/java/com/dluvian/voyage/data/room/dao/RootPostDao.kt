package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.room.view.RootPostView
import kotlinx.coroutines.flow.Flow

private const val HOME_FEED_QUERY = "SELECT * " +
        "FROM RootPostView " +
        "WHERE createdAt <= :until " +
        "AND (authorIsFriend OR myTopic IS NOT NULL) " +
        "AND authorIsMe = 0 " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val TOPIC_FEED_QUERY = "SELECT * " +
        "FROM RootPostView " +
        "WHERE createdAt <= :until " +
        "AND id IN (SELECT postId FROM hashtag WHERE hashtag = :topic) " +
        "AND authorIsMe = 0 " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val PROFILE_FEED_QUERY = "SELECT * " +
        "FROM RootPostView " +
        "WHERE createdAt <= :until " +
        "AND pubkey = :pubkey " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

@Dao
interface RootPostDao {

    @Query("SELECT * FROM RootPostView WHERE id = :id")
    fun getRootPostFlow(id: EventIdHex): Flow<RootPostView?>

    @Query(HOME_FEED_QUERY)
    fun getHomeRootPostFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(HOME_FEED_QUERY)
    suspend fun getHomeRootPosts(until: Long, size: Int): List<RootPostView>

    @Query(TOPIC_FEED_QUERY)
    fun getTopicRootPostFlow(topic: Topic, until: Long, size: Int): Flow<List<RootPostView>>

    @Query(TOPIC_FEED_QUERY)
    suspend fun getTopicRootPosts(topic: Topic, until: Long, size: Int): List<RootPostView>

    @Query(PROFILE_FEED_QUERY)
    fun getProfileRootPostFlow(pubkey: PubkeyHex, until: Long, size: Int): Flow<List<RootPostView>>

    @Query(PROFILE_FEED_QUERY)
    suspend fun getProfileRootPosts(pubkey: PubkeyHex, until: Long, size: Int): List<RootPostView>
}
