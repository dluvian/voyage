package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.room.view.RootPostView
import kotlinx.coroutines.flow.Flow

private const val HOME_FEED_BASE_QUERY = "FROM RootPostView " +
        "WHERE createdAt <= :until " +
        "AND (authorIsFriend OR myTopic IS NOT NULL) " +
        "AND authorIsOneself = 0 " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val HOME_FEED_QUERY = "SELECT * $HOME_FEED_BASE_QUERY"
private const val HOME_FEED_CREATED_AT_QUERY = "SELECT createdAt $HOME_FEED_BASE_QUERY"

private const val HOME_FEED_EXISTS_QUERY = "SELECT EXISTS(SELECT * " +
        "FROM RootPostView " +
        "WHERE (authorIsFriend OR myTopic IS NOT NULL) " +
        "AND authorIsOneself = 0)"

private const val TOPIC_FEED_BASE_QUERY = "FROM RootPostView " +
        "WHERE createdAt <= :until " +
        "AND id IN (SELECT postId FROM hashtag WHERE hashtag = :topic) " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val TOPIC_FEED_QUERY = "SELECT * $TOPIC_FEED_BASE_QUERY"
private const val TOPIC_FEED_CREATED_AT_QUERY = "SELECT createdAt $TOPIC_FEED_BASE_QUERY"

private const val TOPIC_FEED_EXISTS_QUERY = "SELECT EXISTS(SELECT * " +
        "FROM RootPostView " +
        "WHERE id IN (SELECT postId FROM hashtag WHERE hashtag = :topic) " +
        "AND authorIsOneself = 0)"

private const val PROFILE_FEED_BASE_QUERY = "FROM RootPostView " +
        "WHERE createdAt <= :until " +
        "AND pubkey = :pubkey " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val PROFILE_FEED_QUERY = "SELECT * $PROFILE_FEED_BASE_QUERY"
private const val PROFILE_FEED_CREATED_AT_QUERY = "SELECT createdAt $PROFILE_FEED_BASE_QUERY"

private const val PROFILE_FEED_EXISTS_QUERY = "SELECT EXISTS(SELECT * " +
        "FROM RootPostView " +
        "WHERE pubkey = :pubkey)"

@Dao
interface RootPostDao {

    @Query("SELECT * FROM RootPostView WHERE id = :id")
    fun getRootPostFlow(id: EventIdHex): Flow<RootPostView?>

    @Query(HOME_FEED_QUERY)
    fun getHomeRootPostFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(HOME_FEED_EXISTS_QUERY)
    fun hasHomeRootPostsFlow(): Flow<Boolean>

    @Query(HOME_FEED_QUERY)
    suspend fun getHomeRootPosts(until: Long, size: Int): List<RootPostView>

    @Query(HOME_FEED_CREATED_AT_QUERY)
    suspend fun getHomeRootPostsCreatedAt(until: Long, size: Int): List<Long>

    @Query(TOPIC_FEED_QUERY)
    fun getTopicRootPostFlow(topic: Topic, until: Long, size: Int): Flow<List<RootPostView>>

    @Query(TOPIC_FEED_EXISTS_QUERY)
    fun hasTopicRootPostsFlow(topic: Topic): Flow<Boolean>

    @Query(TOPIC_FEED_QUERY)
    suspend fun getTopicRootPosts(topic: Topic, until: Long, size: Int): List<RootPostView>

    @Query(TOPIC_FEED_CREATED_AT_QUERY)
    suspend fun getTopicRootPostsCreatedAt(topic: Topic, until: Long, size: Int): List<Long>

    @Query(PROFILE_FEED_QUERY)
    fun getProfileRootPostFlow(pubkey: PubkeyHex, until: Long, size: Int): Flow<List<RootPostView>>

    @Query(PROFILE_FEED_EXISTS_QUERY)
    fun hasProfileRootPostsFlow(pubkey: PubkeyHex): Flow<Boolean>

    @Query(PROFILE_FEED_QUERY)
    suspend fun getProfileRootPosts(pubkey: PubkeyHex, until: Long, size: Int): List<RootPostView>

    @Query(PROFILE_FEED_CREATED_AT_QUERY)
    suspend fun getProfileRootPostsCreatedAt(pubkey: PubkeyHex, until: Long, size: Int): List<Long>
}
