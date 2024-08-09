package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.data.model.HomeFeedSetting
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

@Dao
interface HomeFeedDao {
    @Query(HOME_FEED_QUERY)
    fun getHomeRootPostFlow(
        setting: HomeFeedSetting,
        until: Long,
        size: Int
    ): Flow<List<RootPostView>>

    @Query(HOME_FEED_QUERY)
    suspend fun getHomeRootPosts(
        setting: HomeFeedSetting,
        until: Long,
        size: Int
    ): List<RootPostView>

    @Query(HOME_FEED_EXISTS_QUERY)
    fun hasHomeRootPostsFlow(
        setting: HomeFeedSetting,
        until: Long = Long.MAX_VALUE,
        size: Int = 1
    ): Flow<Boolean>

    @Query(HOME_FEED_CREATED_AT_QUERY)
    suspend fun getHomeRootPostsCreatedAt(
        setting: HomeFeedSetting,
        until: Long,
        size: Int
    ): List<Long>
}
