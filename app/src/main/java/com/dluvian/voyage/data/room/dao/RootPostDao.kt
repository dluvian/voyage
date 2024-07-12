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
        "AND authorIsMuted = 0 " +
        "AND crossPostedAuthorIsMuted = 0 " +
        "AND NOT EXISTS (SELECT * FROM hashtag WHERE postId = id AND hashtag IN (SELECT mutedItem FROM mute WHERE tag IS 't')) " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val HOME_FEED_QUERY = "SELECT * $HOME_FEED_BASE_QUERY"
private const val HOME_FEED_CREATED_AT_QUERY = "SELECT createdAt $HOME_FEED_BASE_QUERY"
private const val HOME_FEED_EXISTS_QUERY = "SELECT EXISTS($HOME_FEED_QUERY)"

private const val TOPIC_FEED_BASE_QUERY = "FROM RootPostView " +
        "WHERE createdAt <= :until " +
        "AND authorIsMuted = 0 " +
        "AND crossPostedAuthorIsMuted = 0 " +
        "AND id IN (SELECT postId FROM hashtag WHERE hashtag = :topic) " +
        "AND NOT EXISTS (SELECT * FROM hashtag WHERE postId = id AND hashtag IN (SELECT mutedItem FROM mute WHERE tag IS 't' AND mutedItem IS NOT :topic)) " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val TOPIC_FEED_QUERY = "SELECT * $TOPIC_FEED_BASE_QUERY"
private const val TOPIC_FEED_CREATED_AT_QUERY = "SELECT createdAt $TOPIC_FEED_BASE_QUERY"
private const val TOPIC_FEED_EXISTS_QUERY = "SELECT EXISTS($TOPIC_FEED_QUERY)"

private const val PROFILE_FEED_BASE_QUERY = "FROM RootPostView " +
        "WHERE createdAt <= :until " +
        "AND pubkey = :pubkey " +
        "AND NOT EXISTS (SELECT * FROM hashtag WHERE postId = id AND hashtag IN (SELECT mutedItem FROM mute WHERE tag IS 't')) " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val PROFILE_FEED_QUERY = "SELECT * $PROFILE_FEED_BASE_QUERY"
private const val PROFILE_FEED_CREATED_AT_QUERY = "SELECT createdAt $PROFILE_FEED_BASE_QUERY"
private const val PROFILE_FEED_EXISTS_QUERY = "SELECT EXISTS($PROFILE_FEED_QUERY)"


private const val LIST_FEED_BASE_QUERY = """
    FROM RootPostView 
    WHERE createdAt <= :until
    AND (
        pubkey IN (SELECT pubkey FROM profileSetItem WHERE identifier = :identifier)
        OR id IN (SELECT postId FROM hashtag WHERE hashtag IN (SELECT topic FROM topicSetItem WHERE identifier = :identifier))
    )
    AND authorIsMuted = 0 
    AND crossPostedAuthorIsMuted = 0 
    AND NOT EXISTS (SELECT * FROM hashtag WHERE postId = id AND hashtag IN (SELECT mutedItem FROM mute WHERE tag IS 't'))
    ORDER BY createdAt DESC
    LIMIT :size
"""
private const val LIST_FEED_QUERY = "SELECT * $LIST_FEED_BASE_QUERY"
private const val LIST_FEED_CREATED_AT_QUERY = "SELECT createdAt $LIST_FEED_BASE_QUERY"
private const val LIST_FEED_EXISTS_QUERY = "SELECT EXISTS($LIST_FEED_QUERY)"

@Dao
interface RootPostDao {

    @Query("SELECT * FROM RootPostView WHERE id = :id")
    fun getRootPostFlow(id: EventIdHex): Flow<RootPostView?>


    @Query(HOME_FEED_QUERY)
    fun getHomeRootPostFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(HOME_FEED_QUERY)
    suspend fun getHomeRootPosts(until: Long, size: Int): List<RootPostView>

    @Query(HOME_FEED_EXISTS_QUERY)
    fun hasHomeRootPostsFlow(until: Long = Long.MAX_VALUE, size: Int = 1): Flow<Boolean>

    @Query(HOME_FEED_CREATED_AT_QUERY)
    suspend fun getHomeRootPostsCreatedAt(until: Long, size: Int): List<Long>


    @Query(TOPIC_FEED_QUERY)
    fun getTopicRootPostFlow(topic: Topic, until: Long, size: Int): Flow<List<RootPostView>>

    @Query(TOPIC_FEED_EXISTS_QUERY)
    fun hasTopicRootPostsFlow(
        topic: Topic,
        until: Long = Long.MAX_VALUE,
        size: Int = 1
    ): Flow<Boolean>

    @Query(TOPIC_FEED_QUERY)
    suspend fun getTopicRootPosts(topic: Topic, until: Long, size: Int): List<RootPostView>

    @Query(TOPIC_FEED_CREATED_AT_QUERY)
    suspend fun getTopicRootPostsCreatedAt(topic: Topic, until: Long, size: Int): List<Long>


    @Query(PROFILE_FEED_QUERY)
    fun getProfileRootPostFlow(pubkey: PubkeyHex, until: Long, size: Int): Flow<List<RootPostView>>

    @Query(PROFILE_FEED_EXISTS_QUERY)
    fun hasProfileRootPostsFlow(
        pubkey: PubkeyHex,
        until: Long = Long.MAX_VALUE,
        size: Int = 1
    ): Flow<Boolean>

    @Query(PROFILE_FEED_QUERY)
    suspend fun getProfileRootPosts(pubkey: PubkeyHex, until: Long, size: Int): List<RootPostView>

    @Query(PROFILE_FEED_CREATED_AT_QUERY)
    suspend fun getProfileRootPostsCreatedAt(pubkey: PubkeyHex, until: Long, size: Int): List<Long>


    @Query(LIST_FEED_QUERY)
    fun getListRootPostFlow(identifier: String, until: Long, size: Int): Flow<List<RootPostView>>

    @Query(LIST_FEED_EXISTS_QUERY)
    fun hasListRootPostsFlow(
        identifier: String,
        until: Long = Long.MAX_VALUE,
        size: Int = 1
    ): Flow<Boolean>

    @Query(LIST_FEED_QUERY)
    suspend fun getListRootPosts(identifier: String, until: Long, size: Int): List<RootPostView>

    @Query(LIST_FEED_CREATED_AT_QUERY)
    suspend fun getListRootPostsCreatedAt(identifier: String, until: Long, size: Int): List<Long>
}
